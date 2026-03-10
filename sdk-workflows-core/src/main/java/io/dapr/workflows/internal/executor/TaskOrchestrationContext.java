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

package io.dapr.workflows.internal.executor;

import io.dapr.workflows.internal.exception.TaskCanceledException;
import io.dapr.workflows.internal.model.Task;
import io.dapr.workflows.internal.model.TaskOptions;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Internal orchestration context used by the replay engine.
 *
 * <p>This interface is implemented by the executor's inner context and wrapped by
 * {@link io.dapr.workflows.internal.context.DefaultWorkflowContext} to provide the public
 * {@link io.dapr.workflows.WorkflowContext} API.</p>
 */
public interface TaskOrchestrationContext {

  /**
   * Gets the name of the current orchestration.
   *
   * @return the orchestration name
   */
  String getName();

  /**
   * Gets the deserialized input.
   *
   * @param targetType the target class
   * @param <V> the expected type
   * @return the deserialized input or null
   */
  <V> V getInput(Class<V> targetType);

  /**
   * Gets the unique instance ID.
   *
   * @return the instance ID
   */
  String getInstanceId();

  /**
   * Gets the app ID for cross-app routing.
   *
   * @return the app ID or null
   */
  String getAppId();

  /**
   * Gets the current orchestration time in UTC.
   *
   * @return the current instant
   */
  Instant getCurrentInstant();

  /**
   * Returns true if the orchestrator is replaying.
   *
   * @return true if replaying
   */
  boolean getIsReplaying();

  /**
   * Waits for all tasks to complete.
   *
   * @param tasks the tasks
   * @param <V> the task value type
   * @return a task that completes when all input tasks complete
   */
  <V> Task<List<V>> allOf(List<Task<V>> tasks);

  /**
   * Waits for all tasks to complete (varargs).
   *
   * @param tasks the tasks
   * @param <V> the task value type
   * @return a task that completes when all input tasks complete
   */
  @SuppressWarnings("unchecked")
  default <V> Task<List<V>> allOf(Task<V>... tasks) {
    return this.allOf(Arrays.asList(tasks));
  }

  /**
   * Waits for any task to complete.
   *
   * @param tasks the tasks
   * @return a task containing the first completed task
   */
  Task<Task<?>> anyOf(List<Task<?>> tasks);

  /**
   * Waits for any task to complete (varargs).
   *
   * @param tasks the tasks
   * @return a task containing the first completed task
   */
  default Task<Task<?>> anyOf(Task<?>... tasks) {
    return this.anyOf(Arrays.asList(tasks));
  }

  /**
   * Creates a named durable timer.
   *
   * @param name the timer name
   * @param delay the delay duration
   * @return a task that completes after the delay
   */
  Task<Void> createTimer(String name, Duration delay);

  /**
   * Creates a durable timer with the specified delay.
   *
   * @param delay the delay duration
   * @return a task that completes after the delay
   */
  Task<Void> createTimer(Duration delay);

  /**
   * Creates a durable timer that fires at the specified time.
   *
   * @param zonedDateTime the target time
   * @return a task that completes at the specified time
   */
  Task<Void> createTimer(ZonedDateTime zonedDateTime);

  /**
   * Creates a named durable timer that fires at the specified time.
   *
   * @param name the timer name
   * @param zonedDateTime the target time
   * @return a task that completes at the specified time
   */
  Task<Void> createTimer(String name, ZonedDateTime zonedDateTime);

  /**
   * Completes the orchestration with the specified output.
   *
   * @param output the orchestration output
   */
  void complete(Object output);

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
  <V> Task<V> callActivity(String name, Object input, TaskOptions options, Class<V> returnType);

  /**
   * Calls an activity by name.
   *
   * @param name the activity name
   * @return a task that completes when the activity finishes
   */
  default Task<Void> callActivity(String name) {
    return this.callActivity(name, Void.class);
  }

