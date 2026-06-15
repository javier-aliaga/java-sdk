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
 * Durable loop composite: runs the sub-agent sequence a fixed number of iterations, threading
 * state across iterations (so each pass can refine the previous output).
 *
 * <p>Control-inversion replacement for {@code LoopOrchestrationWorkflow}. This representative
 * uses a counted loop; a predicate-based exit would evaluate a condition activity at the top
 * of each iteration (mirroring the existing {@code ExitConditionCheckActivity}).
 */
@ApplicationScoped
@WorkflowMetadata(name = "durable-loop")
public class DurableLoopWorkflow implements Workflow {

  private static final int CHILD_MAX_STEPS = 16;

  @Override
  public WorkflowStub create() {
    return ctx -> {
      DurableLoopInput input = ctx.getInput(DurableLoopInput.class);

      Map<String, String> state = new HashMap<>(input.initialState());
      String lastOutput = null;
      int iterations = input.maxIterations() > 0 ? input.maxIterations() : 1;

      for (int iteration = 0; iteration < iterations; iteration++) {
        for (SubAgentSpec spec : input.subAgents()) {
          String userMessage = DurableRendering.render(spec.userMessageTemplate(), state);
          String output = ctx.callChildWorkflow("react-agent",
              new ReActInput(spec.agentName(), null, userMessage, null, CHILD_MAX_STEPS),
              String.class).await();
          state.put(spec.outputKey(), output);
          lastOutput = output;
        }
      }

      String result = input.finalOutputKey() != null
          ? state.get(input.finalOutputKey()) : lastOutput;
      ctx.complete(result);
    };
  }
}
