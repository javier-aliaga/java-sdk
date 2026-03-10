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

package io.dapr.workflows.client;

import io.dapr.workflows.internal.model.FailureDetails;

/**
 * Workflow failure details wrapping the internal engine's {@link FailureDetails}.
 */
public final class WorkflowFailureDetails {

  private final FailureDetails inner;

  /**
   * Creates a new instance wrapping internal failure details.
   *
   * @param inner the internal failure details
   */
  public WorkflowFailureDetails(FailureDetails inner) {
    this.inner = inner;
  }

  /**
   * Gets the error type (typically the exception class name).
   *
   * @return the error type
   */
  public String getErrorType() {
    return this.inner.getErrorType();
  }

  /**
   * Gets the error message.
   *
   * @return the error message
   */
  public String getErrorMessage() {
    return this.inner.getErrorMessage();
  }

  /**
   * Gets the stack trace, if available.
   *
   * @return the stack trace or null
   */
  public String getStackTrace() {
    return this.inner.getStackTrace();
  }

  /**
   * Checks if the failure was caused by the specified exception type.
   *
   * @param exceptionClass the exception class
   * @return true if caused by the specified type
   */
  public boolean isCausedBy(Class<? extends Exception> exceptionClass) {
    return this.inner.isCausedBy(exceptionClass);
  }
}
