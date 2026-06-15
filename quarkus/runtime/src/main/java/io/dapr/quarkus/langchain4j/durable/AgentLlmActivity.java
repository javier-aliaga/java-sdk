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

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import io.quarkiverse.dapr.workflows.ActivityMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Stateless activity that performs one model call for {@link ReActAgentWorkflow}.
 *
 * <p>It is a pure function of its {@link LlmInput} (conversation + agent name): deserialize
 * the messages, resolve the agent's tool specifications, call the model, and return the full
 * assistant message serialized as JSON. No in-memory run context, so it can run on any
 * replica Dapr schedules it on.
 *
 * <p><b>Note (control-inversion cutover):</b> this injects the real {@link ChatModel}. On this
 * branch the legacy {@code DaprChatModelDecorator} still decorates {@code ChatModel} and would
 * re-route the call; the cutover deletes that decorator. Until then, disable it (or inject the
 * undecorated provider) when exercising this path.
 */
@ApplicationScoped
@ActivityMetadata(name = "agent-llm")
public class AgentLlmActivity implements WorkflowActivity {

  private static final Logger LOG = Logger.getLogger(AgentLlmActivity.class);

  @Inject
  ChatModel chatModel;

  @Inject
  AgentToolSpecRegistry toolSpecRegistry;

  @Override
  public Object run(WorkflowActivityContext ctx) {
    LlmInput input = ctx.getInput(LlmInput.class);
    List<ChatMessage> messages = ChatMessageDeserializer.messagesFromJson(input.messagesJson());
    List<ToolSpecification> tools = toolSpecRegistry.specsFor(input.agentName());

    LOG.debugf("[agent-llm:%s] %d messages, %d tools", input.agentName(), messages.size(), tools.size());

    ChatRequest.Builder request = ChatRequest.builder().messages(messages);
    if (!tools.isEmpty()) {
      request.toolSpecifications(tools);
    }
    ChatResponse response = chatModel.chat(request.build());
    return new LlmResult(ChatMessageSerializer.messageToJson(response.aiMessage()));
  }
}
