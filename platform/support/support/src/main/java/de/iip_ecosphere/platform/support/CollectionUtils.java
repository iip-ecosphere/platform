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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    
}
