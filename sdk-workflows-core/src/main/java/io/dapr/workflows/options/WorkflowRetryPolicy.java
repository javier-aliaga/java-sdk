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

import io.dapr.workflows.internal.model.RetryPolicy;

import javax.annotation.Nullable;
import java.time.Duration;

/**
 * Retry policy for workflow activity and sub-workflow calls.
 *
 * <p>This class extends the internal {@link RetryPolicy} directly, which means any new field
 * added to the engine's retry policy is automatically available here — no manual conversion
 * or field-by-field copying is needed. This eliminates the propagation errors that occurred
 * when separate types required a {@code toRetryPolicy()} conversion method.</p>
 *
 * <p>Use the {@link Builder} for a fluent construction API:</p>
 * <pre>{@code
 * WorkflowRetryPolicy policy = WorkflowRetryPolicy.newBuilder()
 *     .setMaxNumberOfAttempts(5)
 *     .setFirstRetryInterval(Duration.ofSeconds(1))
 *     .setBackoffCoefficient(2.0)
 *     .setMaxRetryInterval(Duration.ofSeconds(30))
 *     .setJitterFactor(0.2)
 *     .build();
 * }</pre>
 */
public final class WorkflowRetryPolicy extends RetryPolicy {

  /**
   * Creates a new workflow retry policy.
   *
   * @param maxNumberOfAttempts the maximum number of attempts; must be 1 or greater
   * @param firstRetryInterval the delay between first and second attempt
   */
  public WorkflowRetryPolicy(int maxNumberOfAttempts, Duration firstRetryInterval) {
    super(maxNumberOfAttempts, firstRetryInterval);
  }

  /**
   * Creates a new builder.
   *
   * @return a new builder instance
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Builder for {@link WorkflowRetryPolicy}.
   */
  public static final class Builder {

    private int maxNumberOfAttempts;
    private Duration firstRetryInterval;
    private double backoffCoefficient = 1.0;
    private Duration maxRetryInterval;
    private Duration retryTimeout;
    private double jitterFactor = 0.0;

    private Builder() {
    }

    /**
     * Sets the maximum number of attempts.
     *
     * @param maxNumberOfAttempts must be 1 or greater
     * @return this builder
     */
    public Builder setMaxNumberOfAttempts(int maxNumberOfAttempts) {
      if (maxNumberOfAttempts <= 0) {
        throw new IllegalArgumentException(
            "The value for maxNumberOfAttempts must be greater than zero.");
      }
      this.maxNumberOfAttempts = maxNumberOfAttempts;
      return this;
    }

    /**
     * Sets the delay between the first and second attempt.
     *
     * @param firstRetryInterval the delay; must not be null, zero, or negative
     * @return this builder
     */
    public Builder setFirstRetryInterval(Duration firstRetryInterval) {
      if (firstRetryInterval == null || firstRetryInterval.isZero() || firstRetryInterval.isNegative()) {
        throw new IllegalArgumentException(
            "The value for firstRetryInterval must be greater than zero.");
      }
      this.firstRetryInterval = firstRetryInterval;
      return this;
    }

    /**
     * Sets the exponential backoff coefficient. Must be 1.0 or greater.
     *
     * @param backoffCoefficient the coefficient
     * @return this builder
     */
    public Builder setBackoffCoefficient(double backoffCoefficient) {
      if (backoffCoefficient < 1.0) {
        throw new IllegalArgumentException(
            "The value for backoffCoefficient must be greater or equal to 1.0.");
      }
      this.backoffCoefficient = backoffCoefficient;
      return this;
    }

    /**
     * Sets the maximum delay between attempts.
     *
     * @param maxRetryInterval the max interval or null to remove
     * @return this builder
     */
    public Builder setMaxRetryInterval(@Nullable Duration maxRetryInterval) {
      this.maxRetryInterval = maxRetryInterval;
      return this;
    }

    /**
     * Sets the overall timeout for retries.
     *
     * @param retryTimeout the timeout
     * @return this builder
     */
    public Builder setRetryTimeout(Duration retryTimeout) {
      this.retryTimeout = retryTimeout;
      return this;
    }

    /**
     * Sets the jitter factor (0.0 to 1.0).
     *
     * <p>Uses a deterministic seed for replay safety. See {@link RetryPolicy#setJitterFactor}.</p>
     *
     * @param jitterFactor must be between 0.0 and 1.0 inclusive
     * @return this builder
     */
    public Builder setJitterFactor(double jitterFactor) {
      if (!Double.isFinite(jitterFactor) || jitterFactor < 0.0 || jitterFactor > 1.0) {
        throw new IllegalArgumentException(
            "The value for jitterFactor must be between 0.0 and 1.0 inclusive.");
      }
      this.jitterFactor = jitterFactor;
      return this;
    }

    /**
     * Builds the retry policy.
     *
     * @return a new WorkflowRetryPolicy
     */
    public WorkflowRetryPolicy build() {
      WorkflowRetryPolicy policy = new WorkflowRetryPolicy(
          this.maxNumberOfAttempts, this.firstRetryInterval);
      policy.setBackoffCoefficient(this.backoffCoefficient);
      if (this.maxRetryInterval != null) {
        policy.setMaxRetryInterval(this.maxRetryInterval);
      }
      if (this.retryTimeout != null) {
        policy.setRetryTimeout(this.retryTimeout);
      }
      policy.setJitterFactor(this.jitterFactor);
      return policy;
    }
  }
}
