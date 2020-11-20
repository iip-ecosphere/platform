/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package de.iip_ecosphere.platform.transport;

import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.impl.DirectMemoryTransferTransportConnector;

/**
 * A factory for creating transport connector instances. This factory shall
 * ensure that the entire platform runs with the same connector instances,
 * however, provides at the same time the flexibility to exchange the creation
 * process. As there is no default connector in this artifact anymore, the factory 
 * must be configured by a {@link TransportFactoryImplementation} before use. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportFactory {

    /**
     * Defines an internal factory implementation to create connectors.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface TransportFactoryImplementation {

        /**
         * Creates a connector instance.
         * 
         * @return the created connector instance
         */
        public TransportConnector createConnector();

        /**
         * Creates an inter-process connector.
         * 
         * @return the created connector instance
         */
        public TransportConnector createIpcConnector();

        /**
         * Creates a direct memory transfer connector instance.
         * 
         * @return the direct memory connector instance
         */
        public TransportConnector createDirectMemoryConnector();
        
    }

    /**
     * Provides a basic implementation factory. Here, inter-process connector creation is delegated to 
     * {@link #createConnector()} and the direct memory connector goes for the default implementation 
     * {@link DirectMemoryTransferTransportConnector}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public abstract static class BaseFactoryImplementation implements TransportFactoryImplementation {

        @Override
        public TransportConnector createIpcConnector() {
            return createConnector();
        }

        @Override
        public TransportConnector createDirectMemoryConnector() {
            return new DirectMemoryTransferTransportConnector();
        }
        
    }
    
    /**
     * The default factory implementation (to be able to return to this instance if needed).
     */
    private static final TransportFactoryImplementation DEFAULT = new BaseFactoryImplementation() {

        @Override
        public TransportConnector createConnector() {
            return new DirectMemoryTransferTransportConnector();
        }
        
    };
    
    private static TransportFactoryImplementation instance = DEFAULT;

    /**
     * Changes the factory implementation. May be replaced by an injection-based
     * mechanism, so far required for testing.
     * 
     * @param inst the factory implementation instance (ignored if <b>null</b>)
     * @return the factory implementation instance before calling this method
     */
    public static TransportFactoryImplementation setFactoryImplementation(TransportFactoryImplementation inst) {
        TransportFactoryImplementation old = instance;
        if (null != inst) {
            instance = inst;
        }
        return old;
    }

    /**
     * Creates a connector instance.
     * 
     * @return the created connector instance
     */
    public static TransportConnector createConnector() {
        return instance.createConnector();
    }
    
    /**
     * Creates an inter-process connector.
     * 
     * @return the created connector instance
     */
    public static TransportConnector createIpcConnector() {
        return instance.createIpcConnector();
    }
    
    /**
     * Creates a direct memory transfer connector instance.
     * 
     * @return the direct memory connector instance
     */
    public static TransportConnector createDirectMemoryConnector() {
        return instance.createDirectMemoryConnector();
    }

}
