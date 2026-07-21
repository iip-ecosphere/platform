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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

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
     * Tests {@link CollectionUtils#toList(java.util.Iterator)} and {@link CollectionUtils#toList(Iterable)}.
     */
    @Test
    public void testToList() {
        List<String> data = new ArrayList<String>();
        List<String> result = CollectionUtils.toList(data);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        
        data.add("HERE");
        data.add("There");
        result = CollectionUtils.toList(data.iterator());
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(data.size(), result.size());
        Assert.assertEquals(data, result);
        
        List<Object> oResult = CollectionUtils.toList(new StringTokenizer("HERE, There", ", "));
        Assert.assertNotNull(oResult);
        Assert.assertFalse(oResult.isEmpty());
        Assert.assertEquals(data.size(), oResult.size());
        Assert.assertEquals(data, oResult);
    }

    /**
     * Tests {@link CollectionUtils#toSet(java.util.Iterator)} and {@link CollectionUtils#toSet(Iterable)}.
     */
    @Test
    public void testToSet() {
        List<String> data = new ArrayList<String>();
        Set<String> result = CollectionUtils.toSet(data);
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

        Set<Object> oResult = CollectionUtils.toSet(new StringTokenizer("HERE, There", ", "));
        Assert.assertNotNull(oResult);
        Assert.assertFalse(oResult.isEmpty());
        Assert.assertEquals(data.size(), oResult.size());
        for (String s : data) {
            Assert.assertTrue(oResult.contains(s));
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
    
    /**
     * Tests {@link CollectionUtils#reverse(Object[])}.
     */
    @Test
    public void testReverse() {
        CollectionUtils.reverse(null);

        String[] data = new String[0];
        CollectionUtils.reverse(data);
        Assert.assertArrayEquals(new String[] {}, data);
        
        data = new String[] {"a"};
        CollectionUtils.reverse(data);
        Assert.assertArrayEquals(new String[] {"a"}, data);

        data = new String[] {"a", "b"};
        CollectionUtils.reverse(data);
        Assert.assertArrayEquals(new String[] {"b", "a"}, data);
    }
    
    /**
     * Tests {@link CollectionUtils#merge(java.util.Map, java.util.Map)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMerge() {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> db1 = new HashMap<>();
        db1.put("host", "localhost");
        db1.put("port", 5432);
        map1.put("database", db1);
        map1.put("timeout", 30);

        Map<String, Object> map2 = new HashMap<>();
        Map<String, Object> db2 = new HashMap<>();
        db2.put("port", 5433);
        db2.put("user", "admin");
        map2.put("database", db2);
        map2.put("debug", true);

        Map<String, Object> merged = CollectionUtils.merge(map1, map2);
        Assert.assertTrue(merged.containsKey("database"));
        Object o = merged.get("database");
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Map<?, ?>);
        Map<String, Object> om = (Map<String, Object>) o;
        Assert.assertEquals("localhost", om.get("host"));
        Assert.assertEquals(5433, om.get("port"));
        Assert.assertEquals("admin", om.get("user"));
        Assert.assertTrue(merged.containsKey("timeout"));
        Assert.assertEquals(30, merged.get("timeout"));
        Assert.assertTrue(merged.containsKey("debug"));
        Assert.assertEquals(true, merged.get("debug"));
    }
    
    /**
     * Tests {@link CollectionUtils#equals(Object, Object)}.
     */
    @Test
    public void testEquals() {
        Assert.assertTrue(CollectionUtils.equals(null, null));
        Assert.assertFalse(CollectionUtils.equals(null, 1));
        Assert.assertFalse(CollectionUtils.equals(1, null));
        Assert.assertTrue(CollectionUtils.equals(1, 1));
        Assert.assertTrue(CollectionUtils.equals("me", "me"));
        Assert.assertFalse(CollectionUtils.equals("me", "here"));
    }
    
    /**
     * Tests {@link CollectionUtils#contains(Object[], Object)}.
     */
    public void testArrayContains() {
        Assert.assertFalse(CollectionUtils.contains(null, null));
        Assert.assertFalse(CollectionUtils.contains(null, 1));
        Assert.assertFalse(CollectionUtils.contains(new Integer[] {}, 1));
        Assert.assertTrue(CollectionUtils.contains(new Integer[] {2, 1}, 1));
    }
    
    /**
     * Tests {@link CollectionUtils#toArray(List)} and {@link CollectionUtils#toListWithNull(Object[])}.
     */
    @Test
    public void testArrayConversions() {
        Assert.assertNull(CollectionUtils.toListWithNull(null));
        Assert.assertNull(CollectionUtils.toArray(null, Object.class));
        
        String[] arr = {"aa", "bb"};
        List<String> lst = CollectionUtils.toListWithNull(arr);
        Assert.assertNotNull(lst);
        Assert.assertEquals(lst.size(), arr.length);
        for (int i = 0; i < arr.length; i++) {
            Assert.assertEquals(arr[i], lst.get(i));    
        }
        String[] arr2 = CollectionUtils.toArray(lst, String.class);
        Assert.assertArrayEquals(arr, arr2);
    }
   
    /**
     * Tests {@link CollectionUtils#toByteArray(List) and {@link CollectionUtils#addAllBytes(List, byte[])}.
     */
    @Test
    public void testByteArrayConversion() {
        List<Integer> intList = new ArrayList<>();
        intList.add(1);
        intList.add(10);
        byte[] arr = CollectionUtils.toByteArray(intList);
        Assert.assertNotNull(arr);
        Assert.assertEquals(intList.size(), arr.length);
        Assert.assertEquals(intList.get(0).byteValue(), arr[0]);
        Assert.assertEquals(intList.get(1).byteValue(), arr[1]);
        
        Assert.assertNull(CollectionUtils.toByteArray(null));
        
        intList.clear();
        CollectionUtils.addAllBytes(intList, arr);
        Assert.assertEquals(intList.size(), arr.length);
        Assert.assertEquals(intList.get(0).byteValue(), arr[0]);
        Assert.assertEquals(intList.get(1).byteValue(), arr[1]);
        
        CollectionUtils.addAllBytes(null, null);
        CollectionUtils.addAllBytes(intList, null);
        Assert.assertEquals(intList.size(), arr.length);
        CollectionUtils.addAllBytes(null, arr);
    }

}
