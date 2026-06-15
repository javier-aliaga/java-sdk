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
import java.util.Map;

/**
 * Input to {@link DurableConditionalWorkflow}.
 *
 * <p>Branch selection is data-driven: if {@code initialState[conditionKey]} equals
 * {@code expectedValue}, the first sub-agent runs; otherwise the second (if present).
 *
 * @param subAgents      {@code [0]} = match branch, {@code [1]} = else branch (optional)
 * @param initialState   seed state for the condition and template rendering
 * @param finalOutputKey state key to return; {@code null} returns the chosen agent's output
 * @param conditionKey   state key to test
 * @param expectedValue  value that selects the match branch
 */
public record DurableConditionalInput(
    List<SubAgentSpec> subAgents,
    Map<String, String> initialState,
    String finalOutputKey,
    String conditionKey,
    String expectedValue) {
}
