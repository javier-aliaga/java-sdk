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

/**
 * One step of a durable composite: which agent to run, the {@code {{var}}} template to render
 * its user message from the shared state, and the state key its output is written to.
 *
 * @param agentName           the agent to run as a {@code react-agent} child workflow
 * @param userMessageTemplate the user-message template; {@code {{key}}} placeholders are
 *                            substituted from the composite's accumulated state
 * @param outputKey           the state key under which this step's output is stored
 */
public record SubAgentSpec(String agentName, String userMessageTemplate, String outputKey) {
}
