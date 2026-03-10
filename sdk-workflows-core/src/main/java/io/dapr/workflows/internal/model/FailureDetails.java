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

import com.google.protobuf.StringValue;
import io.dapr.durabletask.implementation.protobuf.OrchestratorService.TaskFailureDetails;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Details of a task failure.
 */
public final class FailureDetails {

  private final String errorType;
  private final String errorMessage;
  private final String stackTrace;
  private final boolean nonRetriable;

  /**
   * Creates failure details from individual fields.
   *
   * @param errorType the error type
   * @param errorMessage the error message
   * @param stackTrace the stack trace
   * @param nonRetriable whether the failure is non-retriable
   */
  public FailureDetails(
      String errorType,
      @Nullable String errorMessage,
      @Nullable String stackTrace,
      boolean nonRetriable) {
    this.errorType = errorType;
    this.stackTrace = stackTrace;
    this.errorMessage = errorMessage != null ? errorMessage : "";
    this.nonRetriable = nonRetriable;
  }

  /**
   * Creates failure details from an exception.
   *
   * @param exception the exception
   */
  public FailureDetails(Exception exception) {
    this(exception.getClass().getName(), exception.getMessage(), getFullStackTrace(exception), false);
  }

  /**
   * Creates failure details from a protobuf message.
   *
   * @param proto the protobuf failure details
   */
  public FailureDetails(TaskFailureDetails proto) {
    this(proto.getErrorType(),
        proto.getErrorMessage(),
        proto.getStackTrace().getValue(),
        proto.getIsNonRetriable());
  }

  /**
   * Gets the error type (typically the exception class name).
   *
   * @return the error type
   */
  @Nonnull
  public String getErrorType() {
    return this.errorType;
  }

  /**
   * Gets the error message.
   *
   * @return the error message
   */
  @Nonnull
  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * Gets the stack trace, or null if unavailable.
   *
   * @return the stack trace
   */
  @Nullable
  public String getStackTrace() {
    return this.stackTrace;
  }

  /**
   * Returns true if the failure does not permit retries.
   *
   * @return true if non-retriable
   */
  public boolean isNonRetriable() {
    return this.nonRetriable;
  }

  /**
   * Checks if this failure was caused by the specified exception type.
   *
   * @param exceptionClass the exception class to check
   * @return true if caused by the specified type
   */
  public boolean isCausedBy(Class<? extends Exception> exceptionClass) {
    try {
      Class<?> actualClass = Class.forName(this.getErrorType());
      return exceptionClass.isAssignableFrom(actualClass);
    } catch (ClassNotFoundException ex) {
      return false;
    }
  }

  /**
   * Gets the full stack trace of the specified throwable.
   *
   * @param throwable the throwable
   * @return the formatted stack trace
   */
  public static String getFullStackTrace(Throwable throwable) {
    StackTraceElement[] elements = throwable.getStackTrace();
    StringBuilder sb = new StringBuilder(elements.length * 256);
    for (StackTraceElement element : elements) {
      sb.append("\tat ").append(element.toString()).append(System.lineSeparator());
    }
    return sb.toString();
  }

  /**
   * Converts to protobuf representation.
   *
   * @return the protobuf failure details
   */
  public TaskFailureDetails toProto() {
    return TaskFailureDetails.newBuilder()
        .setErrorType(this.getErrorType())
        .setErrorMessage(this.getErrorMessage())
        .setStackTrace(StringValue.of(this.getStackTrace() != null ? this.getStackTrace() : ""))
        .build();
  }
}
