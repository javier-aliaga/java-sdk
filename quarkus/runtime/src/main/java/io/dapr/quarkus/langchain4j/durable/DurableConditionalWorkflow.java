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

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import io.quarkiverse.dapr.workflows.WorkflowMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Durable conditional composite: evaluates each branch's {@code @ActivationCondition} predicate
 * in order and runs the first matching sub-agent as a {@code react-agent} child workflow.
 *
 * <p>Control-inversion replacement for {@code ConditionalOrchestrationWorkflow}. The activation
 * conditions are pure static predicates, so they are invoked reflectively here — deterministic
 * given the (replayed) state, hence replay-safe. (A condition that performs I/O would break
 * determinism; activation conditions are expected to be pure.)
 */
@ApplicationScoped
@WorkflowMetadata(name = "durable-conditional")
public class DurableConditionalWorkflow implements Workflow {

  private static final Logger LOG = Logger.getLogger(DurableConditionalWorkflow.class);
  private static final int CHILD_MAX_STEPS = 16;

  @Override
  public WorkflowStub create() {
    return ctx -> {
      DurableConditionalInput input = ctx.getInput(DurableConditionalInput.class);
      Map<String, String> state = new HashMap<>(input.initialState());

      for (ConditionalBranch branch : input.branches()) {
        if (!matches(branch, state)) {
          continue;
        }
        SubAgentSpec agent = branch.agent();
        String userMessage = DurableRendering.render(agent.userMessageTemplate(), state);
        String output = ctx.callChildWorkflow("react-agent",
            new ReActInput(agent.agentName(), null, userMessage, null, CHILD_MAX_STEPS),
            String.class).await();
        state.put(agent.outputKey(), output);
        String result = input.finalOutputKey() != null
            ? state.get(input.finalOutputKey()) : output;
        ctx.complete(result);
        return;
      }

      // No branch matched.
      ctx.complete(input.finalOutputKey() != null ? state.get(input.finalOutputKey()) : null);
    };
  }

  private static boolean matches(ConditionalBranch branch, Map<String, String> state) {
    if (branch.conditionClass() == null || branch.conditionMethod() == null) {
      return true; // unconditional branch
    }
    try {
      Class<?> declaring = Class.forName(
          branch.conditionClass(), true, Thread.currentThread().getContextClassLoader());
      Method method = findMethod(declaring, branch.conditionMethod());
      Object[] args = new Object[method.getParameterCount()];
      Class<?>[] types = method.getParameterTypes();
      for (int i = 0; i < args.length; i++) {
        String value = i < branch.conditionVars().size() ? state.get(branch.conditionVars().get(i)) : null;
        args[i] = coerce(value, types[i]);
      }
      return Boolean.TRUE.equals(method.invoke(null, args));
    } catch (ReflectiveOperationException e) {
      LOG.warnf("Activation condition %s#%s failed: %s — treating as not matched",
          branch.conditionClass(), branch.conditionMethod(), e.getMessage());
      return false;
    }
  }

  private static Method findMethod(Class<?> declaring, String name) throws NoSuchMethodException {
    for (Method m : declaring.getDeclaredMethods()) {
      if (m.getName().equals(name)) {
        m.setAccessible(true);
        return m;
      }
    }
    throw new NoSuchMethodException(declaring.getName() + "#" + name);
  }

  private static Object coerce(String value, Class<?> type) {
    if (value == null) {
      return type.isPrimitive() ? defaultPrimitive(type) : null;
    }
    if (type == String.class) {
      return value;
    }
    if (type == int.class || type == Integer.class) {
      return Integer.parseInt(value);
    }
    if (type == long.class || type == Long.class) {
      return Long.parseLong(value);
    }
    if (type == boolean.class || type == Boolean.class) {
      return Boolean.parseBoolean(value);
    }
    if (type == double.class || type == Double.class) {
      return Double.parseDouble(value);
    }
    return value;
  }

  private static Object defaultPrimitive(Class<?> type) {
    if (type == boolean.class) {
      return false;
    }
    if (type == int.class) {
      return 0;
    }
    if (type == long.class) {
      return 0L;
    }
    if (type == double.class) {
      return 0d;
    }
    return 0;
  }
}
