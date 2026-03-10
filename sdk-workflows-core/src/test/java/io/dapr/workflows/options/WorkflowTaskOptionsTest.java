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

import io.dapr.workflows.internal.model.TaskOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class WorkflowTaskOptionsTest {

  @Test
  @DisplayName("Constructor with retry policy only")
  void constructorWithRetryPolicy() {
    WorkflowRetryPolicy policy = new WorkflowRetryPolicy(3, Duration.ofSeconds(1));
    WorkflowTaskOptions options = new WorkflowTaskOptions(policy);

    assertEquals(policy, options.getRetryPolicy());
    assertNull(options.getRetryHandler());
    assertNull(options.getAppId());
  }

  @Test
  @DisplayName("Constructor with retry policy and app ID")
  void constructorWithRetryPolicyAndAppId() {
    WorkflowRetryPolicy policy = new WorkflowRetryPolicy(3, Duration.ofSeconds(1));
    WorkflowTaskOptions options = new WorkflowTaskOptions(policy, "remote-app");

    assertEquals(policy, options.getRetryPolicy());
    assertNull(options.getRetryHandler());
    assertEquals("remote-app", options.getAppId());
  }

  @Test
  @DisplayName("Constructor with retry handler only")
  void constructorWithRetryHandler() {
    WorkflowRetryHandler handler = ctx -> ctx.getLastAttemptNumber() < 5;
    WorkflowTaskOptions options = new WorkflowTaskOptions(handler);

    assertNull(options.getRetryPolicy());
    assertEquals(handler, options.getRetryHandler());
    assertNull(options.getAppId());
  }

  @Test
  @DisplayName("Full constructor with all fields")
  void fullConstructor() {
    WorkflowRetryPolicy policy = new WorkflowRetryPolicy(3, Duration.ofSeconds(1));
    WorkflowRetryHandler handler = ctx -> true;
    WorkflowTaskOptions options = new WorkflowTaskOptions(policy, handler, "my-app");

    assertEquals(policy, options.getRetryPolicy());
    assertEquals(handler, options.getRetryHandler());
    assertEquals("my-app", options.getAppId());
  }

  @Test
  @DisplayName("toTaskOptions passes WorkflowRetryPolicy directly as RetryPolicy")
  void toTaskOptionsPassesPolicyDirectly() {
    WorkflowRetryPolicy policy = WorkflowRetryPolicy.newBuilder()
        .setMaxNumberOfAttempts(5)
        .setFirstRetryInterval(Duration.ofSeconds(2))
        .setJitterFactor(0.3)
        .build();
    WorkflowTaskOptions options = new WorkflowTaskOptions(policy, "target-app");

    TaskOptions internal = options.toTaskOptions(null);

    assertNotNull(internal);
    assertNotNull(internal.getRetryPolicy());
    // The policy object passes through without conversion
    assertEquals(5, internal.getRetryPolicy().getMaxNumberOfAttempts());
    assertEquals(Duration.ofSeconds(2), internal.getRetryPolicy().getFirstRetryInterval());
    assertEquals(0.3, internal.getRetryPolicy().getJitterFactor());
    assertEquals("target-app", internal.getAppId());
  }

  @Test
  @DisplayName("toTaskOptions with null handler produces null internal handler")
  void toTaskOptionsNullHandler() {
    WorkflowRetryPolicy policy = new WorkflowRetryPolicy(3, Duration.ofSeconds(1));
    WorkflowTaskOptions options = new WorkflowTaskOptions(policy);

    TaskOptions internal = options.toTaskOptions(null);

    assertNotNull(internal);
    assertNull(internal.getRetryHandler());
  }
}
