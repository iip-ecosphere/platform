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

package de.iip_ecosphere.platform.support.aas;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.Server;

/**
 * The implementing counterpart of {@link InvocablesCreator} in terms of a builder.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ProtocolServerBuilder {

    /**
     * Defines a service function.
     * 
     * @param name the name of the operation
     * @param function the implementing function
     * @return <b>this</b>
     * @throws IllegalArgumentException if the operation is already registered
     */
    public ProtocolServerBuilder defineOperation(String name, Function<Object[], Object> function);

    /**
     * Defines a property with getter/setter implementation. Theoretically, either getter/setter
     * may be <b>null</b> for read-only/write-only properties, but this must be, however, reflected in the AAS so that 
     * no wrong can access happens.
     * 
     * @param name the name of the property
     * @param get the supplier providing read access to the property value (may be <b>null</b>)
     * @param set the consumer providing write access to the property value (may be <b>null</b>)
     * @return <b>this</b>
     * @throws IllegalArgumentException if the property is already registered
     */
    public ProtocolServerBuilder defineProperty(String name, Supplier<Object> get, Consumer<Object> set);

    /**
     * Builds the protocol server instance.
     * 
     * @return the server instance
     */
    public Server build();
    
}
