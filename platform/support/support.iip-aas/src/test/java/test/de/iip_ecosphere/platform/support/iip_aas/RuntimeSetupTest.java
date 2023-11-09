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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.config.RuntimeSetup;
import org.junit.Assert;

/**
 * Tests {@link RuntimeSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RuntimeSetupTest {
    
    /**
     * Tests the runtime setup reading/writing.
     */
    @Test
    public void testRuntimeSetup() {
        RuntimeSetup instance = new RuntimeSetup();
        testRuntimeSetup(instance);
        instance.setAasRegistry("http://localhost:8080");
        instance.setAasRegistry("http://localhost:8081");
    }
    
    /**
     * Tests the runtime setup with the {@code expected} data.
     * 
     * @param expected the expected data
     */
    private void testRuntimeSetup(RuntimeSetup expected) {
        File file = RuntimeSetup.getFile();
        FileUtils.deleteQuietly(file);
        
        expected.store();
        Assert.assertTrue(file.exists());
        RuntimeSetup actual = RuntimeSetup.load();
        Assert.assertNotNull(actual);
        Assert.assertEquals(expected.getAasRegistry(), actual.getAasRegistry());
        Assert.assertEquals(expected.getAasServer(), actual.getAasServer());
        
        FileUtils.deleteQuietly(file);
    }

}