  /**
   * Calls an activity with input.
   *
   * @param name the activity name
   * @param input the input data
   * @return a task that completes when the activity finishes
   */
  default Task<Void> callActivity(String name, Object input) {
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
  default <V> Task<V> callActivity(String name, Class<V> returnType) {
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
  default <V> Task<V> callActivity(String name, Object input, Class<V> returnType) {
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
  default Task<Void> callActivity(String name, Object input, TaskOptions options) {
    return this.callActivity(name, input, options, Void.class);
  }

  /**
   * Restarts the orchestration with new input, preserving unprocessed events.
   *
   * @param input the new input
   */
  default void continueAsNew(Object input) {
    this.continueAsNew(input, true);
  }

  /**
   * Restarts the orchestration with new input.
   *
   * @param input the new input
   * @param preserveUnprocessedEvents whether to keep unprocessed events
   */
  void continueAsNew(Object input, boolean preserveUnprocessedEvents);

  /**
   * Checks if the given patch can be applied.
   *
   * @param patchName the patch name
   * @return true if the patch can be applied
   */
  boolean isPatched(String patchName);

  /**
   * Generates a deterministic UUID safe for replay.
   *
   * @return a deterministic UUID
   */
  default UUID newUuid() {
    throw new RuntimeException("No implementation found.");
  }

  /**
   * Sends an event to another orchestration instance.
   *
   * @param instanceId the target instance ID
   * @param eventName the event name
   */
  default void sendEvent(String instanceId, String eventName) {
    this.sendEvent(instanceId, eventName, null);
  }

  /**
   * Sends an event with data to another orchestration instance.
   *
   * @param instanceId the target instance ID
   * @param eventName the event name
   * @param eventData the event payload
   */
  void sendEvent(String instanceId, String eventName, Object eventData);

  /**
   * Calls a sub-orchestration with full options.
   *
   * @param name the orchestrator name
   * @param input the input data
   * @param instanceId the sub-orchestration instance ID
   * @param options task options
   * @param returnType the return type class
   * @param <V> the return type
   * @return a task that completes when the sub-orchestration finishes
   */
  <V> Task<V> callSubOrchestrator(
      String name,
      @Nullable Object input,
      @Nullable String instanceId,
      @Nullable TaskOptions options,
      Class<V> returnType);

  /**
   * Calls a sub-orchestration by name.
   *
   * @param name the orchestrator name
   * @return a task that completes when the sub-orchestration finishes
   */
  default Task<Void> callSubOrchestrator(String name) {
    return this.callSubOrchestrator(name, null);
  }

  /**
   * Calls a sub-orchestration with input.
   *
   * @param name the orchestrator name
   * @param input the input data
   * @return a task that completes when the sub-orchestration finishes
   */
  default Task<Void> callSubOrchestrator(String name, Object input) {
    return this.callSubOrchestrator(name, input, Void.class);
  }

  /**
   * Calls a sub-orchestration with input expecting a typed result.
   *
   * @param name the orchestrator name
   * @param input the input data
   * @param returnType the return type class
   * @param <V> the return type
   * @return a task that completes when the sub-orchestration finishes
   */
  default <V> Task<V> callSubOrchestrator(String name, Object input, Class<V> returnType) {
    return this.callSubOrchestrator(name, input, null, returnType);
  }

  /**
   * Calls a sub-orchestration with input and instance ID.
   *
   * @param name the orchestrator name
   * @param input the input data
   * @param instanceId the sub-orchestration instance ID
   * @param returnType the return type class
   * @param <V> the return type
   * @return a task that completes when the sub-orchestration finishes
   */
  default <V> Task<V> callSubOrchestrator(
      String name, Object input, String instanceId, Class<V> returnType) {
    return this.callSubOrchestrator(name, input, instanceId, null, returnType);
  }

  /**
   * Calls a sub-orchestration with input, instance ID, and options.
   *
   * @param name the orchestrator name
   * @param input the input data
   * @param instanceId the sub-orchestration instance ID
   * @param options the task options
   * @return a task that completes when the sub-orchestration finishes
   */
  default Task<Void> callSubOrchestrator(
      String name, Object input, String instanceId, TaskOptions options) {
    return this.callSubOrchestrator(name, input, instanceId, options, Void.class);
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
  <V> Task<V> waitForExternalEvent(String name, Duration timeout, Class<V> dataType)
      throws TaskCanceledException;

  /**
   * Waits for an external event with a timeout.
   *
   * @param name the event name
   * @param timeout the timeout duration
   * @return a task that completes when the event is received
   * @throws TaskCanceledException if the timeout expires
   */
  default Task<Void> waitForExternalEvent(String name, Duration timeout)
      throws TaskCanceledException {
    return this.waitForExternalEvent(name, timeout, Void.class);
  }

  /**
   * Waits for an external event indefinitely.
   *
   * @param name the event name
   * @return a task that completes when the event is received
   */
  default Task<Void> waitForExternalEvent(String name) {
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
  default <V> Task<V> waitForExternalEvent(String name, Class<V> dataType) {
    try {
      return this.waitForExternalEvent(name, null, dataType);
    } catch (TaskCanceledException e) {
      throw new RuntimeException(
          "An unexpected exception was thrown while waiting for an external event.", e);
    }
  }

  /**
   * Sets a custom status value for the orchestration.
   *
   * @param customStatus the custom status
   */
  void setCustomStatus(Object customStatus);

  /**
   * Clears the orchestration's custom status.
   */
  void clearCustomStatus();
}
