/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.plugins;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A compound enumeration.
 * 
 * @param <E> the element type
 * @author SUN/Oracle (sun.misc)
 */
public class CompoundEnumeration<E> implements Enumeration<E> {

    private Enumeration<E>[] enums;
    private int index = 0;

    /**
     * Creates a compound enumeration.
     * 
     * @param enums the enumerations to enumerated over.
     */
    public CompoundEnumeration(Enumeration<E>[] enums) {
        this.enums = enums;
    }

    /**
     * Determines the next element and returns whether it exists.
     * 
     * @return does the next element exist
     */
    private boolean next() {
        while (index < enums.length) {
            if (enums[index] != null && enums[index].hasMoreElements()) {
                return true;
            }
            index++;
        }
        return false;
    }

    @Override
    public boolean hasMoreElements() {
        return next();
    }

    @Override
    public E nextElement() {
        if (!next()) {
            throw new NoSuchElementException();
        }
        return enums[index].nextElement();
    }
}