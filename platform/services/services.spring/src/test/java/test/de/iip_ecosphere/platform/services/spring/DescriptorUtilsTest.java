/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.spring.DescriptorUtils;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;
import de.iip_ecosphere.platform.services.spring.SpringInstances;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Tests uncovered aspects of {@link DescriptorUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DescriptorUtilsTest {

    /**
     * Tests reading a service deployment descriptor from classpath.
     *  
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testReadFromClasspath() throws ExecutionException {
        SpringCloudServiceSetup setup = SpringInstances.getConfig();
        SpringCloudServiceSetup testSetup = new SpringCloudServiceSetup();
        testSetup.setDescriptorName("test.yml");
        SpringInstances.setConfig(testSetup);
        
        YamlArtifact art = DescriptorUtils.readFromClasspath();
        Assert.assertNotNull(art);

        SpringInstances.setConfig(setup);
    }
    
    /**
     * Tests creating standalone command line arguments.
     *  
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testCreateStandaloneCommandArgs() throws ExecutionException {
        File f = new File("src/main/resources/void.jar");
        try {
            DescriptorUtils.createStandaloneCommandArgs(f, 6234, "localhost", 6235, "");
            Assert.fail("No exception thrown");
        } catch (IOException e) {
            // exception ok
        }
        f = new File("src/main/resources/descriptorUtilsTest.jar");
        try {
            List<String> args = DescriptorUtils.createStandaloneCommandArgs(f, 6234, "localhost", 6235, "");
            Assert.assertNotNull(args);
            Assert.assertTrue(args.size() > 0);
        } catch (IOException e) {
            // exception ok
        }
    } 
    
    /**
     * Tries loading a resource from BOOT-INF/classes (in resources folder), descriptor comes via 
     * services.environment.spring.
     */
    @Test
    public void testResourceLoader() {
        InputStream in = ResourceLoader.getResourceAsStream("spring.test.txt");
        Assert.assertNotNull(in);
    }
    
}
