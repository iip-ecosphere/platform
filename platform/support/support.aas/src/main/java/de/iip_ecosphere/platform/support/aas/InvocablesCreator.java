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

import java.util.function.Function;

import de.iip_ecosphere.platform.support.function.IOSupplier;
import de.iip_ecosphere.platform.support.logging.LogLevel;

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
    public static final Invokable WRITE_ONLY = Property.PropertyBuilder.WRITE_ONLY;

    /**
     * A setter implementation that does nothing. [convenience]
     */
    public static final Invokable READ_ONLY = Property.PropertyBuilder.READ_ONLY;

    /**
     * A creator that does not return invocables. Shall only be used in situations where <b>null</b>
     * instead of the creator is not feasible.
     */
    public static final InvocablesCreator NULL_CREATOR = new InvocablesCreator() {

        @Override
        public Invokable createGetter(String name) {
            return null;
        }

        @Override
        public Invokable createSetter(String name) {
            return null;
        }

        @Override
        public Invokable createInvocable(String name) {
            return null;
        }
        
    };
    
    /**
     * Creates a getter implementation for a property.
     * Use {@link #WRITE_ONLY} if no getter is intended but also the value shall not be held locally in the property.
     * 
     * @param name the unique name of the property
     * @return the getter implementation
     */
    public Invokable createGetter(String name);

    /**
     * Creates a setter implementation for a property.
     * Use {@link #READ_ONLY} if no setter is intended but also the value shall not be held locally in the property.
     * 
     * @param name the unique name of the property
     * @return the setter implementation
     */
    public Invokable createSetter(String name);
    
    /**
     * Creates an invokable for an operation.
     * 
     * @param name the unique name of the property
     * @return the function implementation
     */
    public Invokable createInvocable(String name);
    
    /**
     * Create an executable creator, i.e., a creator that truly returns executable invokables in BaSyX 1 style.
     * Depending on the implementation, a specific creator implementation my be needed that explicitly resolves
     * the given AAS/element path, obtains the elements and the performs the respective access.
     * 
     * @param aasSupplier supplies the AAS where to start at
     * @param elementPath supplies the element path within the AAS
     * @param unqualifier a function turning invokable names to unqualified names, may be <b>null</b> for none; 
     *     qualification is replaced here by {@code elementPath}
     * @return the creator, by default <b>this</b>
     */
    public default InvocablesCreator executableCreator(IOSupplier<Aas> aasSupplier, String[] elementPath, 
        Function<String, String> unqualifier) {
        return this;
    }

    /**
     * Instructs the implementation that logging messages by the creator and its created instances shall be on the 
     * given level.
     * 
     * @param level the logging level
     */
    public default void setLogLevel(LogLevel level) {
    }
    
}
