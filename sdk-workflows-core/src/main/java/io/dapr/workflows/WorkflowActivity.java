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
 * Interface for defining Dapr Workflow activities.
 *
 * <p>Activities are the basic unit of work in a Dapr Workflow orchestration. Unlike workflows,
 * activities have no determinism restrictions and can perform I/O, network calls, etc.</p>
 *
 * <p>Activities guarantee at-least-once execution, so implementations should be idempotent
 * whenever possible.</p>
 */
public interface WorkflowActivity {

  /**
   * Executes the activity logic.
   *
   * @param ctx the activity context
   * @return a serializable result value
   */
  Object run(WorkflowActivityContext ctx);
}
