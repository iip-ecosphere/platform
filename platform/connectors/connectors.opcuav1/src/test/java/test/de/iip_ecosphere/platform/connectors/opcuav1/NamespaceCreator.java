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

package test.de.iip_ecosphere.platform.connectors.opcuav1;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;

/**
 * Something that creates an OPC UA namespace for a test.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface NamespaceCreator {

    /**
     * Creates the namespace.
     * 
     * @param server the server instance
     * @return the created namespace
     */
    public ManagedNamespaceWithLifecycle createNamespace(OpcUaServer server);
    
}
