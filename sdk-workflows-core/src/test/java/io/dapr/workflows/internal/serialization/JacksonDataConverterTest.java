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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JacksonDataConverterTest {

  private JacksonDataConverter converter;

  @BeforeEach
  void setUp() {
    converter = new JacksonDataConverter();
  }

  @Test
  @DisplayName("serialize null returns null")
  void serializeNull() {
    assertNull(converter.serialize(null));
  }

  @Test
  @DisplayName("serialize string value")
  void serializeString() {
    assertEquals("\"hello\"", converter.serialize("hello"));
  }

  @Test
  @DisplayName("serialize integer value")
  void serializeInteger() {
    assertEquals("42", converter.serialize(42));
  }

  @Test
  @DisplayName("deserialize null returns null")
  void deserializeNull() {
    assertNull(converter.deserialize(null, String.class));
  }

  @Test
  @DisplayName("deserialize empty string returns null")
  void deserializeEmpty() {
    assertNull(converter.deserialize("", String.class));
  }

  @Test
  @DisplayName("deserialize Void type returns null")
  void deserializeVoid() {
    assertNull(converter.deserialize("\"data\"", Void.class));
  }

  @Test
  @DisplayName("deserialize string value")
  void deserializeString() {
    assertEquals("hello", converter.deserialize("\"hello\"", String.class));
  }

  @Test
  @DisplayName("deserialize integer value")
  void deserializeInteger() {
    assertEquals(42, converter.deserialize("42", Integer.class));
  }

  @Test
  @DisplayName("roundtrip serialization")
  void roundtrip() {
    String original = "test data";
    String serialized = converter.serialize(original);
    String deserialized = converter.deserialize(serialized, String.class);
    assertEquals(original, deserialized);
  }

  @Test
  @DisplayName("deserialize invalid JSON throws DataConverterException")
  void deserializeInvalidJson() {
    assertThrows(DataConverter.DataConverterException.class,
        () -> converter.deserialize("not valid json", Integer.class));
  }
}
