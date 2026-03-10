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

package io.dapr.workflows.internal.exception;

import java.util.Collections;
import java.util.List;

/**
 * Exception thrown when one or more tasks in an allOf operation fail.
 */
public final class CompositeTaskFailedException extends RuntimeException {

  private final List<Exception> exceptions;

  /**
   * Creates a new instance.
   *
   * @param message the detail message
   * @param exceptions the list of exceptions from failed tasks
   */
  public CompositeTaskFailedException(String message, List<Exception> exceptions) {
    super(message);
    this.exceptions = Collections.unmodifiableList(exceptions);
  }

  /**
   * Gets the list of exceptions from the failed tasks.
   *
   * @return unmodifiable list of exceptions
   */
  public List<Exception> getExceptions() {
    return this.exceptions;
  }
}
