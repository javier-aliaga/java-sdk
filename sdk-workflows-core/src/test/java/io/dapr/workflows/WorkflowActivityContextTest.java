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

import io.dapr.workflows.internal.executor.TaskActivityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowActivityContextTest {

  private TaskActivityContext mockInnerContext;
  private Logger mockLogger;
  private WorkflowActivityContext context;

  @BeforeEach
  void setUp() {
    mockInnerContext = mock(TaskActivityContext.class);
    mockLogger = mock(Logger.class);
    context = new WorkflowActivityContext(mockInnerContext, mockLogger);
  }

  @Test
  @DisplayName("Constructor throws on null inner context")
  void constructorThrowsOnNullContext() {
    assertThrows(IllegalArgumentException.class,
        () -> new WorkflowActivityContext(null, mockLogger));
  }

  @Test
  @DisplayName("Constructor throws on null logger")
  void constructorThrowsOnNullLogger() {
    assertThrows(IllegalArgumentException.class,
        () -> new WorkflowActivityContext(mockInnerContext, null));
  }

  @Test
  @DisplayName("getLogger returns configured logger")
  void getLoggerReturns() {
    assertEquals(mockLogger, context.getLogger());
  }

  @Test
  @DisplayName("getName delegates to inner context")
  void getNameDelegates() {
    when(mockInnerContext.getName()).thenReturn("ProcessOrder");
    assertEquals("ProcessOrder", context.getName());
  }

  @Test
  @DisplayName("getTaskExecutionId delegates to inner context")
  void getTaskExecutionIdDelegates() {
    when(mockInnerContext.getTaskExecutionId()).thenReturn("exec-456");
    assertEquals("exec-456", context.getTaskExecutionId());
  }

  @Test
  @DisplayName("getInput delegates to inner context")
  void getInputDelegates() {
    when(mockInnerContext.getInput(String.class)).thenReturn("test-input");
    assertEquals("test-input", context.getInput(String.class));
  }

  @Test
  @DisplayName("getTraceParent delegates to inner context")
  void getTraceParentDelegates() {
    when(mockInnerContext.getTraceParent()).thenReturn("00-trace-id");
    assertEquals("00-trace-id", context.getTraceParent());
  }

  @Test
  @DisplayName("Default constructor creates context with default logger")
  void defaultConstructor() {
    WorkflowActivityContext ctx = new WorkflowActivityContext(mockInnerContext);
    assertNotNull(ctx);
    assertNotNull(ctx.getLogger());
  }
}
