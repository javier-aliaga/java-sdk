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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowRetryPolicyTest {

  @Test
  @DisplayName("WorkflowRetryPolicy is a RetryPolicy — no conversion needed")
  void isARetryPolicy() {
    WorkflowRetryPolicy policy = new WorkflowRetryPolicy(3, Duration.ofSeconds(1));
    assertInstanceOf(RetryPolicy.class, policy);
  }

  @Test
  @DisplayName("Constructor sets required fields")
  void constructorSetsFields() {
    WorkflowRetryPolicy policy = new WorkflowRetryPolicy(5, Duration.ofSeconds(2));
    assertEquals(5, policy.getMaxNumberOfAttempts());
    assertEquals(Duration.ofSeconds(2), policy.getFirstRetryInterval());
    assertEquals(1.0, policy.getBackoffCoefficient());
    assertEquals(0.0, policy.getJitterFactor());
  }

  @Test
  @DisplayName("Builder creates policy with all fields")
  void builderCreatesFullPolicy() {
    WorkflowRetryPolicy policy = WorkflowRetryPolicy.newBuilder()
        .setMaxNumberOfAttempts(10)
        .setFirstRetryInterval(Duration.ofSeconds(1))
        .setBackoffCoefficient(2.0)
        .setMaxRetryInterval(Duration.ofSeconds(30))
        .setRetryTimeout(Duration.ofMinutes(5))
        .setJitterFactor(0.2)
        .build();

    assertNotNull(policy);
    assertEquals(10, policy.getMaxNumberOfAttempts());
    assertEquals(Duration.ofSeconds(1), policy.getFirstRetryInterval());
    assertEquals(2.0, policy.getBackoffCoefficient());
    assertEquals(Duration.ofSeconds(30), policy.getMaxRetryInterval());
    assertEquals(Duration.ofMinutes(5), policy.getRetryTimeout());
    assertEquals(0.2, policy.getJitterFactor());
  }

  @Test
  @DisplayName("All fields propagate through inheritance — the key invariant")
  void allFieldsPropagateViaInheritance() {
    WorkflowRetryPolicy policy = WorkflowRetryPolicy.newBuilder()
        .setMaxNumberOfAttempts(3)
        .setFirstRetryInterval(Duration.ofSeconds(1))
        .setBackoffCoefficient(2.0)
        .setMaxRetryInterval(Duration.ofSeconds(60))
        .setJitterFactor(0.5)
        .build();

    // Cast to RetryPolicy — this is what the engine receives
    RetryPolicy enginePolicy = policy;

    // ALL fields must be visible through the base class — no conversion needed
    assertEquals(3, enginePolicy.getMaxNumberOfAttempts());
    assertEquals(Duration.ofSeconds(1), enginePolicy.getFirstRetryInterval());
    assertEquals(2.0, enginePolicy.getBackoffCoefficient());
    assertEquals(Duration.ofSeconds(60), enginePolicy.getMaxRetryInterval());
    assertEquals(0.5, enginePolicy.getJitterFactor());
  }

  @Test
  @DisplayName("Builder validates maxNumberOfAttempts > 0")
  void builderValidatesMaxAttempts() {
    assertThrows(IllegalArgumentException.class, () ->
        WorkflowRetryPolicy.newBuilder()
            .setMaxNumberOfAttempts(0)
            .setFirstRetryInterval(Duration.ofSeconds(1))
            .build());
  }

  @Test
  @DisplayName("Builder validates firstRetryInterval not null or zero")
  void builderValidatesFirstRetryInterval() {
    assertThrows(IllegalArgumentException.class, () ->
        WorkflowRetryPolicy.newBuilder()
            .setMaxNumberOfAttempts(3)
            .setFirstRetryInterval(Duration.ZERO)
            .build());
  }

  @Test
  @DisplayName("Builder validates backoffCoefficient >= 1.0")
  void builderValidatesBackoffCoefficient() {
    assertThrows(IllegalArgumentException.class, () ->
        WorkflowRetryPolicy.newBuilder()
            .setMaxNumberOfAttempts(3)
            .setFirstRetryInterval(Duration.ofSeconds(1))
            .setBackoffCoefficient(0.5)
            .build());
  }

  @Test
  @DisplayName("Builder validates jitterFactor in [0.0, 1.0]")
  void builderValidatesJitterFactor() {
    assertThrows(IllegalArgumentException.class, () ->
        WorkflowRetryPolicy.newBuilder()
            .setMaxNumberOfAttempts(3)
            .setFirstRetryInterval(Duration.ofSeconds(1))
            .setJitterFactor(1.5)
            .build());

    assertThrows(IllegalArgumentException.class, () ->
        WorkflowRetryPolicy.newBuilder()
            .setMaxNumberOfAttempts(3)
            .setFirstRetryInterval(Duration.ofSeconds(1))
            .setJitterFactor(-0.1)
            .build());
  }

  @Test
  @DisplayName("Builder validates jitterFactor must be finite")
  void builderValidatesJitterFactorFinite() {
    assertThrows(IllegalArgumentException.class, () ->
        WorkflowRetryPolicy.newBuilder()
            .setMaxNumberOfAttempts(3)
            .setFirstRetryInterval(Duration.ofSeconds(1))
            .setJitterFactor(Double.NaN)
            .build());
  }

  @Test
  @DisplayName("Constructor validates maxNumberOfAttempts > 0")
  void constructorValidatesMaxAttempts() {
    assertThrows(IllegalArgumentException.class, () ->
        new WorkflowRetryPolicy(-1, Duration.ofSeconds(1)));
  }

  @Test
  @DisplayName("Constructor validates firstRetryInterval not null")
  void constructorValidatesFirstRetryIntervalNull() {
    assertThrows(IllegalArgumentException.class, () ->
        new WorkflowRetryPolicy(3, null));
  }

  @Test
  @DisplayName("Default jitter factor is 0.0")
  void defaultJitterFactorIsZero() {
    WorkflowRetryPolicy policy = new WorkflowRetryPolicy(3, Duration.ofSeconds(1));
    assertEquals(0.0, policy.getJitterFactor());
  }

  @Test
  @DisplayName("Default backoff coefficient is 1.0")
  void defaultBackoffCoefficientIsOne() {
    WorkflowRetryPolicy policy = new WorkflowRetryPolicy(3, Duration.ofSeconds(1));
    assertEquals(1.0, policy.getBackoffCoefficient());
  }
}
