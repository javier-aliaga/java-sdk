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

package io.dapr.workflows.options;

import io.dapr.workflows.WorkflowContext;
import io.dapr.workflows.client.WorkflowFailureDetails;

import java.time.Duration;

/**
 * Context provided to {@link WorkflowRetryHandler} implementations.
 */
public final class WorkflowRetryContext {

  private final WorkflowContext workflowContext;
  private final int lastAttemptNumber;
  private final WorkflowFailureDetails lastFailure;
  private final Duration totalRetryTime;

  /**
   * Creates a new retry context.
   *
   * @param workflowContext the workflow context
   * @param lastAttemptNumber the previous attempt number (starts at 1)
   * @param lastFailure the most recent failure details
   * @param totalRetryTime the total time spent retrying
   */
  public WorkflowRetryContext(
      WorkflowContext workflowContext,
      int lastAttemptNumber,
      WorkflowFailureDetails lastFailure,
      Duration totalRetryTime) {
    this.workflowContext = workflowContext;
    this.lastAttemptNumber = lastAttemptNumber;
    this.lastFailure = lastFailure;
    this.totalRetryTime = totalRetryTime;
  }

  /**
   * Gets the workflow context for scheduling timers or getting the current time.
   *
   * @return the workflow context
   */
  public WorkflowContext getWorkflowContext() {
    return this.workflowContext;
  }

  /**
   * Gets the previous failure details.
   *
   * @return the failure details
   */
  public WorkflowFailureDetails getLastFailure() {
    return this.lastFailure;
  }

  /**
   * Gets the previous attempt number (starts at 1).
   *
   * @return the attempt number
   */
  public int getLastAttemptNumber() {
    return this.lastAttemptNumber;
  }

  /**
   * Gets the total time spent in the retry loop.
   *
   * @return the total retry time
   */
  public Duration getTotalRetryTime() {
    return this.totalRetryTime;
  }
}
