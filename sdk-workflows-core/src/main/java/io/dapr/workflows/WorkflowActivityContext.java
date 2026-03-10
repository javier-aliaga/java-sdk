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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Context for workflow activity execution.
 */
public final class WorkflowActivityContext {

  private final TaskActivityContext innerContext;
  private final Logger logger;

  /**
   * Creates a new context with a default logger.
   *
   * @param innerContext the internal activity context
   */
  public WorkflowActivityContext(TaskActivityContext innerContext) {
    this(innerContext, LoggerFactory.getLogger(WorkflowActivityContext.class));
  }

  /**
   * Creates a new context with a custom logger.
   *
   * @param innerContext the internal activity context
   * @param logger the logger
   */
  public WorkflowActivityContext(TaskActivityContext innerContext, Logger logger) {
    if (innerContext == null) {
      throw new IllegalArgumentException("innerContext cannot be null");
    }
    if (logger == null) {
      throw new IllegalArgumentException("logger cannot be null");
    }
    this.innerContext = innerContext;
    this.logger = logger;
  }

  /**
   * Gets a logger for the activity.
   *
   * @return the logger
   */
  public Logger getLogger() {
    return this.logger;
  }

  /**
   * Gets the name of the current activity.
   *
   * @return the activity name
   */
  public String getName() {
    return this.innerContext.getName();
  }

  /**
   * Gets the execution ID.
   *
   * @return the execution ID
   */
  public String getTaskExecutionId() {
    return this.innerContext.getTaskExecutionId();
  }

  /**
   * Gets the deserialized activity input.
   *
   * @param targetType the target class
   * @param <T> the target type
   * @return the deserialized input
   */
  public <T> T getInput(Class<T> targetType) {
    return this.innerContext.getInput(targetType);
  }

  /**
   * Gets the trace parent for distributed tracing.
   *
   * @return the trace parent
   */
  public String getTraceParent() {
    return this.innerContext.getTraceParent();
  }
}
