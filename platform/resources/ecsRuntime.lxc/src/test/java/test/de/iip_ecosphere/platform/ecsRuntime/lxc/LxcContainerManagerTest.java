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

package test.de.iip_ecosphere.platform.ecsRuntime.lxc;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import au.com.jcloud.lxd.model.Container;
import au.com.jcloud.lxd.service.ILxdService;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.ecsRuntime.lxc.LxcContainerManager;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;

/**
 * Template test.
 * 
 * @author Luca Schulz, SSE
 */
public class LxcContainerManagerTest {
        
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
    	
    	// Some assumtions made for Jenkins to run test successfully
    	// To activate test again either assume false or set "!SystemUtils.IS_OS_WINDOWS"
    	Assume.assumeTrue(SystemUtils.IS_OS_WINDOWS);
    	Assume.assumeTrue(!SystemUtils.USER_HOME.startsWith("/home/"));
    	
    	String userHome = System.getProperty("user.home");
		System.setProperty("snap_cert", userHome + File.separator  + "snap/lxd/common/config/client.crt");
		System.setProperty("snap_key", userHome + File.separator  + "snap/lxd/common/config/client.key");

        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.NONE); // no AAS here
        // TODO test against full AAS setup, see EcsAasTest
        LxcContainerManager cm = (LxcContainerManager) EcsFactory.getContainerManager();
        Assert.assertTrue(cm instanceof LxcContainerManager);
        
        // Checking if LXC API Client can connect with LXC daemon.
        if (cm.getLxcClient() == null) {
        	System.out.println("null");
            return;
        }
        
        System.out.println(cm.getLxcClient().loadServerInfo());
        
        String testName = "test-container";

        //---- Adding container -----------------
        String workingDir = System.getProperty("user.dir");
        String imageLocationStr = "file://" + workingDir + "/src/test/resources/";
        URI location = new URI(imageLocationStr);        
        
        // Is the id of the container same as in the yaml file?
        Assert.assertEquals(testName, cm.addContainer(location));
        Thread.sleep(4000);
        Assert.assertEquals("Stopped", cm.getLxcClient().loadContainerState(testName).getStatus());
        
        //---- Starting container -----------------
        cm.startContainer(testName);
        Thread.sleep(3000);
        Assert.assertEquals("Running", cm.getLxcClient().loadContainerState(testName).getStatus());
        // Is there a running container with a given name?
        Assert.assertNotNull(getLxcName(testName, "running", cm));
        
        //---- Freezing container -----------------
        cm.freezeContainer(testName);
        Thread.sleep(3000);
        Assert.assertEquals("Frozen", cm.getLxcClient().loadContainerState(testName).getStatus());
        // Is there a running container with a given name?
        Assert.assertNotNull(getLxcName(testName, "frozen", cm));
        
        //---- Unfreezing container -----------------
        cm.unfreezeContainer(testName);
        Thread.sleep(3000);
        Assert.assertEquals("Running", cm.getLxcClient().loadContainerState(testName).getStatus());
        // Is there a running container with a given name?
        Assert.assertNotNull(getLxcName(testName, "running", cm));

        //---- Stopping container -----------------
        cm.stopContainer(testName);
        Thread.sleep(3000);
        Assert.assertEquals("Stopped", cm.getLxcClient().loadContainerState(testName).getStatus());
        // Is there a exited container with a given name?
        Assert.assertNotNull(getLxcName(testName, "stopped", cm));

        //---- Delete container --------------
        cm.deleteContainer(testName);
        Thread.sleep(3000);
        // Removed from system?
        Assert.assertNull(cm.getLxcName(testName));

        
        //---- LXC System Version --------------
        Assert.assertNotEquals(null, cm.getContainerSystemVersion());
        
        ActiveAasBase.setNotificationMode(oldM);
        
    }
    
    /**
     * Returns the Lxc Name of a container with a given {@code name} and {@code state}.
     * 
     * @param name container's name
     * @param state container's state (created, running etc)
     * @param cm container Manager
     * @return container Lxc Name
     */
    public String getLxcName(String name, String state, LxcContainerManager cm) throws ExecutionException {
        ILxdService lxcClient = cm.getLxcClient();
        
        // Getting list of all container that LXC "knows".
        Collection<Container> containers = null;
		try {
			containers = lxcClient.loadContainerMap().values();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (containers.size() == 0) {
	        return null;
		}
         
        // Looking for the container with a given name.
		for (Container container : containers) {
			if(container.getName().equals(name) && container.getStatus().equalsIgnoreCase(state)) {
				return container.getName();
			}
		}
        return null;
    }   
}
