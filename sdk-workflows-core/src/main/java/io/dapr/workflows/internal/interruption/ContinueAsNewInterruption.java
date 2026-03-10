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

package io.dapr.workflows.internal.interruption;

/**
 * Thrown when an orchestrator calls continueAsNew, signaling a restart with new input.
 *
 * <p>This is a control-flow signal, not a user-visible error.</p>
 */
public final class ContinueAsNewInterruption extends RuntimeException {

  private final Object newInput;
  private final boolean preserveUnprocessedEvents;

  /**
   * Creates a new instance.
   *
   * @param newInput the new orchestration input
   * @param preserveUnprocessedEvents whether to keep unprocessed external events
   */
  public ContinueAsNewInterruption(Object newInput, boolean preserveUnprocessedEvents) {
    super("continueAsNew");
    this.newInput = newInput;
    this.preserveUnprocessedEvents = preserveUnprocessedEvents;
  }

  /**
   * Gets the new input for the restarted orchestration.
   *
   * @return the new input
   */
  public Object getNewInput() {
    return this.newInput;
  }

  /**
   * Returns whether unprocessed external events should be preserved.
   *
   * @return true if events should be preserved
   */
  public boolean isPreserveUnprocessedEvents() {
    return this.preserveUnprocessedEvents;
  }
}
