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

import java.util.Optional;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

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
    
    private static ConnectorCreator mainCreator = DefaultTransportFactoryDescriptor.DEFAULT_DM_CREATOR;
    private static ConnectorCreator ipcCreator = DefaultTransportFactoryDescriptor.DEFAULT_DM_CREATOR;
    private static ConnectorCreator dmCreator = DefaultTransportFactoryDescriptor.DEFAULT_DM_CREATOR;
    private static boolean initialized = false;

    /**
     * Changes the main factory implementation. May be replaced by an injection-based
     * mechanism, so far required for testing.
     * 
     * @param inst the factory implementation instance (ignored if <b>null</b>)
     * @return the factory implementation instance before calling this method
     */
    public static ConnectorCreator setMainImplementation(ConnectorCreator inst) {
        initialize();
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
        initialize();
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
        initialize();
        ConnectorCreator old = dmCreator;
        if (null != inst) {
            dmCreator = inst;
        }
        return old;
    }
    
    /**
     * Initializes the factory if not already done.
     */
    private static void initialize() {
        if (!initialized) {
            Optional<TransportFactoryDescriptor> desc = ServiceLoaderUtils.findFirst(TransportFactoryDescriptor.class);
            if (desc.isPresent()) {
                TransportFactoryDescriptor descriptor = desc.get();
                LoggerFactory.getLogger(TransportFactory.class).info("Configuring TransportFactory with " 
                    + descriptor.getClass().getName());
                mainCreator = getCreator(descriptor.getMainCreator(), mainCreator);
                ipcCreator = getCreator(descriptor.getIpcCreator(), ipcCreator);
                dmCreator = getCreator(descriptor.getDmCreator(), dmCreator);
            } 
            initialized = true;
        } 
    }
    
    /**
     * Returns either {@code creator} if not <b>null</b> or {@code dflt}.
     * 
     * @param creator the primary creator to return (if not <b>null</b>)
     * @param dflt the default creator to return if the primary creator is not present
     * @return the creator, either {@code creator} or {@code dflt}
     */
    private static ConnectorCreator getCreator(ConnectorCreator creator, ConnectorCreator dflt) {
        return null == creator ? dflt : creator;
    }

    /**
     * Creates a connector instance.
     * 
     * @return the created connector instance
     */
    public static TransportConnector createConnector() {
        initialize();
        return mainCreator.createConnector();
    }
    
    /**
     * Creates an inter-process connector.
     * 
     * @return the created connector instance
     */
    public static TransportConnector createIpcConnector() {
        initialize();
        return ipcCreator.createConnector();
    }
    
    /**
     * Creates a direct memory transfer connector instance.
     * 
     * @return the direct memory connector instance
     */
    public static TransportConnector createDirectMemoryConnector() {
        initialize();
        return dmCreator.createConnector();
    }
    
    /**
     * Returns the descriptive name of the main connector.
     * 
     * @return the descriptive name
     */
    public static String getConnectorName() {
        initialize();
        return mainCreator.getName();
    }

}
