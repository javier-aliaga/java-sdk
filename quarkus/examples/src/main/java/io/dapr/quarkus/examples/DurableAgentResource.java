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

import io.dapr.quarkus.langchain4j.durable.ReActInput;
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
}
