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

import io.dapr.durabletask.Task;
import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import io.quarkiverse.dapr.workflows.WorkflowMetadata;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Durable parallel composite: runs all sub-agents concurrently as {@code react-agent} child
 * workflows from the same seed state, then collects each output under its state key.
 *
 * <p>Control-inversion replacement for {@code ParallelOrchestrationWorkflow}. Fan-out uses
 * {@link io.dapr.workflows.WorkflowContext#allOf}; the children land on whatever replicas
 * Dapr chooses, which is safe because each is a self-contained durable workflow. Reuses
 * {@link DurableSequenceInput} (sub-agents are independent here — no state threading).
 */
@ApplicationScoped
@WorkflowMetadata(name = "durable-parallel")
public class DurableParallelWorkflow implements Workflow {

  private static final int CHILD_MAX_STEPS = 16;

  @Override
  public WorkflowStub create() {
    return ctx -> {
      DurableSequenceInput input = ctx.getInput(DurableSequenceInput.class);

      List<Task<String>> tasks = new ArrayList<>();
      for (SubAgentSpec spec : input.subAgents()) {
        String userMessage = DurableRendering.render(spec.userMessageTemplate(), input.initialState());
        tasks.add(ctx.callChildWorkflow("react-agent",
            new ReActInput(spec.agentName(), null, userMessage, null, CHILD_MAX_STEPS), String.class));
      }

      List<String> outputs = ctx.allOf(tasks).await();

      Map<String, String> state = new HashMap<>(input.initialState());
      for (int i = 0; i < input.subAgents().size(); i++) {
        state.put(input.subAgents().get(i).outputKey(), outputs.get(i));
      }

      String fallback = String.join("\n\n", outputs);
      ctx.complete(DurableOutput.resolve(input.combiner(), input.finalOutputKey(), state, fallback));
    };
  }
}
