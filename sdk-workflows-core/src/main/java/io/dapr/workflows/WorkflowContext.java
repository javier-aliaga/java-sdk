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
import io.dapr.workflows.options.WorkflowTaskOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Context available to workflow orchestrations for scheduling tasks, timers, and sub-workflows.
 *
 * <p>Activity and sub-workflow calls pass {@link io.dapr.workflows.options.WorkflowRetryPolicy}
 * directly to the engine as a {@link io.dapr.workflows.internal.model.RetryPolicy} (via inheritance).
 * No field-by-field conversion is needed, eliminating propagation errors.</p>
 */
public final class WorkflowContext {

  private final TaskOrchestrationContext innerContext;
  private final Logger logger;

  /**
   * Creates a new context with a default logger.
   *
   * @param innerContext the internal orchestration context
   */
  public WorkflowContext(TaskOrchestrationContext innerContext) {
    this(innerContext, LoggerFactory.getLogger(WorkflowContext.class));
  }

  /**
   * Creates a new context with a logger for the specified class.
   *
   * @param innerContext the internal orchestration context
   * @param loggerClass the class for the logger
   */
  public WorkflowContext(TaskOrchestrationContext innerContext, Class<?> loggerClass) {
    this(innerContext, LoggerFactory.getLogger(loggerClass));
  }

