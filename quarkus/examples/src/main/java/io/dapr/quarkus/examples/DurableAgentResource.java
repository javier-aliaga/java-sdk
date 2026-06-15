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

package io.dapr.quarkus.examples;

import io.dapr.quarkus.langchain4j.durable.DurableConditionalInput;
import io.dapr.quarkus.langchain4j.durable.DurableLoopInput;
import io.dapr.quarkus.langchain4j.durable.DurableSequenceInput;
import io.dapr.quarkus.langchain4j.durable.ReActInput;
import io.dapr.quarkus.langchain4j.durable.SubAgentSpec;
import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.WorkflowInstanceStatus;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Runnable entry point for the control-inversion approach (milestone 1).
 *
 * <p>Renders a prompt from the request and starts the durable
 * {@link io.dapr.quarkus.langchain4j.durable.ReActAgentWorkflow} directly via the Dapr
 * workflow client — the agent's ReAct loop runs <em>as</em> a workflow, so any LLM/tool
 * call is replayable and replica-agnostic. This stands in for the eventual generated
 * {@code @Agent} proxy that will start the workflow transparently.
 *
 * <pre>
 * curl "http://localhost:8080/durable?topic=dragons"
 * </pre>
 */
@Path("/durable")
public class DurableAgentResource {

  @Inject
  DaprWorkflowClient workflowClient;

  /**
   * Starts the durable ReAct workflow for a single creative-writer agent and returns its result.
   *
   * @param topic the story topic
   * @return the generated story text
   * @throws TimeoutException if the workflow does not complete within the wait window
   */
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String run(@QueryParam("topic") @DefaultValue("dragons and wizards") String topic)
      throws TimeoutException {
    String userMessage = "You are a creative writer. Generate a draft of a story no more than "
        + "3 sentences around the topic '" + topic + "'. Return only the story and nothing else.";

    ReActInput input = new ReActInput("creative-writer-agent", null, userMessage, null, 8);
    String instanceId = "durable-" + UUID.randomUUID();
    workflowClient.scheduleNewWorkflow("react-agent", input, instanceId);

    WorkflowInstanceStatus status =
        workflowClient.waitForInstanceCompletion(instanceId, Duration.ofSeconds(60), true);
    return status.readOutputAs(String.class);
  }

  /**
   * Starts a durable sequential composite (two react-agent children) and returns its result.
   *
   * <p>Runs {@code creative-writer-agent} then {@code style-editor-agent} as child workflows,
   * threading the {@code story} state between them — the control-inversion equivalent of a
   * {@code @SequenceAgent}, with no planner bridge.
   *
   * @param topic the story topic
   * @return the final (style-edited) story
   * @throws TimeoutException if the workflow does not complete within the wait window
   */
  @GET
  @Path("/sequence")
  @Produces(MediaType.TEXT_PLAIN)
  public String sequence(@QueryParam("topic") @DefaultValue("dragons and wizards") String topic)
      throws TimeoutException {
    DurableSequenceInput input = new DurableSequenceInput(
        List.of(
            new SubAgentSpec("creative-writer-agent",
                "You are a creative writer. Write a 3-sentence story about {{topic}}. "
                    + "Return only the story.", "story"),
            new SubAgentSpec("style-editor-agent",
                "You are a style editor. Improve the style of this story: {{story}}. "
                    + "Return only the improved story.", "story")),
        Map.of("topic", topic),
        "story");

    String instanceId = "durable-sequence-" + UUID.randomUUID();
    workflowClient.scheduleNewWorkflow("durable-sequence", input, instanceId);

    WorkflowInstanceStatus status =
        workflowClient.waitForInstanceCompletion(instanceId, Duration.ofSeconds(60), true);
    return status.readOutputAs(String.class);
  }

