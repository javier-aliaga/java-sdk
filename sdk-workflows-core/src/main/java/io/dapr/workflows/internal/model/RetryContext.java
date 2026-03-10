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

package io.dapr.workflows.internal.model;

import io.dapr.workflows.internal.executor.TaskOrchestrationContext;

import java.time.Duration;

/**
 * Context data provided to {@link RetryHandler} implementations.
 */
public final class RetryContext {

  private final TaskOrchestrationContext orchestrationContext;
  private final int lastAttemptNumber;
  private final FailureDetails lastFailure;
  private final Duration totalRetryTime;

  /**
   * Creates a new retry context.
   *
   * @param orchestrationContext the orchestration context
   * @param lastAttemptNumber the last attempt number
   * @param lastFailure the last failure details
   * @param totalRetryTime the total time spent retrying
   */
  public RetryContext(
      TaskOrchestrationContext orchestrationContext,
      int lastAttemptNumber,
      FailureDetails lastFailure,
      Duration totalRetryTime) {
    this.orchestrationContext = orchestrationContext;
    this.lastAttemptNumber = lastAttemptNumber;
    this.lastFailure = lastFailure;
    this.totalRetryTime = totalRetryTime;
  }

  /**
   * Gets the orchestration context.
   *
   * @return the orchestration context
   */
  public TaskOrchestrationContext getOrchestrationContext() {
    return this.orchestrationContext;
  }

  /**
   * Gets the details of the previous task failure.
   *
   * @return the failure details
   */
  public FailureDetails getLastFailure() {
    return this.lastFailure;
  }

  /**
   * Gets the previous retry attempt number (starts at 1).
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
