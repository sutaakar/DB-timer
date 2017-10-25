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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.integrationtests.controller.client.KieServerMgmtControllerClient;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class TimerIT {

    private static ReleaseId releaseIdTimerProject = new ReleaseId("org.kie.server.testing", "timer-project",
            "1.0.0.Final");
    private static ReleaseId releaseIdTimerProcess = new ReleaseId("org.kie.server.testing", "intermediate-timer-process",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "timer-project";

    private static KieServerMgmtControllerClient kieServerMgmtControllerClient;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/timer-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/intermediate-timer-process").getFile());

        kieServerMgmtControllerClient = new KieServerMgmtControllerClient("http://localhost:8080/standalone-controller/rest/controller",
                "controllerUser", "controllerUser1@");
    }

//    @Test
//    public void testTimerStartEvent() throws InterruptedException {
//        kieServerMgmtControllerClient.saveContainerSpec("test-kie-server", "test-kie-server", CONTAINER_ID, CONTAINER_ID, releaseIdTimerProject.getGroupId(), releaseIdTimerProject.getArtifactId(), releaseIdTimerProject.getVersion(), KieContainerStatus.STARTED);
//
//        Thread.sleep(15_000L);
//    }

    @Test
    public void testTimerIntermediateEvent() throws InterruptedException {
        kieServerMgmtControllerClient.saveContainerSpec("test-kie-server", "test-kie-server", CONTAINER_ID, CONTAINER_ID, releaseIdTimerProcess.getGroupId(), releaseIdTimerProcess.getArtifactId(), releaseIdTimerProcess.getVersion(), KieContainerStatus.STARTED);

        // Wait until project is deployed
        Thread.sleep(5_000L);

        List<Thread> workers = new ArrayList<Thread>(); 

        for (int i=0; i<8; i++) {
            Thread worker = new Thread(new Worker());
            worker.start();
            workers.add(worker);
        }

        for (Thread worker : workers) {
            worker.join();
        }

        Thread.sleep(30_000L);
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration("http://localhost:8080/kie-server/services/rest/server",
                    "yoda", "usetheforce123@", 30_000L);
            KieServicesClient kieServerClient = KieServicesFactory.newKieServicesClient(configuration);
            ProcessServicesClient processClient = kieServerClient.getServicesClient(ProcessServicesClient.class);

            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("timer", "15s");
            processClient.startProcess(CONTAINER_ID, "definition-project.timer-process", variables);
        }
    }
}
