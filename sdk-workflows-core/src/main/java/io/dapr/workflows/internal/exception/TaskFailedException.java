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

package io.dapr.workflows.internal.exception;

import io.dapr.workflows.internal.model.FailureDetails;

/**
 * Exception thrown when a scheduled task (activity or sub-orchestration) fails.
 */
public final class TaskFailedException extends RuntimeException {

  private final String taskName;
  private final int taskId;
  private final FailureDetails failureDetails;

  /**
   * Creates a new instance.
   *
   * @param taskName the name of the failed task
   * @param taskId the ID of the failed task
   * @param failureDetails the failure details
   */
  public TaskFailedException(String taskName, int taskId, FailureDetails failureDetails) {
    super(String.format("Task '%s' (#%d) failed with an unhandled exception: %s",
        taskName, taskId, failureDetails.getErrorMessage()));
    this.taskName = taskName;
    this.taskId = taskId;
    this.failureDetails = failureDetails;
  }

  /**
   * Gets the name of the failed task.
   *
   * @return the task name
   */
  public String getTaskName() {
    return this.taskName;
  }

  /**
   * Gets the ID of the failed task.
   *
   * @return the task ID
   */
  public int getTaskId() {
    return this.taskId;
  }

  /**
   * Gets the failure details.
   *
   * @return the failure details
   */
  public FailureDetails getFailureDetails() {
    return this.failureDetails;
  }
}
