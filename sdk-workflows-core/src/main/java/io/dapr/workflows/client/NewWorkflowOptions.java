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

import java.time.Instant;

/**
 * Options for scheduling a new workflow instance.
 */
public final class NewWorkflowOptions {

  private String version;
  private String instanceId;
  private Object input;
  private Instant startTime;

  /**
   * Sets the workflow version.
   *
   * @param version the version
   * @return this options
   */
  public NewWorkflowOptions setVersion(String version) {
    this.version = version;
    return this;
  }

  /**
   * Sets the instance ID. If not set, one will be generated automatically.
   *
   * @param instanceId the instance ID
   * @return this options
   */
  public NewWorkflowOptions setInstanceId(String instanceId) {
    this.instanceId = instanceId;
    return this;
  }

  /**
   * Sets the workflow input.
   *
   * @param input the serializable input
   * @return this options
   */
  public NewWorkflowOptions setInput(Object input) {
    this.input = input;
    return this;
  }

  /**
   * Sets the start time for delayed execution.
   *
   * @param startTime the start time
   * @return this options
   */
  public NewWorkflowOptions setStartTime(Instant startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * Gets the version.
   *
   * @return the version
   */
  public String getVersion() {
    return this.version;
  }

  /**
   * Gets the instance ID.
   *
   * @return the instance ID
   */
  public String getInstanceId() {
    return this.instanceId;
  }

  /**
   * Gets the input.
   *
   * @return the input
   */
  public Object getInput() {
    return this.input;
  }

  /**
   * Gets the start time.
   *
   * @return the start time
   */
  public Instant getStartTime() {
    return this.startTime;
  }
}
