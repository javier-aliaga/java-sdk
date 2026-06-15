/*
 * Copyright 2026 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
limitations under the License.
*/

package io.dapr.quarkus.langchain4j.durable;

import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.WorkflowInstanceStatus;
import io.quarkus.arc.Arc;
import org.jboss.logging.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * The {@code java.lang.reflect.Proxy} handler behind a durable agent bean.
 *
 * <p>Replaces the AiServices-built agent: each call renders the agent's templates from the
 * method arguments, starts the matching durable workflow ({@code react-agent} for a leaf,
 * {@code durable-*} for a composite), waits for it, and returns the result. This is what makes
 * the control-inversion engine "drop-in" — the user's {@code @Agent} interface is unchanged.
 */
public class DurableAgentInvocationHandler implements InvocationHandler {

  private static final Logger LOG = Logger.getLogger(DurableAgentInvocationHandler.class);
  private static final int LEAF_MAX_STEPS = 16;
  private static final int WAIT_MINUTES = 10;

  private final Map<String, AgentMethodMeta> metasByMethod;

  public DurableAgentInvocationHandler(Map<String, AgentMethodMeta> metasByMethod) {
    this.metasByMethod = metasByMethod;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) {
    if (method.getDeclaringClass() == Object.class) {
      return switch (method.getName()) {
        case "toString" -> "DurableAgentProxy" + metasByMethod.keySet();
        case "hashCode" -> System.identityHashCode(proxy);
        case "equals" -> proxy == (args == null ? null : args[0]);
        default -> throw new UnsupportedOperationException(method.getName());
      };
    }

    AgentMethodMeta meta = metasByMethod.get(method.getName());
    if (meta == null) {
      throw new IllegalStateException("No durable metadata for agent method " + method.getName());
    }

    // Build the initial state from the @V parameter names.
    Map<String, String> state = new HashMap<>();
    if (args != null) {
      for (int i = 0; i < meta.varNames().size() && i < args.length; i++) {
        if (args[i] != null) {
          state.put(meta.varNames().get(i), String.valueOf(args[i]));
        }
      }
    }

    Object input = buildInput(meta, state);
    String instanceId = meta.agentName() + "-" + UUID.randomUUID();
    LOG.infof("[DurableAgent:%s] starting %s workflow %s", meta.agentName(), meta.workflowName(), instanceId);

    DaprWorkflowClient client = Arc.container().instance(DaprWorkflowClient.class).get();
    client.scheduleNewWorkflow(meta.workflowName(), input, instanceId);
    try {
      WorkflowInstanceStatus status =
          client.waitForInstanceCompletion(instanceId, Duration.ofMinutes(WAIT_MINUTES), true);
      String result = status.readOutputAs(String.class);
      return method.getReturnType() == void.class ? null : result;
    } catch (TimeoutException e) {
      throw new IllegalStateException(
          "Durable agent '" + meta.agentName() + "' did not complete within " + WAIT_MINUTES + "m", e);
    }
  }

  private static Object buildInput(AgentMethodMeta meta, Map<String, String> state) {
    return switch (meta.workflowName()) {
      case "react-agent" -> new ReActInput(
          meta.agentName(),
          DurableRendering.render(meta.systemTemplate(), state),
          DurableRendering.render(meta.userTemplate(), state),
          null,
          LEAF_MAX_STEPS);
      case "durable-sequence", "durable-parallel" -> new DurableSequenceInput(
          meta.subAgents(), state, meta.outputKey());
      case "durable-loop" -> new DurableLoopInput(
          meta.subAgents(), state, meta.outputKey(), meta.maxIterations());
      default -> throw new IllegalStateException("Unsupported durable workflow: " + meta.workflowName());
    };
  }
}
