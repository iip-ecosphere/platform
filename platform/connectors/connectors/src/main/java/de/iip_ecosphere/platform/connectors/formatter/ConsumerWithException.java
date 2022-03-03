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

package de.iip_ecosphere.platform.connectors.formatter;

import java.io.IOException;

import de.iip_ecosphere.platform.support.function.IOConsumer;

/**
 * Specific consumer that throws IO exceptions.
 * 
 * @param <T> the type of data produced by parsing
 * @author Holger Eichelberger, SSE
 */
public interface ConsumerWithException<T> extends IOConsumer<T> {
    
    /**
     * Consumes an intermediary type for output producing.
     * 
     * @param data the intermediary data
     * @throws IOException if errors during I/O occur
     */
    default void consume(T data) throws IOException {
        accept(data); // changed for migration to IOConsumer
    }

}
