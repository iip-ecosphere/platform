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

package test.de.iip_ecosphere.platform.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.yaml.YamlFile;

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
        YamlFile.read(ResourceLoader.getResourceAsStream("nameplate.yml"));
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
        YamlFile.getFieldAsMap(data, bindingsFieldPath); // no impl
        Assert.assertEquals("a", YamlFile.asString(null, "a"));
        Assert.assertEquals("1", YamlFile.asString(1, "a"));
        Assert.assertNotNull(YamlFile.asList(null));
        final ArrayList<Object> testList = new ArrayList<>();
        Assert.assertTrue(testList == YamlFile.asList(testList));
        new YamlFileExtended(); // for constructor coverage;
        YamlFile.getFieldAsString(data, bindingsPath, bindingsPath); // no impl
        YamlFile.getFieldAsList(data, bindingsPath); // no impl
        
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        Map<String, Object> bMap = new HashMap<>();
        bMap.put("b1", "abc");
        bMap.put("b2", new HashMap<>());
        map.put("b", bMap);
        YamlFile.overwrite(map, Object.class, map);
    }
    
    /**
     * For constructor coverage.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class YamlFileExtended extends YamlFile {
        
    }

    /**
     * A basic test type.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BasicTestType {
        
        private int iValue;
        
        // public no-arg constructor

        /**
         * Returns iValue. 
         * 
         * @return iValue the new value
         */
        public int getIValue() {
            return iValue;
        }

        /**
         * Sets iValue. [required by Snakeyaml]
         * 
         * @param iValue the new value
         */
        public void setIValue(int iValue) {
            this.iValue = iValue;
        }
        
    }

    /**
     * A refined test type. Snakeyaml failed in something like this.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TestType extends BasicTestType {
        
    }
    
    /**
     * Tests {@link YamlFile#fixList(List, Class)} and {@link YamlFile#fixListSafe(List, Class)}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testFixList() {
        Assert.assertNull(YamlFile.fixListSafe(null, TestType.class));
        List<TestType> list = new ArrayList<>();
        Assert.assertEquals(list, YamlFile.fixListSafe(list, TestType.class));
        
        Map<Object, Object> oMap = new HashMap<>();
        oMap.put("iValue", 10);
        List<Object> oList = new ArrayList<>();
        oList.add(oMap);
        list = (List) oList; // wrong, but snakeyaml does something like that, probably via reflection
        list = YamlFile.fixListSafe(list, TestType.class);
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.get(0) instanceof TestType);
        Assert.assertEquals(10, list.get(0).getIValue());
    }

    /**
     * Tests {@link YamlFile#getMap(Map, String...)}.
     */
    @Test
    public void testGetMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", 1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("obj", map);
        Map<String, Object> map3 = new HashMap<>();
        map3.put("obj", map2);
        Assert.assertNull(YamlFile.getMap(map3));
        Assert.assertNull(YamlFile.getMap(map3, "test"));
        Assert.assertNotNull(YamlFile.getMap(map3, "obj"));
        Assert.assertNotNull(YamlFile.getMap(map3, "obj", "obj"));
        Assert.assertNotNull(YamlFile.getMap(map3, "obj", "obj", "key"));
    }

    /**
     * Extended test type.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ExtendedTestType extends BasicTestType {
        
        private BasicTestType nested;

        /**
         * Returns the nested object.
         * 
         * @return the nested object
         */
        public BasicTestType getNested() {
            return nested;
        }

        /**
         * Changes the nested object.
         * 
         * @param nested the nested object to set
         */
        public void setNested(BasicTestType nested) {
            this.nested = nested;
        }
        
    }

}
