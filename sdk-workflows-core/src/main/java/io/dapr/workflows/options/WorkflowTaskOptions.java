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

import io.dapr.workflows.internal.model.RetryHandler;
import io.dapr.workflows.internal.model.TaskOptions;

import javax.annotation.Nullable;

/**
 * Options for workflow activity and child workflow calls.
 *
 * <p>Combines retry policy, retry handler, and cross-app routing configuration.</p>
 */
public final class WorkflowTaskOptions {

  private final WorkflowRetryPolicy retryPolicy;
  private final WorkflowRetryHandler retryHandler;
  private final String appId;

  /**
   * Creates options with a retry policy and retry handler.
   *
   * @param retryPolicy the retry policy
   * @param retryHandler the retry handler
   */
  public WorkflowTaskOptions(
      @Nullable WorkflowRetryPolicy retryPolicy,
      @Nullable WorkflowRetryHandler retryHandler) {
    this(retryPolicy, retryHandler, null);
  }

  /**
   * Creates options with a retry policy.
   *
   * @param retryPolicy the retry policy
   */
  public WorkflowTaskOptions(@Nullable WorkflowRetryPolicy retryPolicy) {
    this(retryPolicy, null, null);
  }

  /**
   * Creates options with a retry handler.
   *
   * @param retryHandler the retry handler
   */
  public WorkflowTaskOptions(@Nullable WorkflowRetryHandler retryHandler) {
    this(null, retryHandler, null);
  }

  /**
   * Creates options with a retry policy and app ID for cross-app calls.
   *
   * @param retryPolicy the retry policy
   * @param appId the target app ID
   */
  public WorkflowTaskOptions(@Nullable WorkflowRetryPolicy retryPolicy, String appId) {
    this(retryPolicy, null, appId);
  }

  /**
   * Creates full options.
   *
   * @param retryPolicy the retry policy
   * @param retryHandler the retry handler
   * @param appId the target app ID
   */
  public WorkflowTaskOptions(
      @Nullable WorkflowRetryPolicy retryPolicy,
      @Nullable WorkflowRetryHandler retryHandler,
      @Nullable String appId) {
    this.retryPolicy = retryPolicy;
    this.retryHandler = retryHandler;
    this.appId = appId;
  }

  /**
   * Gets the retry policy.
   *
   * @return the retry policy or null
   */
  @Nullable
  public WorkflowRetryPolicy getRetryPolicy() {
    return this.retryPolicy;
  }

  /**
   * Gets the retry handler.
   *
   * @return the retry handler or null
   */
  @Nullable
  public WorkflowRetryHandler getRetryHandler() {
    return this.retryHandler;
  }

  /**
   * Gets the target app ID for cross-app calls.
   *
   * @return the app ID or null
   */
  @Nullable
  public String getAppId() {
    return this.appId;
  }

  /**
   * Converts to the internal engine TaskOptions.
   *
   * <p>Since {@link WorkflowRetryPolicy} extends {@link io.dapr.workflows.internal.model.RetryPolicy},
   * the retry policy is passed directly — no field-by-field conversion needed.</p>
   *
   * @param workflowContext the workflow context (for retry handler adaptation)
   * @return the internal task options, or null if no options are configured
   */
  public TaskOptions toTaskOptions(
      @Nullable io.dapr.workflows.WorkflowContext workflowContext) {
    RetryHandler internalHandler = toInternalRetryHandler(workflowContext);

    return TaskOptions.builder()
        .retryPolicy(this.retryPolicy)  // direct pass — IS-A RetryPolicy
        .retryHandler(internalHandler)
        .appId(this.appId)
        .build();
  }

  private RetryHandler toInternalRetryHandler(
      @Nullable io.dapr.workflows.WorkflowContext workflowContext) {
    if (this.retryHandler == null) {
      return null;
    }
    return retryContext -> {
      io.dapr.workflows.client.WorkflowFailureDetails failureDetails =
          new io.dapr.workflows.client.WorkflowFailureDetails(retryContext.getLastFailure());
      WorkflowRetryContext workflowRetryContext = new WorkflowRetryContext(
          workflowContext,
          retryContext.getLastAttemptNumber(),
          failureDetails,
          retryContext.getTotalRetryTime());
      return this.retryHandler.handle(workflowRetryContext);
    };
  }
}
