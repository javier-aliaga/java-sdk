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

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents an asynchronous operation in a durable orchestration.
 *
 * <p>Tasks are created by the workflow context when scheduling activities, sub-workflows, or timers.
 * Orchestrator code uses {@link #await()} to block until the task completes.</p>
 *
 * @param <V> the return type of the task
 */
public abstract class Task<V> {

  /** The underlying completable future. */
  protected final CompletableFuture<V> future;

  /**
   * Creates a new task wrapping a completable future.
   *
   * @param future the future
   */
  protected Task(CompletableFuture<V> future) {
    this.future = future;
  }

  /**
   * Returns true if the task completed in any fashion.
   *
   * @return true if done
   */
  public boolean isDone() {
    return this.future.isDone();
  }

  /**
   * Returns true if the task was cancelled.
   *
   * @return true if cancelled
   */
  public boolean isCancelled() {
    return this.future.isCancelled();
  }

  /**
   * Blocks the orchestrator until this task completes and returns its result.
   *
   * @return the result
   */
  public abstract V await();

  /**
   * Returns a new task that applies a function to this task's result.
   *
   * @param fn the function
   * @param <U> the function's return type
   * @return the new task
   */
  public abstract <U> Task<U> thenApply(Function<V, U> fn);

  /**
   * Returns a new task that applies an action to this task's result.
   *
   * @param fn the consumer action
   * @return the new task
   */
  public abstract Task<Void> thenAccept(Consumer<V> fn);
}
