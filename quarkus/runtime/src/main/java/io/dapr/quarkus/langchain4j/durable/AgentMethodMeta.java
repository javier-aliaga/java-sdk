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

import java.util.List;

/**
 * Build-time-extracted description of one agent method, used by
 * {@link DurableAgentInvocationHandler} to start the right durable workflow.
 *
 * <p>Recorder-serializable (record + Strings/ints/Lists), so it can be passed through a
 * {@code @Recorder} into the synthetic bean that replaces the AiServices-built agent.
 *
 * @param workflowName  target workflow: {@code react-agent} (leaf) or {@code durable-sequence}
 *                      / {@code durable-parallel} / {@code durable-loop} (composite)
 * @param agentName     agent name (leaf) or composite name; also used to name the run
 * @param userTemplate  leaf {@code @UserMessage} template, or {@code null}
 * @param systemTemplate leaf {@code @SystemMessage} template, or {@code null}
 * @param varNames      method parameter names in order (the {@code @V} names), for
 *                      {@code {{var}}} substitution / initial state
 * @param subAgents     composite sub-agent steps (empty for a leaf)
 * @param outputKey     composite output key to return (or {@code null})
 * @param maxIterations loop iteration count (0 if not a loop)
 */
public record AgentMethodMeta(
    String workflowName,
    String agentName,
    String userTemplate,
    String systemTemplate,
    List<String> varNames,
    List<SubAgentSpec> subAgents,
    String outputKey,
    int maxIterations) {
}
