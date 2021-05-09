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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerManager.FactoryDescriptor;
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
     */
    @Test
    public void testContainerManager() throws URISyntaxException, ExecutionException, InterruptedException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.NONE); // no AAS here
        // TODO test against full AAS setup, see EcsAasTest
        DockerContainerManager cm = (DockerContainerManager) EcsFactory.getContainerManager();
        Assert.assertTrue(cm instanceof DockerContainerManager);
        // TODO go on testing with cm
        String testId = "01";
        
        //---- Adding container -----------------
        String workingDir = System.getProperty("user.dir");
        String imageLocationStr = workingDir + "/src/test/resources/";
        URI location = new URI(imageLocationStr);
        
        // Is the id of the container same as in the yaml file?
        Assert.assertEquals(testId, cm.addContainer(location));
        
        // Is Docker container with a given name deployed?
        String dockerId = cm.getContainer(testId).getDockerId();
        Assert.assertEquals("Created", getDockerState(dockerId));
        
        //---- Starting container -----------------
        cm.startContainer(testId);
        Thread.sleep(3000);
        Assert.assertEquals("Up", getDockerState(dockerId));
        
        //---- Stopping container -----------------
        
        cm.stopContainer(testId);
        Thread.sleep(3000);
        Assert.assertEquals("Exited", getDockerState(dockerId));
        
        // Removing container
        cm.undeployContainer(testId);
        
        ActiveAasBase.setNotificationMode(oldM);
    }
    
    /**
     * Returns a Docker state of a given container {@code dockerId}.
     * 
     * @param dockerId Docker id of the container
     * @return state Docker state of the container
     * @throws URISyntaxException
     */
    public static String getDockerState(String dockerId) throws URISyntaxException {
        String dockerState = null;
        
        Runtime rt = Runtime.getRuntime();
        String command = "docker container ls -a";
        try {
            Process proc = rt.exec(command);
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(proc.getInputStream()));
            
            // Read the output from the command
            String line = null;
            while (true) {
                line = stdInput.readLine();
                if (line == null) {
                    break;
                }
                
                // Output to parse:
                // CONTAINER ID        IMAGE                    COMMAND                  CREATED             STATUS    
                // 8f6983acd81a        arvindr226/alpine-ssh    "/usr/sbin/sshd -D"      3 weeks ago         Up 3 secon
                
                // Skipping the header
                if (line.substring(0, 12).equals("CONTAINER ID")) {
                    continue;
                }
                String conId = line.substring(0, 12).trim();
                
                if (conId.equals(dockerId)) {
                    String status = line.substring(90, 120).trim();
                    String[] statusAsList = status.split(" ");
                    dockerState = statusAsList[0];
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        
        return dockerState;
    }
    
}
