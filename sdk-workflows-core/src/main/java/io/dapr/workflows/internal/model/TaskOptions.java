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

/**
 * Internal options for controlling task execution behavior.
 */
public final class TaskOptions {

  private final RetryPolicy retryPolicy;
  private final RetryHandler retryHandler;
  private final String appId;

  private TaskOptions(RetryPolicy retryPolicy, RetryHandler retryHandler, String appId) {
    this.retryPolicy = retryPolicy;
    this.retryHandler = retryHandler;
    this.appId = appId;
  }

  /**
   * Creates a new builder.
   *
   * @return a new builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a TaskOptions with default values.
   *
   * @return new default TaskOptions
   */
  public static TaskOptions create() {
    return new Builder().build();
  }

  /**
   * Creates a TaskOptions with the specified retry policy.
   *
   * @param retryPolicy the retry policy
   * @return new TaskOptions
   */
  public static TaskOptions withRetryPolicy(RetryPolicy retryPolicy) {
    return new Builder().retryPolicy(retryPolicy).build();
  }

  /**
   * Creates a TaskOptions with the specified retry handler.
   *
   * @param retryHandler the retry handler
   * @return new TaskOptions
   */
  public static TaskOptions withRetryHandler(RetryHandler retryHandler) {
    return new Builder().retryHandler(retryHandler).build();
  }

  /**
   * Creates a TaskOptions with the specified app ID.
   *
   * @param appId the app ID
   * @return new TaskOptions
   */
  public static TaskOptions withAppId(String appId) {
    return new Builder().appId(appId).build();
  }

  /**
   * Returns true if a retry policy is configured.
   *
   * @return true if retry policy is set
   */
  public boolean hasRetryPolicy() {
    return this.retryPolicy != null;
  }

  /**
   * Gets the configured retry policy.
   *
   * @return the retry policy or null
   */
  public RetryPolicy getRetryPolicy() {
    return this.retryPolicy;
  }

  /**
   * Returns true if a retry handler is configured.
   *
   * @return true if retry handler is set
   */
  public boolean hasRetryHandler() {
    return this.retryHandler != null;
  }

  /**
   * Gets the configured retry handler.
   *
   * @return the retry handler or null
   */
  public RetryHandler getRetryHandler() {
    return this.retryHandler;
  }

  /**
   * Gets the configured app ID.
   *
   * @return the app ID or null
   */
  public String getAppId() {
    return this.appId;
  }

  /**
   * Returns true if an app ID is configured.
   *
   * @return true if app ID is set
   */
  public boolean hasAppId() {
    return this.appId != null && !this.appId.isEmpty();
  }

  /**
   * Builder for {@code TaskOptions}.
   */
  public static final class Builder {

    private RetryPolicy retryPolicy;
    private RetryHandler retryHandler;
    private String appId;

    private Builder() {
    }

    /**
     * Sets the retry policy.
     *
     * @param retryPolicy the retry policy
     * @return this builder
     */
    public Builder retryPolicy(RetryPolicy retryPolicy) {
      this.retryPolicy = retryPolicy;
      return this;
    }

    /**
     * Sets the retry handler.
     *
     * @param retryHandler the retry handler
     * @return this builder
     */
    public Builder retryHandler(RetryHandler retryHandler) {
      this.retryHandler = retryHandler;
      return this;
    }

    /**
     * Sets the app ID for cross-app routing.
     *
     * @param appId the app ID
     * @return this builder
     */
    public Builder appId(String appId) {
      this.appId = appId;
      return this;
    }

    /**
     * Builds the TaskOptions.
     *
     * @return new TaskOptions
     */
    public TaskOptions build() {
      return new TaskOptions(this.retryPolicy, this.retryHandler, this.appId);
    }
  }
}
