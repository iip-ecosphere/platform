package de.iip_ecosphere.platform.libs.ads;

import java.io.IOException;

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

/**
 * Returns memory sizes for a certain type.
 * 
 * @param <T> the type
 * @author Holger Eichelberger, SSE
 * @author Alexander Weber, SSE
 */
public interface MemorySizeCalculator<T> {

    /**
     * Determines the ADS memory size of {@code value} with type {@code cls}.
     *  
     * @param value the value to determine the memory size for
     * @return the memory size
     * @throws IOException if value contains elements that cannot be written
     */
    public int determineMemorySize(T value) throws IOException;
    
    /**
     * Shortcut to get the size of on element of the object.
     * 
     * @return The bytes a part of this object takes up
     */
    public int getMemSize();

}