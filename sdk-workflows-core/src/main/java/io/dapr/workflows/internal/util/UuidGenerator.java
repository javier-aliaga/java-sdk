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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Deterministic UUID generation for replay-safe orchestration IDs.
 */
public final class UuidGenerator {

  private UuidGenerator() {
  }

  /**
   * Generates a deterministic UUID from a namespace and name.
   *
   * @param version the UUID version
   * @param algorithm the hash algorithm
   * @param namespace the namespace UUID
   * @param name the name to hash
   * @return the generated UUID
   */
  public static UUID generate(int version, String algorithm, UUID namespace, String name) {
    MessageDigest hasher = createHasher(algorithm);

    if (namespace != null) {
      ByteBuffer ns = ByteBuffer.allocate(16);
      ns.putLong(namespace.getMostSignificantBits());
      ns.putLong(namespace.getLeastSignificantBits());
      hasher.update(ns.array());
    }

    hasher.update(name.getBytes(StandardCharsets.UTF_8));
    ByteBuffer hash = ByteBuffer.wrap(hasher.digest());

    final long msb = (hash.getLong() & 0xffffffffffff0fffL) | (version & 0x0f) << 12;
    final long lsb = (hash.getLong() & 0x3fffffffffffffffL) | 0x8000000000000000L;

    return new UUID(msb, lsb);
  }

  private static MessageDigest createHasher(String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(String.format("%s not supported.", algorithm));
    }
  }
}
