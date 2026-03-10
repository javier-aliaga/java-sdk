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

package io.dapr.workflows.internal.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

/**
 * Internal utility methods.
 */
public final class Helpers {

  /** Maximum representable duration. */
  public static final Duration MAX_DURATION = Duration.ofSeconds(Long.MAX_VALUE, 999999999L);

  private Helpers() {
  }

  /**
   * Throws if the argument is null.
   *
   * @param argValue the argument value
   * @param argName the argument name
   * @param <V> the type of the argument
   * @return the argument value if not null
   */
  @Nonnull
  public static <V> V throwIfArgumentNull(@Nullable V argValue, String argName) {
    if (argValue == null) {
      throw new IllegalArgumentException("The argument '" + argName + "' was null.");
    }
    return argValue;
  }

  /**
   * Throws if the argument is null, empty, or whitespace.
   *
   * @param argValue the argument value
   * @param argName the argument name
   * @return the argument value if valid
   */
  @Nonnull
  public static String throwIfArgumentNullOrWhiteSpace(String argValue, String argName) {
    throwIfArgumentNull(argValue, argName);
    if (argValue.trim().isEmpty()) {
      throw new IllegalArgumentException(
          "The argument '" + argName + "' was empty or contained only whitespace.");
    }
    return argValue;
  }

  /**
   * Throws if the orchestrator has already completed.
   *
   * @param isComplete whether the orchestrator is complete
   */
  public static void throwIfOrchestratorComplete(boolean isComplete) {
    if (isComplete) {
      throw new IllegalStateException("The orchestrator has already completed");
    }
  }

  /**
   * Returns true if the timeout is infinite.
   *
   * @param timeout the timeout duration
   * @return true if infinite
   */
  public static boolean isInfiniteTimeout(Duration timeout) {
    return timeout == null || timeout.isNegative() || timeout.equals(MAX_DURATION);
  }

  /**
   * Safe power function that throws on overflow.
   *
   * @param base the base
   * @param exponent the exponent
   * @return the result
   */
  public static double powExact(double base, double exponent) {
    if (base == 0.0) {
      return 0.0;
    }
    double result = Math.pow(base, exponent);
    if (result == Double.POSITIVE_INFINITY) {
      throw new ArithmeticException("Double overflow resulting in POSITIVE_INFINITY");
    }
    if (result == Double.NEGATIVE_INFINITY) {
      throw new ArithmeticException("Double overflow resulting in NEGATIVE_INFINITY");
    }
    if (Double.compare(-0.0f, result) == 0) {
      throw new ArithmeticException("Double overflow resulting in negative zero");
    }
    if (Double.compare(+0.0f, result) == 0) {
      throw new ArithmeticException("Double overflow resulting in positive zero");
    }
    return result;
  }

  /**
   * Returns true if the string is null or empty.
   *
   * @param value the string to check
   * @return true if null or empty
   */
  public static boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }
}
