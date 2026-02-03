/*
 * Copyright 2025 The Dapr Authors
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

package io.dapr.springboot.examples.workerone;


import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import io.dapr.workflows.WorkflowContext;
import io.dapr.workflows.WorkflowStub;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class RegisterV1Components  {

  private final Logger logger = LoggerFactory.getLogger(RegisterV1Components.class);

  @Component
  public class FullVersionWorkflow implements Workflow{
    private final Logger logger = LoggerFactory.getLogger(FullVersionWorkflow.class);

    @Override
    public WorkflowStub create() {
      return new WorkflowStub() {
        @Override
        public void run(WorkflowContext ctx) {
          String result = "";
          result += ctx.callActivity(Activity1.name, String.class).await() +", ";
          ctx.waitForExternalEvent("followup").await();
          result += ctx.callActivity(Activity2.name, String.class).await();

          ctx.complete(result);
        }
      };
    }

    @Override
    public String getName() {
      return "FullVersionWorkflow";
    }

    @Override
    public String getVersion() {
      return "V1";
    }

    @Override
    public Boolean isLatestVersion() {
      return true;
    }
  }

  @Component
  public class Activity1 implements WorkflowActivity {
    public static final String name = "Activity1";
    @Override
    public Object run(WorkflowActivityContext ctx) {
      logger.info(name + " started");
      return name;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  @Component
  public class Activity2 implements WorkflowActivity {
    public static final String name = "Activity2";
    @Override
    public Object run(WorkflowActivityContext ctx) {
      logger.info(name + " started");
      return name;
    }

    @Override
    public String getName() {
      return name;
    }
  }

}
