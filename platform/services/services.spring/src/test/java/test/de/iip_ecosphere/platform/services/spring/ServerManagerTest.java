/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.spring;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.spring.ArtifactResolver;
import de.iip_ecosphere.platform.services.spring.DescriptorUtils;
import de.iip_ecosphere.platform.services.spring.ServerManager;
import de.iip_ecosphere.platform.services.spring.SpringCloudArtifactDescriptor;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceDescriptor;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;
import de.iip_ecosphere.platform.services.spring.SpringInstances;
import de.iip_ecosphere.platform.services.spring.descriptor.Server;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.services.spring.yaml.YamlServer;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

/**
 * Tests {@link ServerManager} and {@link ArtifactResolver}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServerManagerTest {
    
    /**
     * Tests a server manager with Spring JAR and ZIP.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testServerManager() throws ExecutionException {
        NotificationMode oldMode = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        SpringCloudServiceSetup oldSetup = SpringInstances.getConfig();
        SpringCloudServiceSetup setup = new SpringCloudServiceSetup();
        setup.setDownloadDir(FileUtils.getTempDirectory());
        SpringInstances.setConfig(setup);

        System.out.println("\nTesting JAR:");
        testServerManager(new File("target/jars/simpleStream.spring.jar"), false);
        
        System.out.println("\nTesting JAR (as process):");
        testServerManager(new File("target/jars/simpleStream.spring.jar"), true);
       
        System.out.println("\nTesting ZIP:");
        testServerManager(new File("target/jars/simpleStream.spring.zip"), false);
        
        System.out.println("\nTesting ZIP (as process):");
        testServerManager(new File("target/jars/simpleStream.spring.zip"), true);

        SpringInstances.setConfig(oldSetup);
        ActiveAasBase.setNotificationMode(oldMode);
    }

    /**
     * Tests a server manager with {@code jarFile}.
     * 
     * @param file the artifact file to use
     * @param forceAsProcess force starting servers as own JVM processes
     * @return the created server manager
     * @throws ExecutionException shall not occur
     */
    private ServerManager testServerManager(File file, boolean forceAsProcess) throws ExecutionException {
        ServerManager mgr = new ServerManager(() -> NetworkManagerFactory.getInstance());
        Assert.assertEquals(0, mgr.getRunningServersCount());
                
        YamlArtifact yamlArtifact = DescriptorUtils.readFromFile(file);
        SpringCloudArtifactDescriptor artifact = SpringCloudArtifactDescriptor.createInstance(
            yamlArtifact, file.toURI(), file);
        if (forceAsProcess) {
            for (SpringCloudServiceDescriptor s: artifact.getServers()) {
                Server ser = s.getServer();
                if (ser instanceof YamlServer) {
                    ((YamlServer) ser).setAsProcess(true);
                }
            }
        }
        
        List<SpringCloudArtifactDescriptor> a = CollectionUtils.toList(artifact);
        mgr.startServers(null, a);
        int running = mgr.getRunningServersCount();
        TimeUtils.sleep(4000);
        mgr.stopServers(a);

        Assert.assertEquals(1, running);
        Assert.assertEquals(0, mgr.getRunningServersCount());
        return mgr;
    }
    
}
