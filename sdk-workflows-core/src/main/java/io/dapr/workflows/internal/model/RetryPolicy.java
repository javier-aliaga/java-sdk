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

import javax.annotation.Nullable;
import java.time.Duration;

/**
 * Declarative retry policy for activity or sub-orchestration calls.
 *
 * <p>This is the internal engine representation. Users should use
 * {@link io.dapr.workflows.options.WorkflowRetryPolicy} which extends this class,
 * ensuring all fields propagate automatically without manual conversion.</p>
 */
public class RetryPolicy {

  private int maxNumberOfAttempts;
  private Duration firstRetryInterval;
  private double backoffCoefficient = 1.0;
  private Duration maxRetryInterval = Duration.ZERO;
  private Duration retryTimeout = Duration.ZERO;
  private double jitterFactor = 0.0;

  /**
   * Creates a new retry policy.
   *
   * @param maxNumberOfAttempts the maximum number of attempts; must be 1 or greater
   * @param firstRetryInterval the delay between first and second attempt
   */
  public RetryPolicy(int maxNumberOfAttempts, Duration firstRetryInterval) {
    this.setMaxNumberOfAttempts(maxNumberOfAttempts);
    this.setFirstRetryInterval(firstRetryInterval);
  }

  /**
   * Sets the maximum number of attempts.
   *
   * @param maxNumberOfAttempts must be 1 or greater
   * @return this policy
   */
  public RetryPolicy setMaxNumberOfAttempts(int maxNumberOfAttempts) {
    if (maxNumberOfAttempts <= 0) {
      throw new IllegalArgumentException("The value for maxNumberOfAttempts must be greater than zero.");
    }
    this.maxNumberOfAttempts = maxNumberOfAttempts;
    return this;
  }

  /**
   * Sets the delay between the first and second attempt.
   *
   * @param firstRetryInterval the delay; must not be null, zero, or negative
   * @return this policy
   */
  public RetryPolicy setFirstRetryInterval(Duration firstRetryInterval) {
    if (firstRetryInterval == null) {
      throw new IllegalArgumentException("firstRetryInterval cannot be null.");
    }
    if (firstRetryInterval.isZero() || firstRetryInterval.isNegative()) {
      throw new IllegalArgumentException("The value for firstRetryInterval must be greater than zero.");
    }
    this.firstRetryInterval = firstRetryInterval;
    return this;
  }

  /**
   * Sets the exponential backoff coefficient. Must be 1.0 or greater.
   *
   * @param backoffCoefficient the backoff coefficient
   * @return this policy
   */
  public RetryPolicy setBackoffCoefficient(double backoffCoefficient) {
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
   * @return this policy
   */
  public RetryPolicy setMaxRetryInterval(@Nullable Duration maxRetryInterval) {
    if (maxRetryInterval != null && maxRetryInterval.compareTo(this.firstRetryInterval) < 0) {
      throw new IllegalArgumentException(
          "The value for maxRetryInterval must be greater than or equal to the value for firstRetryInterval.");
    }
    this.maxRetryInterval = maxRetryInterval;
    return this;
  }

  /**
   * Sets the overall timeout for retries.
   *
   * @param retryTimeout the timeout; must not be null and must be >= firstRetryInterval
   * @return this policy
   */
  public RetryPolicy setRetryTimeout(Duration retryTimeout) {
    if (retryTimeout == null || retryTimeout.compareTo(this.firstRetryInterval) < 0) {
      throw new IllegalArgumentException(
          "The value for retryTimeout cannot be null and must be greater than or equal "
              + "to the value for firstRetryInterval.");
    }
    this.retryTimeout = retryTimeout;
    return this;
  }

  /**
   * Sets the jitter factor applied to computed retry delays.
   *
   * <p>A value between 0.0 (no jitter) and 1.0 (up to 100% reduction). Uses a deterministic
   * seed derived from the first-attempt timestamp and attempt number to ensure replay safety.</p>
   *
   * @param jitterFactor must be between 0.0 and 1.0 inclusive
   * @return this policy
   */
  public RetryPolicy setJitterFactor(double jitterFactor) {
    if (!Double.isFinite(jitterFactor) || jitterFactor < 0.0 || jitterFactor > 1.0) {
      throw new IllegalArgumentException(
          "The value for jitterFactor must be between 0.0 and 1.0 inclusive.");
    }
    this.jitterFactor = jitterFactor;
    return this;
  }

  /**
   * Gets the maximum number of attempts.
   *
   * @return the maximum number of attempts
   */
  public int getMaxNumberOfAttempts() {
    return this.maxNumberOfAttempts;
  }

  /**
   * Gets the delay between the first and second attempt.
   *
   * @return the first retry interval
   */
  public Duration getFirstRetryInterval() {
    return this.firstRetryInterval;
  }

  /**
   * Gets the exponential backoff coefficient.
   *
   * @return the backoff coefficient
   */
  public double getBackoffCoefficient() {
    return this.backoffCoefficient;
  }

  /**
   * Gets the maximum delay between attempts.
   *
   * @return the max retry interval
   */
  public Duration getMaxRetryInterval() {
    return this.maxRetryInterval;
  }

  /**
   * Gets the overall timeout for retries.
   *
   * @return the retry timeout
   */
  public Duration getRetryTimeout() {
    return this.retryTimeout;
  }

  /**
   * Gets the jitter factor.
   *
   * @return the jitter factor
   */
  public double getJitterFactor() {
    return this.jitterFactor;
  }
}
