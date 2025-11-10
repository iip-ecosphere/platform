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

package de.iip_ecosphere.platform.support.function;

import java.io.IOException;

/**
 * A simple (optional) function that may throw an {@link IOException}.
 * 
 * @author Holger Eichelberger, SSE
 */
@FunctionalInterface
public interface IOFunction<T, R> {
    
    /**
     * Applies this function to the given argument.
     *
     * @param arg the function argument
     * @return the function result
     * @throws IOException if an I/O error occurs.
     */
    R apply(final T arg) throws IOException;

}
