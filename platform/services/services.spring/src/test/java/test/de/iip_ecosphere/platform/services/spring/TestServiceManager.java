/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
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
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactory;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceManager;

/**
 * Tests {@ink SpringCloudServiceManager}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestServiceManager {
    
    /**
     * Tests {@ink SpringCloudServiceManager}.
     * 
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testApp() throws ExecutionException {
        ServiceManager mgr = ServiceFactory.getServiceManager();
        Assert.assertTrue(mgr instanceof SpringCloudServiceManager);
        File f = new File("./target/jars/simpleStream.spring.jar");
        Assert.assertTrue("Test cannot be executed as " + f 
            + " does not exist. Was it downloaded by Maven?", f.exists());
        String aId = mgr.addArtifact(f.toURI());
        ArtifactDescriptor aDesc = mgr.getArtifact(aId);
        // TODO
    }
    
}
