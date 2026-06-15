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

import java.util.HashMap;
import java.util.Map;

/**
 * Durable conditional composite: runs one of two sub-agents (each a {@code react-agent} child)
 * based on a data-driven condition over the seed state.
 *
 * <p>Control-inversion replacement for {@code ConditionalOrchestrationWorkflow}. This
 * representative tests a state value for equality; a richer version would evaluate a condition
 * activity (mirroring the existing {@code ConditionCheckActivity}) so the predicate can run
 * arbitrary logic deterministically.
 */
@ApplicationScoped
@WorkflowMetadata(name = "durable-conditional")
public class DurableConditionalWorkflow implements Workflow {

  private static final int CHILD_MAX_STEPS = 16;

  @Override
  public WorkflowStub create() {
    return ctx -> {
      DurableConditionalInput input = ctx.getInput(DurableConditionalInput.class);

      Map<String, String> state = new HashMap<>(input.initialState());
      boolean match = input.expectedValue() != null
          && input.expectedValue().equals(state.get(input.conditionKey()));

      SubAgentSpec chosen;
      if (match) {
        chosen = input.subAgents().get(0);
      } else if (input.subAgents().size() > 1) {
        chosen = input.subAgents().get(1);
      } else {
        ctx.complete(input.finalOutputKey() != null ? state.get(input.finalOutputKey()) : null);
        return;
      }

      String userMessage = DurableRendering.render(chosen.userMessageTemplate(), state);
      String output = ctx.callChildWorkflow("react-agent",
          new ReActInput(chosen.agentName(), null, userMessage, null, CHILD_MAX_STEPS),
          String.class).await();
      state.put(chosen.outputKey(), output);

      String result = input.finalOutputKey() != null
          ? state.get(input.finalOutputKey()) : output;
      ctx.complete(result);
    };
  }
}
