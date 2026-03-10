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

package io.dapr.workflows;

import io.dapr.workflows.internal.exception.TaskCanceledException;
import io.dapr.workflows.internal.executor.TaskOrchestrationContext;
import io.dapr.workflows.internal.model.Task;
import io.dapr.workflows.internal.model.TaskOptions;
import io.dapr.workflows.options.WorkflowRetryPolicy;
import io.dapr.workflows.options.WorkflowTaskOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowContextTest {

  private TaskOrchestrationContext mockInnerContext;
  private Logger mockLogger;
  private WorkflowContext context;

  @BeforeEach
  void setUp() {
    mockInnerContext = mock(TaskOrchestrationContext.class);
    mockLogger = mock(Logger.class);
    context = new WorkflowContext(mockInnerContext, mockLogger);
  }

  @Test
  @DisplayName("Constructor throws on null inner context")
  void constructorThrowsOnNullContext() {
    assertThrows(IllegalArgumentException.class,
        () -> new WorkflowContext(null, mockLogger));
  }

  @Test
  @DisplayName("Constructor throws on null logger")
  void constructorThrowsOnNullLogger() {
    assertThrows(IllegalArgumentException.class,
        () -> new WorkflowContext(mockInnerContext, (Logger) null));
  }

  @Test
  @DisplayName("getLogger returns NOP logger during replay")
  void getLoggerReturnsNopDuringReplay() {
    when(mockInnerContext.getIsReplaying()).thenReturn(true);
    assertEquals(NOPLogger.NOP_LOGGER, context.getLogger());
  }

  @Test
  @DisplayName("getLogger returns real logger when not replaying")
  void getLoggerReturnsRealWhenNotReplaying() {
    when(mockInnerContext.getIsReplaying()).thenReturn(false);
    assertEquals(mockLogger, context.getLogger());
  }

  @Test
  @DisplayName("getName delegates to inner context")
  void getNameDelegates() {
    when(mockInnerContext.getName()).thenReturn("TestWorkflow");
    assertEquals("TestWorkflow", context.getName());
  }

  @Test
  @DisplayName("getInstanceId delegates to inner context")
  void getInstanceIdDelegates() {
    when(mockInnerContext.getInstanceId()).thenReturn("instance-123");
    assertEquals("instance-123", context.getInstanceId());
  }

  @Test
  @DisplayName("getCurrentInstant delegates to inner context")
  void getCurrentInstantDelegates() {
    Instant now = Instant.now();
    when(mockInnerContext.getCurrentInstant()).thenReturn(now);
    assertEquals(now, context.getCurrentInstant());
  }

  @Test
  @DisplayName("isReplaying delegates to inner context")
  void isReplayingDelegates() {
    when(mockInnerContext.getIsReplaying()).thenReturn(true);
    assertEquals(true, context.isReplaying());
  }

  @Test
  @DisplayName("complete delegates to inner context")
  void completeDelegates() {
    context.complete("result");
    verify(mockInnerContext).complete("result");
  }

  @Test
  @DisplayName("callActivity passes WorkflowRetryPolicy directly as RetryPolicy — no conversion")
  void callActivityPassesRetryPolicyDirectly() {
    WorkflowRetryPolicy retryPolicy = WorkflowRetryPolicy.newBuilder()
        .setMaxNumberOfAttempts(5)
        .setFirstRetryInterval(Duration.ofSeconds(1))
        .setBackoffCoefficient(2.0)
        .setMaxRetryInterval(Duration.ofSeconds(30))
        .setJitterFactor(0.3)
        .build();
    WorkflowTaskOptions options = new WorkflowTaskOptions(retryPolicy);

    context.callActivity("MyActivity", "input", options, String.class);

    ArgumentCaptor<TaskOptions> captor = ArgumentCaptor.forClass(TaskOptions.class);
    verify(mockInnerContext).callActivity(eq("MyActivity"), eq("input"), captor.capture(), eq(String.class));

    TaskOptions captured = captor.getValue();
    assertNotNull(captured);
    assertNotNull(captured.getRetryPolicy());

    // The retry policy IS the same object — no conversion happened
    assertEquals(5, captured.getRetryPolicy().getMaxNumberOfAttempts());
    assertEquals(Duration.ofSeconds(1), captured.getRetryPolicy().getFirstRetryInterval());
    assertEquals(2.0, captured.getRetryPolicy().getBackoffCoefficient());
    assertEquals(Duration.ofSeconds(30), captured.getRetryPolicy().getMaxRetryInterval());
    assertEquals(0.3, captured.getRetryPolicy().getJitterFactor());
  }

  @Test
  @DisplayName("callActivity with null options passes null to inner context")
  void callActivityNullOptions() {
    context.callActivity("MyActivity", "input", null, Void.class);
    verify(mockInnerContext).callActivity("MyActivity", "input", null, Void.class);
  }

  @Test
  @DisplayName("callChildWorkflow passes options with app ID")
  void callChildWorkflowPassesAppId() {
    WorkflowRetryPolicy retryPolicy = new WorkflowRetryPolicy(3, Duration.ofSeconds(1));
    WorkflowTaskOptions options = new WorkflowTaskOptions(retryPolicy, "remote-app");

    context.callChildWorkflow("ChildWorkflow", "input", "child-1", options, String.class);

    ArgumentCaptor<TaskOptions> captor = ArgumentCaptor.forClass(TaskOptions.class);
    verify(mockInnerContext).callSubOrchestrator(
        eq("ChildWorkflow"), eq("input"), eq("child-1"), captor.capture(), eq(String.class));

    TaskOptions captured = captor.getValue();
    assertEquals("remote-app", captured.getAppId());
  }

  @Test
  @DisplayName("createTimer delegates duration to inner context")
  void createTimerDurationDelegates() {
    Duration delay = Duration.ofMinutes(5);
    context.createTimer(delay);
    verify(mockInnerContext).createTimer(delay);
  }

  @Test
  @DisplayName("createTimer delegates ZonedDateTime to inner context")
  void createTimerZonedDateTimeDelegates() {
    ZonedDateTime time = ZonedDateTime.now().plusHours(1);
    context.createTimer(time);
    verify(mockInnerContext).createTimer(time);
  }

  @Test
  @DisplayName("waitForExternalEvent delegates to inner context")
  void waitForExternalEventDelegates() throws TaskCanceledException {
    Duration timeout = Duration.ofMinutes(10);
    context.waitForExternalEvent("approval", timeout, String.class);
    verify(mockInnerContext).waitForExternalEvent("approval", timeout, String.class);
  }

  @Test
  @DisplayName("continueAsNew delegates to inner context")
  void continueAsNewDelegates() {
    context.continueAsNew("newInput", false);
    verify(mockInnerContext).continueAsNew("newInput", false);
  }

  @Test
  @DisplayName("newUuid delegates to inner context")
  void newUuidDelegates() {
    UUID expected = UUID.randomUUID();
    when(mockInnerContext.newUuid()).thenReturn(expected);
    assertEquals(expected, context.newUuid());
  }

  @Test
  @DisplayName("setCustomStatus delegates to inner context")
  void setCustomStatusDelegates() {
    context.setCustomStatus("processing");
    verify(mockInnerContext).setCustomStatus("processing");
  }

  @Test
  @DisplayName("isPatched delegates to inner context")
  void isPatchedDelegates() {
    when(mockInnerContext.isPatched("v2")).thenReturn(true);
    assertEquals(true, context.isPatched("v2"));
  }

  @Test
  @DisplayName("sendEvent delegates to inner context")
  void sendEventDelegates() {
    context.sendEvent("target-id", "myEvent", "data");
    verify(mockInnerContext).sendEvent("target-id", "myEvent", "data");
  }

  @Test
  @DisplayName("allOf delegates to inner context")
  void allOfDelegates() {
    context.allOf(Collections.emptyList());
    verify(mockInnerContext).allOf(Collections.emptyList());
  }

  @Test
  @DisplayName("anyOf delegates to inner context")
  void anyOfDelegates() {
    context.anyOf(Collections.emptyList());
    verify(mockInnerContext).anyOf(Collections.emptyList());
  }

  @Test
  @DisplayName("Default constructor creates context with class-based logger")
  void defaultConstructor() {
    WorkflowContext ctx = new WorkflowContext(mockInnerContext);
    assertNotNull(ctx);
  }

  @Test
  @DisplayName("Class-based constructor creates context")
  void classBasedConstructor() {
    WorkflowContext ctx = new WorkflowContext(mockInnerContext, getClass());
    assertNotNull(ctx);
  }
}
