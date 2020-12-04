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

package de.iip_ecosphere.platform.connectors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * A service type/instance registry as internal structure to manage the contents of an AAS for this component.
 *  
 * @author Holger Eichelberger, SSE
 */
public class ConnectorRegistry {
    
    private static final List<Connector<?, ?, ?, ?, ?>> INSTANCES 
        = Collections.synchronizedList(new ArrayList<Connector<?, ?, ?, ?, ?>>());
    
    /**
     * Returns the service loader for connector descriptors.
     * 
     * @return the service loader
     */
    public static ServiceLoader<ConnectorDescriptor> getRegisteredConnectorDescriptorsLoader() {
        return ServiceLoader.load(ConnectorDescriptor.class);
    }
    
    /**
     * Returns the registered connector descriptors as iterator.
     * 
     * @return the registered connector descriptors
     */
    public static Iterator<ConnectorDescriptor> getRegisteredConnectorDescriptors() {
        ServiceLoader<ConnectorDescriptor> loader = getRegisteredConnectorDescriptorsLoader();
        return loader.iterator();
    }
    
    /**
     * Registers a connector instance.
     * 
     * @param instance the instance
     */
    public static void registerConnector(Connector<?, ?, ?, ?, ?> instance) {
        INSTANCES.add(instance);
        // notify AAS in parallel?
    }

    /**
     * Unregisters a connector instance.
     * 
     * @param instance the instance
     */
    public static void unregisterConnector(Connector<?, ?, ?, ?, ?> instance) {
        INSTANCES.remove(instance);
        // notify AAS in parallel?
    }
    
    /**
     * Returns the number of registered connector instances.
     * 
     * @return the number of instances
     */
    public static int getRegisteredConnectorInstancesCount() {
        return INSTANCES.size();
    }
    
    /**
     * Returns the specified instance.
     * 
     * @return an iterator over all instances
     */
    public static Iterator<Connector<?, ?, ?, ?, ?>> getRegisteredConnectorInstances() {
        return INSTANCES.iterator();
    }

}
