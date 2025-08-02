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
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

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
        ArrayUtils.reverse(array);
    }
    
}
