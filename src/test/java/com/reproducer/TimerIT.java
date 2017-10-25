/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.reproducer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.controller.client.KieServerMgmtControllerClient;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class TimerIT {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "timer-project",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "timer-project";

    private static KieServerMgmtControllerClient kieServerMgmtControllerClient;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/timer-project").getFile());

        kieServerMgmtControllerClient = new KieServerMgmtControllerClient("http://localhost:8080/standalone-controller/rest/controller",
                "controllerUser", "controllerUser1@");
    }

    @Test
    public void testTimerStartEvent() throws InterruptedException {
        kieServerMgmtControllerClient.saveContainerSpec("test-kie-server", "test-kie-server", CONTAINER_ID, CONTAINER_ID, releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion(), KieContainerStatus.STARTED);

        Thread.sleep(15_000L);
    }
}
