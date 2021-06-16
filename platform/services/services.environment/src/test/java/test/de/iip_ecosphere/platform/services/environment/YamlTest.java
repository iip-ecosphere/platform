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

package test.de.iip_ecosphere.platform.services.environment;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.services.environment.YamlService;

/**
 * Tests reading a (more complex) YAML service deployment descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlTest {

    /**
     * Tests the basic YAML service descriptor reading.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testYaml() throws IOException {
        YamlArtifact art = YamlArtifact.readFromYamlSafe(YamlTest.class.getResourceAsStream("/deployment.yml"));
        Assert.assertEquals("art", art.getId());
        Assert.assertEquals("simpleStream.spring", art.getName());
        Assert.assertEquals("0.1.9", art.getVersion().toString());
        Assert.assertNotNull(art.getServices());
        Assert.assertEquals(2, art.getServices().size());
        YamlService service = art.getServices().get(0);

        Assert.assertEquals("simpleStream-create", service.getId());
        Assert.assertEquals("create", service.getName());
        Assert.assertEquals("0.2.0", service.getVersion().toString());
        Assert.assertEquals(ServiceKind.SOURCE_SERVICE, service.getKind());
        Assert.assertTrue(service.isDeployable());
    }
    
}
