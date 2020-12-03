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
 * must be configured by instances of {@link ConnectorCreator} before use. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportFactory {

    /**
     * Defines an internal factory implementation to create connectors.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ConnectorCreator {

        /**
         * Creates a connector instance.
         * 
         * @return the created connector instance
         */
        public TransportConnector createConnector();

        /**
         * Returns the descriptive name of the connector.
         *   
         * @return the name
         */
        public String getName();
        
    }
    
    /**
     * The default factory implementation (to be able to return to this instance if needed).
     */
    private static final ConnectorCreator DEFAULT = new ConnectorCreator() {

        @Override
        public TransportConnector createConnector() {
            return new DirectMemoryTransferTransportConnector();
        }

        @Override
        public String getName() {
            return DirectMemoryTransferTransportConnector.NAME;
        }
        
    };
    
    private static ConnectorCreator mainCreator = DEFAULT;
    
    private static ConnectorCreator ipcCreator = DEFAULT;
    
    private static ConnectorCreator dmCreator = DEFAULT;

    /**
     * Changes the main factory implementation. May be replaced by an injection-based
     * mechanism, so far required for testing.
     * 
     * @param inst the factory implementation instance (ignored if <b>null</b>)
     * @return the factory implementation instance before calling this method
     */
    public static ConnectorCreator setMainImplementation(ConnectorCreator inst) {
        ConnectorCreator old = mainCreator;
        if (null != inst) {
            mainCreator = inst;
        }
        return old;
    }
    
    /**
     * Changes the inter-process factory implementation. May be replaced by an injection-based
     * mechanism, so far required for testing.
     * 
     * @param inst the factory implementation instance (ignored if <b>null</b>)
     * @return the factory implementation instance before calling this method
     */
    public static ConnectorCreator setIpcImplementation(ConnectorCreator inst) {
        ConnectorCreator old = ipcCreator;
        if (null != inst) {
            ipcCreator = inst;
        }
        return old;
    }
    
    /**
     * Changes the direct memory factory implementation. May be replaced by an injection-based
     * mechanism, so far required for testing.
     * 
     * @param inst the factory implementation instance (ignored if <b>null</b>)
     * @return the factory implementation instance before calling this method
     */
    public static ConnectorCreator setDmImplementation(ConnectorCreator inst) {
        ConnectorCreator old = dmCreator;
        if (null != inst) {
            dmCreator = inst;
        }
        return old;
    }
    

    /**
     * Creates a connector instance.
     * 
     * @return the created connector instance
     */
    public static TransportConnector createConnector() {
        return mainCreator.createConnector();
    }
    
    /**
     * Creates an inter-process connector.
     * 
     * @return the created connector instance
     */
    public static TransportConnector createIpcConnector() {
        return ipcCreator.createConnector();
    }
    
    /**
     * Creates a direct memory transfer connector instance.
     * 
     * @return the direct memory connector instance
     */
    public static TransportConnector createDirectMemoryConnector() {
        return dmCreator.createConnector();
    }
    
    /**
     * Returns the descriptive name of the main connector.
     * 
     * @return the descriptive name
     */
    public static String getConnectorName() {
        return mainCreator.getName();
    }

}