  /**
   * Creates a new context with a custom logger.
   *
   * @param innerContext the internal orchestration context
   * @param logger the logger
   */
  public WorkflowContext(TaskOrchestrationContext innerContext, Logger logger) {
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
   * Gets a logger that is suppressed during replay.
   *
   * @return the logger
   */
  public Logger getLogger() {
    if (this.innerContext.getIsReplaying()) {
      return NOPLogger.NOP_LOGGER;
    }
    return this.logger;
  }

  /**
   * Gets the workflow name.
   *
   * @return the name
   */
  public String getName() {
    return this.innerContext.getName();
  }

  /**
   * Gets the unique workflow instance ID.
   *
   * @return the instance ID
   */
  public String getInstanceId() {
    return this.innerContext.getInstanceId();
  }

  /**
   * Gets the current orchestration time in UTC.
   *
   * @return the current instant
   */
  public Instant getCurrentInstant() {
    return this.innerContext.getCurrentInstant();
  }

  /**
   * Returns true if the orchestrator is replaying.
   *
   * @return true if replaying
   */
  public boolean isReplaying() {
    return this.innerContext.getIsReplaying();
  }

  /**
   * Gets the deserialized workflow input.
   *
   * @param targetType the target class
   * @param <V> the target type
   * @return the deserialized input or null
   */
  public <V> V getInput(Class<V> targetType) {
    return this.innerContext.getInput(targetType);
  }

  /**
   * Completes the workflow with the specified output.
   *
   * @param output the serializable output
   */
  public void complete(Object output) {
    this.innerContext.complete(output);
  }

  /**
   * Calls an activity with full options.
   *
   * @param name the activity name
   * @param input the input data
   * @param options task options (retry, app ID)
   * @param returnType the return type class
   * @param <V> the return type
   * @return a task that completes when the activity finishes
   */
  public <V> Task<V> callActivity(
      String name,
      @Nullable Object input,
      @Nullable WorkflowTaskOptions options,
      Class<V> returnType) {
    return this.innerContext.callActivity(name, input, toTaskOptions(options), returnType);
  }

  /**
   * Calls an activity by name.
   *
   * @param name the activity name
   * @return a task that completes when the activity finishes
   */
  public Task<Void> callActivity(String name) {
    return this.callActivity(name, null, null, Void.class);
  }

  /**
   * Calls an activity with input.
   *
   * @param name the activity name
   * @param input the input data
   * @return a task that completes when the activity finishes
   */
  public Task<Void> callActivity(String name, Object input) {
    return this.callActivity(name, input, null, Void.class);
  }

  /**
   * Calls an activity expecting a typed result.
   *
   * @param name the activity name
   * @param returnType the return type class
   * @param <V> the return type
   * @return a task that completes when the activity finishes
   */
  public <V> Task<V> callActivity(String name, Class<V> returnType) {
    return this.callActivity(name, null, null, returnType);
  }

  /**
   * Calls an activity with input expecting a typed result.
   *
   * @param name the activity name
   * @param input the input data
   * @param returnType the return type class
   * @param <V> the return type
   * @return a task that completes when the activity finishes
   */
  public <V> Task<V> callActivity(String name, Object input, Class<V> returnType) {
    return this.callActivity(name, input, null, returnType);
  }

  /**
   * Calls an activity with input and options.
   *
   * @param name the activity name
   * @param input the input data
   * @param options the task options
   * @return a task that completes when the activity finishes
   */
  public Task<Void> callActivity(String name, Object input, WorkflowTaskOptions options) {
    return this.callActivity(name, input, options, Void.class);
  }

  /**
   * Calls a child workflow with full options.
   *
   * @param name the workflow name
   * @param input the input data
   * @param instanceId the child workflow instance ID
   * @param options task options
   * @param returnType the return type class
   * @param <V> the return type
   * @return a task that completes when the child workflow finishes
   */
  public <V> Task<V> callChildWorkflow(
      String name,
      @Nullable Object input,
      @Nullable String instanceId,
      @Nullable WorkflowTaskOptions options,
      Class<V> returnType) {
    return this.innerContext.callSubOrchestrator(
        name, input, instanceId, toTaskOptions(options), returnType);
  }

  /**
   * Calls a child workflow by name.
   *
   * @param name the workflow name
   * @return a task that completes when the child workflow finishes
   */
  public Task<Void> callChildWorkflow(String name) {
    return this.callChildWorkflow(name, null, null, null, Void.class);
  }

  /**
   * Calls a child workflow with input.
   *
   * @param name the workflow name
   * @param input the input data
   * @return a task that completes when the child workflow finishes
   */
  public Task<Void> callChildWorkflow(String name, Object input) {
    return this.callChildWorkflow(name, input, null, null, Void.class);
  }

  /**
   * Calls a child workflow with input expecting a typed result.
   *
   * @param name the workflow name
   * @param input the input data
   * @param returnType the return type class
   * @param <V> the return type
   * @return a task that completes when the child workflow finishes
   */
  public <V> Task<V> callChildWorkflow(String name, Object input, Class<V> returnType) {
    return this.callChildWorkflow(name, input, null, null, returnType);
  }

  /**
   * Calls a child workflow with input, instance ID, and options.
   *
   * @param name the workflow name
   * @param input the input data
   * @param instanceId the child workflow instance ID
   * @param options the task options
   * @return a task that completes when the child workflow finishes
   */
  public Task<Void> callChildWorkflow(
      String name, Object input, String instanceId, WorkflowTaskOptions options) {
    return this.callChildWorkflow(name, input, instanceId, options, Void.class);
  }

  /**
   * Creates a durable timer with the specified delay.
   *
   * @param delay the delay before the timer expires
   * @return a task that completes after the delay
   */
  public Task<Void> createTimer(Duration delay) {
    return this.innerContext.createTimer(delay);
  }

  /**
   * Creates a durable timer that expires at the specified time.
   *
   * @param zonedDateTime the target time
   * @return a task that completes at the specified time
   */
  public Task<Void> createTimer(ZonedDateTime zonedDateTime) {
    return this.innerContext.createTimer(zonedDateTime);
  }

  /**
   * Waits for an external event with a timeout and typed data.
   *
   * @param name the event name
   * @param timeout the timeout duration
   * @param dataType the data type class
   * @param <V> the data type
   * @return a task that completes when the event is received
   * @throws TaskCanceledException if the timeout expires
   */
  public <V> Task<V> waitForExternalEvent(String name, Duration timeout, Class<V> dataType)
      throws TaskCanceledException {
    return this.innerContext.waitForExternalEvent(name, timeout, dataType);
  }

  /**
   * Waits for an external event with a timeout.
   *
   * @param name the event name
   * @param timeout the timeout duration
   * @return a task that completes when the event is received
   * @throws TaskCanceledException if the timeout expires
   */
  public Task<Void> waitForExternalEvent(String name, Duration timeout)
      throws TaskCanceledException {
    return this.waitForExternalEvent(name, timeout, Void.class);
  }

  /**
   * Waits for an external event indefinitely.
   *
   * @param name the event name
   * @return a task that completes when the event is received
   */
  public Task<Void> waitForExternalEvent(String name) {
    return this.waitForExternalEvent(name, Void.class);
  }

  /**
   * Waits for an external event with typed data indefinitely.
   *
   * @param name the event name
   * @param dataType the data type class
   * @param <V> the data type
   * @return a task that completes when the event is received
   */
  public <V> Task<V> waitForExternalEvent(String name, Class<V> dataType) {
    try {
      return this.waitForExternalEvent(name, null, dataType);
    } catch (TaskCanceledException e) {
      throw new RuntimeException(
          "An unexpected exception was thrown while waiting for an external event.", e);
    }
  }

  /**
   * Waits for all tasks to complete.
   *
   * @param tasks the tasks
   * @param <V> the task value type
   * @return a task that completes when all input tasks complete
   */
  public <V> Task<List<V>> allOf(List<Task<V>> tasks) {
    return this.innerContext.allOf(tasks);
  }

  /**
   * Waits for all tasks to complete (varargs).
   *
   * @param tasks the tasks
   * @param <V> the task value type
   * @return a task that completes when all input tasks complete
   */
  @SuppressWarnings("unchecked")
  public <V> Task<List<V>> allOf(Task<V>... tasks) {
    return this.allOf(Arrays.asList(tasks));
  }

  /**
   * Waits for any task to complete.
   *
   * @param tasks the tasks
   * @return a task containing the first completed task
   */
  public Task<Task<?>> anyOf(List<Task<?>> tasks) {
    return this.innerContext.anyOf(tasks);
  }

  /**
   * Waits for any task to complete (varargs).
   *
   * @param tasks the tasks
   * @return a task containing the first completed task
   */
  public Task<Task<?>> anyOf(Task<?>... tasks) {
    return this.anyOf(Arrays.asList(tasks));
  }

  /**
   * Restarts the workflow with new input, preserving unprocessed events.
   *
   * @param input the new input
   */
  public void continueAsNew(Object input) {
    this.continueAsNew(input, true);
  }

  /**
   * Restarts the workflow with new input.
   *
   * @param input the new input
   * @param preserveUnprocessedEvents whether to keep unprocessed events
   */
  public void continueAsNew(Object input, boolean preserveUnprocessedEvents) {
    this.innerContext.continueAsNew(input, preserveUnprocessedEvents);
  }

  /**
   * Generates a deterministic UUID safe for replay.
   *
   * @return a deterministic UUID
   */
  public UUID newUuid() {
    return this.innerContext.newUuid();
  }

  /**
   * Sets a custom status value for the workflow.
   *
   * @param customStatus the custom status
   */
  public void setCustomStatus(Object customStatus) {
    this.innerContext.setCustomStatus(customStatus);
  }

  /**
   * Checks if the given patch can be applied.
   *
   * @param patchName the patch name
   * @return true if the patch can be applied
   */
  public boolean isPatched(String patchName) {
    return this.innerContext.isPatched(patchName);
  }

  /**
   * Sends an event to another workflow instance.
   *
   * @param instanceId the target instance ID
   * @param eventName the event name
   * @param eventData the event payload
   */
  public void sendEvent(String instanceId, String eventName, Object eventData) {
    this.innerContext.sendEvent(instanceId, eventName, eventData);
  }

  /**
   * Sends an event to another workflow instance.
   *
   * @param instanceId the target instance ID
   * @param eventName the event name
   */
  public void sendEvent(String instanceId, String eventName) {
    this.sendEvent(instanceId, eventName, null);
  }

  @Nullable
  private TaskOptions toTaskOptions(@Nullable WorkflowTaskOptions options) {
    if (options == null) {
      return null;
    }
    return options.toTaskOptions(this);
  }
}
