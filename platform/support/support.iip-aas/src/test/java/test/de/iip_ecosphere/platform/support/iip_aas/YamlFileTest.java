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

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.config.YamlFile;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Tests {@link YamlFile}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlFileTest {

    /**
     * Tests {@link YamlFile}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testYamlFile() throws IOException {
        Object data = YamlFile.read(ResourceLoader.getResourceAsStream("nameplate.yml"));
        Map<Object, Object> map = YamlFile.asMap(null);
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
        
        List<Object> list  = YamlFile.getFieldAsList(data, "services");
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        
        map = YamlFile.asMap(data);
        Assert.assertNotNull(map);
        Assert.assertFalse(map.isEmpty());

        list = YamlFile.asList(null);
        Assert.assertNotNull(list);
        Assert.assertTrue(list.isEmpty());

        data = YamlFile.asString(null, "");
        Assert.assertNotNull(data);
        Assert.assertEquals("", data);
    }

    /**
     * Tests {@link YamlFile} with a Spring setup.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testYamlFileSpring() throws IOException {
        Object data = YamlFile.read(ResourceLoader.getResourceAsStream("YamlFile.yml"));
        final String bindingsPath = "spring.cloud.stream.bindings";
        final String[] bindingsFieldPath = bindingsPath.split("\\.");
        Map<Object, Object> tmp = YamlFile.getFieldAsMap(data, bindingsFieldPath);
        boolean found = false;
        for (Map.Entry<Object, Object> ent : tmp.entrySet()) {
            String dest = YamlFile.getFieldAsString(ent.getValue(), "destination", null);
            Assert.assertNotNull(dest);
            found = true;
        }
        Assert.assertTrue(found);
    }

}
