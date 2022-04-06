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

import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.spring.DescriptorUtils;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;
import de.iip_ecosphere.platform.services.spring.SpringInstances;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;

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
    
}
