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

import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;

/**
 * Creates invocables for AAS, e.g., for a remote protocol. This interface just creates instances, i.e., it is
 * more a factory than a builder. For local direct calls, you may just use lambda expressions. The counterpart
 * is {@link ProtocolServerBuilder}.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface InvocablesCreator {

    /**
     * A getter implementation that does nothing. [convenience]
     */
    public static final Supplier<Object> WRITE_ONLY = Property.PropertyBuilder.WRITE_ONLY;

    /**
     * A setter implementation that does nothing. [convenience]
     */
    public static final Consumer<Object> READ_ONLY = Property.PropertyBuilder.READ_ONLY;

    /**
     * Creates a getter implementation for a property, i.e., for {@link PropertyBuilder#bind(Supplier, Consumer)}.
     * Use {@link #WRITE_ONLY} if no getter is intended but also the value shall not be held locally in the property.
     * 
     * @param name the unique name of the property
     * @return the getter implementation
     */
    public Supplier<Object> createGetter(String name);

    /**
     * Creates a setter implementation for a property, i.e., for {@link PropertyBuilder#bind(Supplier, Consumer)}.
     * Use {@link #READY_ONLY} if no setter is intended but also the value shall not be held locally in the property.
     * 
     * @param name the unique name of the property
     * @return the setter implementation
     */
    public Consumer<Object> createSetter(String name);
    
    /**
     * Creates an invocable for an operation, i.e., for {@link OperationBuilder#setInvocable(Function)}.
     * 
     * @param name the unique name of the property
     * @return the function implementation
     */
    public Function<Object[], Object> createInvocable(String name);
    
}
