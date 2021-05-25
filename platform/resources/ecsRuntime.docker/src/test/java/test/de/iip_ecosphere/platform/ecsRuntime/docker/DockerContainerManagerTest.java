/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.ecsRuntime.docker;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;

import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerManager;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;

/**
 * Template test.
 * 
 * @author Monika Staciwa, SSE
 */
public class DockerContainerManagerTest {
        
    /**
     * Template test.
     * @throws URISyntaxException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws IOException 
     */
    @Test
    public void testContainerManager() throws 
        URISyntaxException, ExecutionException, InterruptedException, IOException {

        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.NONE); // no AAS here
        // TODO test against full AAS setup, see EcsAasTest
        DockerContainerManager cm = (DockerContainerManager) EcsFactory.getContainerManager();
        Assert.assertTrue(cm instanceof DockerContainerManager);
        
        // Checking if Docker API Client can connect with Docker daemon.
        if (cm.getDockerClient() == null) {
            return;
        }
        
        String testId = "01";
        String testName = "test-container";
        
        //---- Adding container -----------------
        String workingDir = System.getProperty("user.dir");
        String imageLocationStr = "file://" + workingDir + "/src/test/resources/";
        //String imageLocationStr = "http://localhost:8081/";
        URI location = new URI(imageLocationStr);        
        
        // Is the id of the container same as in the yaml file?
        Assert.assertEquals(testId, cm.addContainer(location));
        Assert.assertEquals(ContainerState.AVAILABLE, cm.getState(testId));
        Thread.sleep(4000);
        // Does the container have a Docker Id?
        Assert.assertNotNull(cm.getDockerId(testName));
        
        //---- Starting container -----------------
        cm.startContainer(testId);
        Thread.sleep(3000);
        // Is there a running container with a given name?
        Assert.assertNotNull(getContainerId(testName, "running", cm));
        Assert.assertEquals(ContainerState.DEPLOYED, cm.getState(testId));

        //---- Stopping container -----------------
        cm.stopContainer(testId);
        Thread.sleep(3000);
        // Is there a exited container with a given name?
        Assert.assertNotNull(getContainerId(testName, "exited", cm));
        Assert.assertEquals(ContainerState.STOPPED, cm.getState(testId));

        //---- Undeploying container --------------
        cm.undeployContainer(testId);
        Thread.sleep(3000);
        // Removed from Docker system?
        Assert.assertNull(cm.getDockerId(testName));
        // Removed from Platform?
        Assert.assertNull(cm.getContainer(testId));
        
        //---- Docker System Version --------------
        Assert.assertNotEquals("", cm.getContainerSystemVersion());
        
        ActiveAasBase.setNotificationMode(oldM);
        
    }
    
    /**
     * Returns the Docker Id of a container with a given {@code name} and {@code state}.
     * 
     * @param name container's name
     * @param state container's state (created, running etc)
     * @param cm container Manager
     * @return container Docker Id
     */
    public static String getContainerId(String name, String state, DockerContainerManager cm) {
        DockerClient dockerClient = cm.getDockerClient();
        
        ArrayList<Container> containers = (ArrayList<Container>) dockerClient.listContainersCmd()
                .withStatusFilter(Arrays.asList(state))
                .withNameFilter(Arrays.asList(name))
                .exec();
        
        if (containers.size() == 0) {
            return null;
        } 
        
        for (int i = 0; i < containers.size(); i++) {
            Container container = containers.get(i);
            String dockerName = container.getNames()[0];
            // removing the slash symbol before the name
            dockerName = dockerName.substring(1, dockerName.length());
            if (dockerName.equals(name)) {
                return container.getId();
            }
        }
        return null;
    }
}
