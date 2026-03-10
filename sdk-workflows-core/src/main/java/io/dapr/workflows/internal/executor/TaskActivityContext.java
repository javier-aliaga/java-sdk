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

package io.dapr.workflows.internal.executor;

/**
 * Internal context for activity execution.
 */
public interface TaskActivityContext {

  /**
   * Gets the name of the current activity.
   *
   * @return the activity name
   */
  String getName();

  /**
   * Gets the deserialized activity input.
   *
   * @param targetType the target class
   * @param <T> the target type
   * @return the deserialized input
   */
  <T> T getInput(Class<T> targetType);

  /**
   * Gets the execution ID of the current activity.
   *
   * @return the execution ID
   */
  String getTaskExecutionId();

  /**
   * Gets the task ID of the current activity.
   *
   * @return the task ID
   */
  int getTaskId();

  /**
   * Gets the trace parent ID for distributed tracing.
   *
   * @return the trace parent
   */
  String getTraceParent();
}
