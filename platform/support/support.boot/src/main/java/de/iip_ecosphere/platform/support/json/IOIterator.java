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

package de.iip_ecosphere.platform.support.json;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * An iterator that can throw {@link IOException}.
 * 
 * @param <T> the type of element
 * @author Holger Eichelberger, SSE
 */
public interface IOIterator<T> {

    /**
     * Returns {@code true} if the iteration has more elements.
     *
     * @return {@code true} if the iteration has more elements
     * @throws IOException if providing the next element caused an I/O problem
     */
    public boolean hasNext() throws IOException;

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     * @throws IOException if checking for the next element caused an I/O problem
     */
    public T next() throws IOException;
    
}