  /**
   * Starts a durable parallel composite (two react-agent children run concurrently).
   *
   * @param topic the story topic
   * @return the combined outputs
   * @throws TimeoutException if the workflow does not complete within the wait window
   */
  @GET
  @Path("/parallel")
  @Produces(MediaType.TEXT_PLAIN)
  public String parallel(@QueryParam("topic") @DefaultValue("dragons and wizards") String topic)
      throws TimeoutException {
    DurableSequenceInput input = new DurableSequenceInput(
        List.of(
            new SubAgentSpec("creative-writer-agent",
                "Write a 3-sentence story about {{topic}}. Return only the story.", "story"),
            new SubAgentSpec("style-editor-agent",
                "Write a fancy 3-sentence story about {{topic}}. Return only the story.", "fancy")),
        Map.of("topic", topic),
        null);
    return runComposite("durable-parallel", "durable-parallel-", input);
  }

  /**
   * Starts a durable loop composite (one agent refined over N iterations).
   *
   * @param topic      the story topic
   * @param iterations how many refinement passes
   * @return the final refined story
   * @throws TimeoutException if the workflow does not complete within the wait window
   */
  @GET
  @Path("/loop")
  @Produces(MediaType.TEXT_PLAIN)
  public String loop(@QueryParam("topic") @DefaultValue("dragons and wizards") String topic,
      @QueryParam("iterations") @DefaultValue("2") int iterations) throws TimeoutException {
    DurableLoopInput input = new DurableLoopInput(
        List.of(new SubAgentSpec("creative-writer-agent",
            "Refine a 3-sentence story about {{topic}}. Current draft: {{story}}. "
                + "Return only the story.", "story")),
        Map.of("topic", topic),
        "story",
        iterations);
    return runComposite("durable-loop", "durable-loop-", input);
  }

  /**
   * Starts a durable conditional composite (branch on the {@code mode} state value).
   *
   * @param topic the story topic
   * @param mode  {@code create} selects the writer branch; anything else the editor branch
   * @return the chosen branch's output
   * @throws TimeoutException if the workflow does not complete within the wait window
   */
  @GET
  @Path("/conditional")
  @Produces(MediaType.TEXT_PLAIN)
  public String conditional(@QueryParam("topic") @DefaultValue("dragons and wizards") String topic,
      @QueryParam("mode") @DefaultValue("create") String mode) throws TimeoutException {
    DurableConditionalInput input = new DurableConditionalInput(
        List.of(
            new SubAgentSpec("creative-writer-agent",
                "Write a 3-sentence story about {{topic}}. Return only the story.", "story"),
            new SubAgentSpec("style-editor-agent",
                "Write a fancy 3-sentence story about {{topic}}. Return only the story.", "story")),
        Map.of("topic", topic, "mode", mode),
        "story",
        "mode",
        "create");
    return runComposite("durable-conditional", "durable-conditional-", input);
  }

  private String runComposite(String workflowName, String idPrefix, Object input)
      throws TimeoutException {
    String instanceId = idPrefix + UUID.randomUUID();
    workflowClient.scheduleNewWorkflow(workflowName, input, instanceId);
    WorkflowInstanceStatus status =
        workflowClient.waitForInstanceCompletion(instanceId, Duration.ofSeconds(60), true);
    return status.readOutputAs(String.class);
  }

  /**
   * Starts the durable ReAct workflow for a tool-using research agent and returns its result.
   *
   * <p>Exercises the {@code agent-tool} activity: the model requests a tool call, the tool runs
   * as a replica-agnostic activity, and its result is fed back into the loop.
   *
   * @param country the country to research
   * @return the research summary
   * @throws TimeoutException if the workflow does not complete within the wait window
   */
  @GET
  @Path("/research")
  @Produces(MediaType.TEXT_PLAIN)
  public String research(@QueryParam("country") @DefaultValue("France") String country)
      throws TimeoutException {
    String userMessage = "You are a research assistant. Write a concise summary about the country "
        + country + " using the available tools. Return only the summary.";

    ReActInput input = new ReActInput("research-location-agent", null, userMessage, null, 8);
    String instanceId = "durable-research-" + UUID.randomUUID();
    workflowClient.scheduleNewWorkflow("react-agent", input, instanceId);

    WorkflowInstanceStatus status =
        workflowClient.waitForInstanceCompletion(instanceId, Duration.ofSeconds(60), true);
    return status.readOutputAs(String.class);
  }
}
