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
 * Durable sequential composite: runs each sub-agent in order as a {@code react-agent} child
 * workflow, threading shared state between steps.
 *
 * <p>This is the control-inversion replacement for {@code SequentialOrchestrationWorkflow}: it
 * calls child workflows directly via {@link io.dapr.workflows.WorkflowContext#callChildWorkflow}
 * instead of bridging an in-memory LangChain4j planner. Because each child is itself a durable
 * {@link ReActAgentWorkflow}, the whole tree is replayable and replica-agnostic — no
 * {@code DaprWorkflowPlanner}, exchange queue, or per-thread context.
 *
 * <p>The parallel / loop / conditional composites follow the same shape ({@code allOf} for
 * parallel, a counted/condition loop, an {@code if} on a condition activity). Added alongside
 * the existing planner-bridge composites for comparison; the bridge is removed at the cutover.
 */
@ApplicationScoped
@WorkflowMetadata(name = "durable-sequence")
public class DurableSequenceWorkflow implements Workflow {

  private static final int CHILD_MAX_STEPS = 16;

  @Override
  public WorkflowStub create() {
    return ctx -> {
      DurableSequenceInput input = ctx.getInput(DurableSequenceInput.class);

      Map<String, String> state = new HashMap<>(input.initialState());
      String lastOutput = null;

      for (SubAgentSpec spec : input.subAgents()) {
        String userMessage = DurableRendering.render(spec.userMessageTemplate(), state);
        ReActInput childInput = new ReActInput(
            spec.agentName(), null, userMessage, null, CHILD_MAX_STEPS);

        // Deterministic child instance id is auto-assigned by the runtime; the child is a
        // durable react-agent, so this step is itself recoverable/replica-agnostic.
        String output = ctx.callChildWorkflow("react-agent", childInput, String.class).await();
        state.put(spec.outputKey(), output);
        lastOutput = output;
      }

      ctx.complete(DurableOutput.resolve(input.combiner(), input.finalOutputKey(), state, lastOutput));
    };
  }
}
