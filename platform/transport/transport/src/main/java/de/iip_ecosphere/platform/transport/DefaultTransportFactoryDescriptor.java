/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport;

import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.impl.DirectMemoryTransferTransportConnector;

/**
 * The default transport factory descriptor, returning {@link #DEFAULT_DM_CREATOR} for all creator types.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DefaultTransportFactoryDescriptor implements TransportFactoryDescriptor {

    /**
     * The default factory implementation (to be able to return to this instance if needed).
     */
    public static final ConnectorCreator DEFAULT_DM_CREATOR = new ConnectorCreator() {

        @Override
        public TransportConnector createConnector() {
            return new DirectMemoryTransferTransportConnector();
        }

        @Override
        public String getName() {
            return DirectMemoryTransferTransportConnector.NAME;
        }
        
    };
    
    @Override
    public ConnectorCreator getMainCreator() {
        return DEFAULT_DM_CREATOR;
    }

    @Override
    public ConnectorCreator getIpcCreator() {
        return DEFAULT_DM_CREATOR;
    }

    @Override
    public ConnectorCreator getDmCreator() {
        return DEFAULT_DM_CREATOR;
    }

}
