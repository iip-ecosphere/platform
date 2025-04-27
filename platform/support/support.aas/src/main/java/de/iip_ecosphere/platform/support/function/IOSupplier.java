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
 * A supplier that may throw an {@link IOException}.
 * 
 * @param <T> the type of results supplied by this supplier
 * @author Holger Eichelberger, SSE
 */
@FunctionalInterface
public interface IOSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws IOException if accepting fails for some reason
     */
    T get() throws IOException;

}
