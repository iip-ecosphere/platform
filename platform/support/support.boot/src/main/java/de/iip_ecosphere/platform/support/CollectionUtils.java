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

package de.iip_ecosphere.platform.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.iip_ecosphere.platform.support.commons.Commons;

/**
 * Some useful additional collection methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CollectionUtils {

    /**
     * Turns given {@code elements} into a list.
     * 
     * @param <T> the element type
     * @param elements the elements
     * @return the list containing all {@code elements}
     * @see #toListWithNull(Object[])
     */
    @SafeVarargs
    public static <T> List<T> toList(T... elements) {
        List<T> result = new ArrayList<T>();
        for (T e : elements) {
            result.add(e);
        }
        return result;
    }

    /**
     * Turns an array into a list.
     * 
     * @param <T> the element type
     * @param elements the array to convert, may be <b>null</b>
     * @return the converted list, may be <b>null</b> if {@code elements} was <b>null</b>
     * @see #toList(Object...)
     */
    public static <T> List<T> toListWithNull(T[] elements) {
        return null == elements ? null : toList(elements);
    }
    
    /**
     * Converts a number list to a byte array.
     * 
     * @param list the list to be converted, may be <b>null</b>
     * @return the converted array, may be <b>null</b> if {@code list} was <b>null</b>
     */
    public static byte[] toByteArray(List<? extends Number> list) {
        byte[] result = null;
        if (null != list) {
            result = new byte[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = list.get(i).byteValue();
            }
        }
        return result;
    }
    
    /**
     * Adds all values from {@code data} to {@code list}.
     * 
     * @param list the list to modify, may be <b>null</b> for no action
     * @param data the array to copy from, may be <b>null</b> for no action
     * @return {@code list}
     */
    public static List<Integer> addAllBytes(List<Integer> list, byte[] data) {
        if (null != list && null != data) {
            for (int i = 0; i < data.length; i++) {
                list.add(Integer.valueOf(data[i]));
            }
        }
        return list;
    }

    /**
     * Turns a list into an array.
     * 
     * @param <T> the element type
     * @param list the list to convert, may be <b>null</b>
     * @return the converted array, may be <b>null</b> if {@code list} was null
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(List<T> list, Class<T> cls) {
        T[] result;
        if (list != null) {
            result = (T[]) java.lang.reflect.Array.newInstance(cls, list.size());
            list.toArray(result);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Turns given {@code elements} into a list.
     * 
     * @param <T> the element type
     * @param elements the elements
     * @return the list containing all {@code elements}
     */
    @SafeVarargs
    public static <T> Set<T> toSet(T... elements) {
        Set<T> result = new HashSet<T>();
        for (T e : elements) {
            result.add(e);
        }
        return result;
    }

    /**
     * Turns given {@code elements} to {@code list}.
     * 
     * @param <T> the element type
     * @param list the list to be modified as a side effect
     * @param elements the elements
     * @return the list with all {@code elements} added
     */
    @SafeVarargs
    public static <T> List<T> addAll(List<T> list, T... elements) {
        for (T e : elements) {
            list.add(e);
        }
        return list;
    }
    
    /**
     * Turns given {@code elements} to {@code set}.
     * 
     * @param <T> the element type
     * @param set the set to be modified as a side effect
     * @param elements the elements
     * @return the set with all {@code elements} added
     */
    @SafeVarargs
    public static <T> Set<T> addAll(Set<T> set, T... elements) {
        for (T e : elements) {
            set.add(e);
        }
        return set;
    }
    
    /**
     * Turns the elements in the iterable into a list.
     * 
     * @param <T> the element type
     * @param iterable the iterable
     * @return the list with all elements in {@code iterable}
     */
    public static <T> List<T> toList(Iterable<T> iterable) {
        return toList(iterable.iterator());
    }
    
    /**
     * Turns the elements in the iterator into a list.
     * 
     * @param <T> the element type
     * @param iterator the iterator
     * @return the list with all elements in {@code iterator}
     */
    public static <T> List<T> toList(Iterator<T> iterator) {
        List<T> result = new ArrayList<T>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    /**
     * Turns the elements in the enumeration into a list.
     * 
     * @param <T> the element type
     * @param en the enumeration
     * @return the list with all elements in {@code en}
     */
    public static <T> List<T> toList(Enumeration<T> en) {
        List<T> result = new ArrayList<T>();
        while (en.hasMoreElements()) {
            result.add(en.nextElement());
        }
        return result;
    }

    /**
     * Turns the elements in the iterable into a set.
     * 
     * @param <T> the element type
     * @param iterable the iterable
     * @return the set with all elements in {@code iterable}
     */
    public static <T> Set<T> toSet(Iterable<T> iterable) {
        return toSet(iterable.iterator());
    }

    /**
     * Turns the elements in the iterator into a set.
     * 
     * @param <T> the element type
     * @param iterator the iterator
     * @return the set with all elements in {@code iterator}
     */
    public static <T> Set<T> toSet(Iterator<T> iterator) {
        Set<T> result = new HashSet<T>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    /**
     * Turns the elements in the enumeration into a set.
     * 
     * @param <T> the element type
     * @param en the enumeration
     * @return the set with all elements in {@code en}
     */
    public static <T> Set<T> toSet(Enumeration<T> en) {
        Set<T> result = new HashSet<T>();
        while (en.hasMoreElements()) {
            result.add(en.nextElement());
        }
        return result;
    }

    /**
     * Turns a collection into a string with configurable lead-in, separator and lead-out.
     * 
     * @param collection the collection
     * @param leadIn the lead-in
     * @param leadOut the lead-out
     * @param separator the separator
     * @return the string representation
     */
    public static String toString(Collection<?> collection, String leadIn, String leadOut, String separator) {
        String result = leadIn;
        boolean first = true;
        for (Object o: collection) {
            if (!first) {
                result += separator;
            }
            result += o.toString();
            first = false;
        }
        return result + leadOut;
    }

    /**
     * Turns a collection into a string with no lead-in, one space as separator and no lead-out.
     * 
     * @param collection the collection
     * @return the string representation
     */
    public static String toStringSpaceSeparated(Collection<?> collection) {
        return toString(collection, "", "", " ");
    }
    
    /**
     * Reverses the order of the given array. There is no special handling for multi-dimensional arrays. This method 
     * does nothing for a <b>null</b> input array.
     *
     * @param array  the array to reverse, may be <b>null</b>
     */
    public static void reverse(final Object[] array) {
        Commons.getInstance().reverse(array);
    }
    
    /**
     * Merges source into target recursively.
     *
     * If a key exists in both maps:
     * a) If both values are Map&lt;String, Object&gt;, they are merged recursively.
     * b) Otherwise, the value from source replaces the one in target.
     *
     * @param <K> the key type (assumed to be homogeneous even in sub maps)
     * @param <V> the value type (assumed to be homogeneous even in sub maps)
     * @param target the map to merge into (modified in place)
     * @param source the map to merge from
     * @return the target map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> merge(Map<K, V> target, Map<K, V> source) {
        for (Map.Entry<K, V> entry : source.entrySet()) {
            K key = entry.getKey();
            V sourceValue = entry.getValue();
            V targetValue = target.get(key);
            if (targetValue instanceof Map<?, ?> && sourceValue instanceof Map<?, ?>) {
                merge((Map<K, V>) targetValue, (Map<K, V>) sourceValue);
            } else {
                target.put(key, sourceValue);
            }
        }
        return target;
    }    
    
    /**
     * Returns whether two objects are equal even if they are <b>null</b>.
     * 
     * @param o1 the first object
     * @param o2 the second object
     * @return {@code true} for equal or both are <b>null</b>, {@code false} for different
     */
    public static boolean equals(Object o1, Object o2) {
        return o1 == null ? o1 == o2 : o1.equals(o2);
    }

    /**
     * Returns whether {@code element} is in {@code array}, compared by equality.
     * 
     * @param <T> the element type
     * @param array the array, may be <b>null</b>
     * @param element the element, may be <b>null</b> 
     * @return {@code true} for containment, {@code false} else
     */
    public static <T> boolean contains(T[] array, T element) {
        boolean found = false;
        if (null != array) {
            for (int i = 0; !found && i < array.length; i++) {
                found |=  equals(array[i], element);
            }
        }
        return found;
    }
    
}
