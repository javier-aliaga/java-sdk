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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryPolicyTest {

  @Test
  @DisplayName("Constructor sets required fields with defaults")
  void constructorSetsDefaults() {
    RetryPolicy policy = new RetryPolicy(3, Duration.ofSeconds(1));
    assertEquals(3, policy.getMaxNumberOfAttempts());
    assertEquals(Duration.ofSeconds(1), policy.getFirstRetryInterval());
    assertEquals(1.0, policy.getBackoffCoefficient());
    assertEquals(Duration.ZERO, policy.getMaxRetryInterval());
    assertEquals(Duration.ZERO, policy.getRetryTimeout());
    assertEquals(0.0, policy.getJitterFactor());
  }

  @Test
  @DisplayName("Rejects zero maxNumberOfAttempts")
  void rejectsZeroAttempts() {
    assertThrows(IllegalArgumentException.class,
        () -> new RetryPolicy(0, Duration.ofSeconds(1)));
  }

  @Test
  @DisplayName("Rejects negative maxNumberOfAttempts")
  void rejectsNegativeAttempts() {
    assertThrows(IllegalArgumentException.class,
        () -> new RetryPolicy(-1, Duration.ofSeconds(1)));
  }

  @Test
  @DisplayName("Rejects null firstRetryInterval")
  void rejectsNullInterval() {
    assertThrows(IllegalArgumentException.class,
        () -> new RetryPolicy(3, null));
  }

  @Test
  @DisplayName("Rejects zero firstRetryInterval")
  void rejectsZeroInterval() {
    assertThrows(IllegalArgumentException.class,
        () -> new RetryPolicy(3, Duration.ZERO));
  }

  @Test
  @DisplayName("Rejects backoffCoefficient less than 1.0")
  void rejectsLowBackoff() {
    RetryPolicy policy = new RetryPolicy(3, Duration.ofSeconds(1));
    assertThrows(IllegalArgumentException.class,
        () -> policy.setBackoffCoefficient(0.5));
  }

  @Test
  @DisplayName("Rejects maxRetryInterval less than firstRetryInterval")
  void rejectsMaxRetryIntervalTooSmall() {
    RetryPolicy policy = new RetryPolicy(3, Duration.ofSeconds(5));
    assertThrows(IllegalArgumentException.class,
        () -> policy.setMaxRetryInterval(Duration.ofSeconds(1)));
  }

  @Test
  @DisplayName("Accepts null maxRetryInterval")
  void acceptsNullMaxRetryInterval() {
    RetryPolicy policy = new RetryPolicy(3, Duration.ofSeconds(1));
    policy.setMaxRetryInterval(null);
    // No exception — null means "no max"
  }

  @Test
  @DisplayName("Rejects jitterFactor outside [0.0, 1.0]")
  void rejectsInvalidJitter() {
    RetryPolicy policy = new RetryPolicy(3, Duration.ofSeconds(1));
    assertThrows(IllegalArgumentException.class, () -> policy.setJitterFactor(-0.1));
    assertThrows(IllegalArgumentException.class, () -> policy.setJitterFactor(1.1));
    assertThrows(IllegalArgumentException.class, () -> policy.setJitterFactor(Double.NaN));
    assertThrows(IllegalArgumentException.class, () -> policy.setJitterFactor(Double.POSITIVE_INFINITY));
  }

  @Test
  @DisplayName("Setters return this for chaining")
  void settersReturnThis() {
    RetryPolicy policy = new RetryPolicy(1, Duration.ofSeconds(1));
    RetryPolicy result = policy
        .setMaxNumberOfAttempts(5)
        .setFirstRetryInterval(Duration.ofSeconds(2))
        .setBackoffCoefficient(2.0)
        .setMaxRetryInterval(Duration.ofSeconds(30))
        .setRetryTimeout(Duration.ofMinutes(5))
        .setJitterFactor(0.5);
    assertEquals(policy, result);
  }

  @Test
  @DisplayName("All fields are readable after setting")
  void allFieldsReadable() {
    RetryPolicy policy = new RetryPolicy(5, Duration.ofSeconds(2))
        .setBackoffCoefficient(3.0)
        .setMaxRetryInterval(Duration.ofMinutes(1))
        .setRetryTimeout(Duration.ofMinutes(10))
        .setJitterFactor(0.8);

    assertEquals(5, policy.getMaxNumberOfAttempts());
    assertEquals(Duration.ofSeconds(2), policy.getFirstRetryInterval());
    assertEquals(3.0, policy.getBackoffCoefficient());
    assertEquals(Duration.ofMinutes(1), policy.getMaxRetryInterval());
    assertEquals(Duration.ofMinutes(10), policy.getRetryTimeout());
    assertEquals(0.8, policy.getJitterFactor());
  }
}
