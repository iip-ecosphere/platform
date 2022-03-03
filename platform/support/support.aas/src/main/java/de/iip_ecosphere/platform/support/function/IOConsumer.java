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
 * A consumer that may throw an {@link IOException}.
 * 
 * @param <T> the input argument type
 * @author Holger Eichelberger, SSE
 */
@FunctionalInterface
public interface IOConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param input the input argument
     * @throws IOException if accepting fails for some reason
     */
    void accept(T input) throws IOException;

}
