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

package test.de.iip_ecosphere.platform.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.CollectionUtils;

/**
 * Tests {@link CollectionUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CollectionUtilsTest {

    /**
     * Tests {@link CollectionUtils#toList(java.util.Iterator)}.
     */
    @Test
    public void testToList() {
        List<String> data = new ArrayList<String>();
        List<String> result = CollectionUtils.toList(data.iterator());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        
        data.add("HERE");
        data.add("There");
        result = CollectionUtils.toList(data.iterator());
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(data.size(), result.size());
        Assert.assertEquals(data, result);
    }

    /**
     * Tests {@link CollectionUtils#toSet(java.util.Iterator)}.
     */
    @Test
    public void testToSet() {
        List<String> data = new ArrayList<String>();
        Set<String> result = CollectionUtils.toSet(data.iterator());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        
        data.add("HERE");
        data.add("There");
        result = CollectionUtils.toSet(data.iterator());
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(data.size(), result.size());
        for (String s : data) {
            Assert.assertTrue(result.contains(s));
        }
        
        result = CollectionUtils.toSet("1", "2", "2");
        Assert.assertTrue(result.contains("1"));
        Assert.assertTrue(result.contains("2"));
        Assert.assertEquals(2, result.size());
    }
    
    /**
     * Tests {@link CollectionUtils#toList(Object...)}.
     */
    @Test
    public void testToListVarArg() {
        List<String> tmp = CollectionUtils.toList();
        Assert.assertNotNull(tmp);
        Assert.assertEquals(0, tmp.size());
        
        tmp = CollectionUtils.toList("a");
        Assert.assertNotNull(tmp);
        Assert.assertEquals(1, tmp.size());
        Assert.assertEquals("a", tmp.get(0));

        tmp = CollectionUtils.toList("a", "b");
        Assert.assertNotNull(tmp);
        Assert.assertEquals(2, tmp.size());
        Assert.assertEquals("a", tmp.get(0));
        Assert.assertEquals("b", tmp.get(1));
    }

    /**
     * Tests {@link CollectionUtils#addAll(List, Object...)}.
     */
    @Test
    public void testAddListAllVarArg() {
        List<String> tmp = new ArrayList<String>();
        List<String> res = CollectionUtils.addAll(tmp);
        Assert.assertNotNull(res);
        Assert.assertTrue(res == tmp);
        Assert.assertEquals(0, tmp.size());
        
        res = CollectionUtils.addAll(tmp, "a");
        Assert.assertNotNull(res);
        Assert.assertTrue(res == tmp);
        Assert.assertEquals(1, tmp.size());
        Assert.assertEquals("a", tmp.get(0));

        res = CollectionUtils.addAll(tmp, "a", "b");
        Assert.assertNotNull(res);
        Assert.assertTrue(res == tmp);
        Assert.assertEquals(3, tmp.size());
        Assert.assertEquals("a", tmp.get(0));
        Assert.assertEquals("a", tmp.get(1));
        Assert.assertEquals("b", tmp.get(2));
    }

    /**
     * Tests {@link CollectionUtils#addAll(Set, Object...)}.
     */
    @Test
    public void testAddSetAllVarArg() {
        Set<String> tmp = new HashSet<String>();
        Set<String> res = CollectionUtils.addAll(tmp);
        Assert.assertNotNull(res);
        Assert.assertTrue(res == tmp);
        Assert.assertEquals(0, tmp.size());
        
        res = CollectionUtils.addAll(tmp, "a");
        Assert.assertNotNull(res);
        Assert.assertTrue(res == tmp);
        Assert.assertEquals(1, tmp.size());
        Assert.assertTrue(tmp.contains("a"));

        res = CollectionUtils.addAll(tmp, "a", "b");
        Assert.assertNotNull(res);
        Assert.assertTrue(res == tmp);
        Assert.assertEquals(2, tmp.size()); // "a" is duplicate
        Assert.assertTrue(tmp.contains("a"));
        Assert.assertTrue(tmp.contains("b"));
    }
    
    /**
     * Tests {@link CollectionUtils#toString(java.util.Collection, String, String, String)} and 
     * {@link CollectionUtils#toStringSpaceSeparated(java.util.Collection)}.
     */
    @Test
    public void testToString() {
        List<Integer> list = CollectionUtils.toList();
        Assert.assertEquals("", CollectionUtils.toStringSpaceSeparated(list));
        Assert.assertEquals("[]", CollectionUtils.toString(list, "[", "]", ", "));
        
        list = CollectionUtils.toList(1);
        Assert.assertEquals("1", CollectionUtils.toStringSpaceSeparated(list));
        Assert.assertEquals("[1]", CollectionUtils.toString(list, "[", "]", ", "));
        
        list = CollectionUtils.toList(1, 2);
        Assert.assertEquals("1 2", CollectionUtils.toStringSpaceSeparated(list));
        Assert.assertEquals("[1, 2]", CollectionUtils.toString(list, "[", "]", ", "));
    }

}
