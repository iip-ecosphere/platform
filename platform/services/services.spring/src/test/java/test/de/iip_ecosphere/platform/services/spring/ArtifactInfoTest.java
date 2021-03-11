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

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.spring.yaml.ArtifactInfo;
import de.iip_ecosphere.platform.services.spring.yaml.ServiceInfo;

/**
 * Tests {@link ArtifactInfo} and {@link ServiceInfo}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArtifactInfoTest {

    /**
     * Tests the yaml reader.
     */
    @Test
    public void testYaml() {
        ArtifactInfo info = ArtifactInfo.readFromYaml(null);
        Assert.assertTrue(info.getServices().isEmpty());
        
        info = ArtifactInfo.readFromYaml(getClass().getClassLoader().getResourceAsStream("test.yml"));
        Assert.assertEquals("art", info.getId());
        Assert.assertEquals("art-name", info.getName());
        Assert.assertFalse(info.getServices().isEmpty());
        Assert.assertEquals(2, info.getServices().size());
        
        ServiceInfo service = info.getServices().get(0);
        Assert.assertEquals("id-0", service.getId());
        Assert.assertEquals("name-0", service.getName());
        Assert.assertEquals("1.0.2", service.getVersion());
        Assert.assertEquals("desc desc-0", service.getDescription());
        Assert.assertEquals(2, service.getCmdArg().size());
        Assert.assertEquals("arg-0-1", service.getCmdArg().get(0));
        Assert.assertEquals("arg-0-2", service.getCmdArg().get(1));
        Assert.assertEquals(0, service.getDependencies().size());
        
        service = info.getServices().get(1);
        Assert.assertEquals("id-1", service.getId());
        Assert.assertEquals("name-1", service.getName());
        Assert.assertEquals("1.0.3", service.getVersion());
        Assert.assertEquals("desc desc-1", service.getDescription());
        Assert.assertEquals(0, service.getCmdArg().size());
        Assert.assertEquals(1, service.getDependencies().size());
        Assert.assertEquals("id-0", service.getDependencies().get(0).getId());
    }
    
}
