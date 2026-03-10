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

package io.dapr.workflows.internal.serialization;

import com.google.protobuf.Timestamp;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Interface for serializing and deserializing data passed to orchestrators and activities.
 */
public interface DataConverter {

  /**
   * Serializes the input into a text representation.
   *
   * @param value the value to serialize
   * @return the serialized text or null if the value is null
   */
  @Nullable
  String serialize(@Nullable Object value);

  /**
   * Deserializes text data into an object of the specified type.
   *
   * @param data the text data
   * @param target the target class
   * @param <T> the target type
   * @return the deserialized object or null
   */
  @Nullable
  <T> T deserialize(@Nullable String data, Class<T> target);

  /**
   * Unchecked exception for data conversion errors.
   */
  class DataConverterException extends RuntimeException {
    /**
     * Creates a new DataConverterException.
     *
     * @param message the error message
     * @param cause the root cause
     */
    public DataConverterException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * Converts a protobuf Timestamp to an Instant.
   *
   * @param ts the timestamp
   * @return the instant
   */
  static Instant getInstantFromTimestamp(Timestamp ts) {
    if (ts == null) {
      return null;
    }
    return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).truncatedTo(ChronoUnit.MILLIS);
  }

  /**
   * Converts an Instant to a protobuf Timestamp.
   *
   * @param instant the instant
   * @return the timestamp
   */
  static Timestamp getTimestampFromInstant(Instant instant) {
    if (instant == null) {
      return null;
    }
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }
}
