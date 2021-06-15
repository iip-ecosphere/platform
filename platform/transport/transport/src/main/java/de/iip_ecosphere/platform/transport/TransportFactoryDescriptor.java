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

/**
 * Defines the transport factory descriptor to customize the transport factory with individual protocols.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface TransportFactoryDescriptor {
    
    /**
     * Returns the main transport connector creator.
     * 
     * @return the main transport connector creator
     */
    public ConnectorCreator getMainCreator();

    /**
     * Returns the interprocess transport connector creator.
     * 
     * @return the interprocess transport creator
     */
    public ConnectorCreator getIpcCreator();

    /**
     * Returns the direct memory connector creator.
     * 
     * @return the direct memory  transport creator
     */
    public ConnectorCreator getDmCreator();

}
