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

import io.dapr.durabletask.implementation.protobuf.OrchestratorService;

/**
 * Represents the runtime status of a workflow instance.
 */
public enum WorkflowRuntimeStatus {

  /** The workflow is currently running. */
  RUNNING,

  /** The workflow completed normally. */
  COMPLETED,

  /** The workflow is transitioning to a new instance (compatibility). */
  CONTINUED_AS_NEW,

  /** The workflow failed with an unhandled exception. */
  FAILED,

  /** The workflow was canceled (compatibility). */
  CANCELED,

  /** The workflow was terminated via management API. */
  TERMINATED,

  /** The workflow is scheduled but not yet started. */
  PENDING,

  /** The workflow is in a suspended state. */
  SUSPENDED;

  /**
   * Converts from protobuf status.
   *
   * @param proto the protobuf status
   * @return the workflow runtime status
   */
  public static WorkflowRuntimeStatus fromProtobuf(
      OrchestratorService.OrchestrationStatus proto) {
    switch (proto) {
      case ORCHESTRATION_STATUS_RUNNING:
        return RUNNING;
      case ORCHESTRATION_STATUS_COMPLETED:
        return COMPLETED;
      case ORCHESTRATION_STATUS_CONTINUED_AS_NEW:
        return CONTINUED_AS_NEW;
      case ORCHESTRATION_STATUS_FAILED:
        return FAILED;
      case ORCHESTRATION_STATUS_CANCELED:
        return CANCELED;
      case ORCHESTRATION_STATUS_TERMINATED:
        return TERMINATED;
      case ORCHESTRATION_STATUS_PENDING:
        return PENDING;
      case ORCHESTRATION_STATUS_SUSPENDED:
        return SUSPENDED;
      default:
        throw new IllegalArgumentException("Unknown orchestration status: " + proto);
    }
  }

  /**
   * Converts to protobuf status.
   *
   * @return the protobuf status
   */
  public OrchestratorService.OrchestrationStatus toProtobuf() {
    switch (this) {
      case RUNNING:
        return OrchestratorService.OrchestrationStatus.ORCHESTRATION_STATUS_RUNNING;
      case COMPLETED:
        return OrchestratorService.OrchestrationStatus.ORCHESTRATION_STATUS_COMPLETED;
      case CONTINUED_AS_NEW:
        return OrchestratorService.OrchestrationStatus.ORCHESTRATION_STATUS_CONTINUED_AS_NEW;
      case FAILED:
        return OrchestratorService.OrchestrationStatus.ORCHESTRATION_STATUS_FAILED;
      case CANCELED:
        return OrchestratorService.OrchestrationStatus.ORCHESTRATION_STATUS_CANCELED;
      case TERMINATED:
        return OrchestratorService.OrchestrationStatus.ORCHESTRATION_STATUS_TERMINATED;
      case PENDING:
        return OrchestratorService.OrchestrationStatus.ORCHESTRATION_STATUS_PENDING;
      case SUSPENDED:
        return OrchestratorService.OrchestrationStatus.ORCHESTRATION_STATUS_SUSPENDED;
      default:
        throw new IllegalArgumentException("Unknown workflow runtime status: " + this);
    }
  }
}
