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

package io.dapr.workflows;

/**
 * Interface for defining Dapr Workflow orchestrations.
 *
 * <p>Implementations must be deterministic. See the documentation for a full list of
 * determinism constraints that apply to workflow code.</p>
 */
public interface Workflow {

  /**
   * Creates a workflow stub that defines the orchestration logic.
   *
   * @return the workflow stub
   */
  WorkflowStub create();

  /**
   * Executes the workflow logic by delegating to the stub.
   *
   * @param ctx the workflow context
   */
  default void run(WorkflowContext ctx) {
    create().run(ctx);
  }
}
