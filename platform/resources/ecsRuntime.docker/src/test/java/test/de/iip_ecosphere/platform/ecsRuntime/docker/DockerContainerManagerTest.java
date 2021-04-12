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

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerManager;

/**
 * Template test.
 * 
 * @author Monika Staciwa, SSE
 */
public class DockerContainerManagerTest {
    
    /**
     * Template test.
     */
    @Test
    public void testContainerManager() {
        ContainerManager cm = EcsFactory.getContainerManager();
        Assert.assertTrue(cm instanceof DockerContainerManager);
        // TODO go on testing with cm
        
    }
    
}
