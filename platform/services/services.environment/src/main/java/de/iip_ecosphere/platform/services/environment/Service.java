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

package de.iip_ecosphere.platform.services.environment;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.Version;

/**
 * Defines the (administrative) interface of an IIP-Ecosphere service.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Service extends ParameterConfigurerProvider, ServiceBase {
    
    /**
     * The name of the service.
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * The version of the service.
     * 
     * @return the version
     */
    public Version getVersion();
    
    /**
     * The description of the service.
     * 
     * @return the description, may be empty
     */
    public String getDescription();

    /**
     * Returns whether the service is deployable in distributable manner or fixed in deployment location.
     * 
     * @return {@code true} for deployable, {@code false} for fixed
     */
    public boolean isDeployable();

    /**
     * Returns whether the service is a top-level service.
     * 
     * @return {@code true} for top-level, {@code false} for nested
     */
    public boolean isTopLevel();

    /**
     * Returns the service kind.
     * 
     * @return the service kind
     */
    public ServiceKind getKind();
    
    /**
     * Returns the resolved network address of the netMgtKey specified in the service descriptor.
     * 
     * @return the resolved address, may be <b>null</b> in particular if no key was specified in the service descriptor
     */
    public default ServerAddress getNetMgtKeyAddress() {
        return null;
    }

    /**
     * Migrates a service. However, it may be required to migrate/move the containing artifact. [adaptation]
     * 
     * @param resourceId the target resource id, e.g., a device
     * @throws ExecutionException if migration fails
     */
    public void migrate(String resourceId) throws ExecutionException;
    
    /**
     * Updates the service by the service in the given {@code location}. This operation is responsible for stopping
     * the running service (if needed), replacing it, starting the new service.
     * 
     * @param location the location of the new service, e.g., an URL
     * @throws ExecutionException if the given service cannot be updated for some reason, e.g., because the replacement
     *   service is not an evolved version of the running service
     */
    public void update(URI location) throws ExecutionException;
    
    /**
     * Switches to an interface-compatible service. This method cares for stopping the old service, performing
     * a handover if adequate, starting the {@code target} service. [adaptation]
     * 
     * @param targetId the id of the target service
     * @throws ExecutionException if switching the service cannot be performed for some reason
     */
    public void switchTo(String targetId) throws ExecutionException;
    
    /**
     * Activates the service. [adaptation]
     * 
     * @throws ExecutionException in case that activating fails, e.g., because the service is already active 
     */
    public void activate() throws ExecutionException;

    /**
     * Passivates the service. [adaptation]
     * 
     * @throws ExecutionException in case that passivating fails, e.g., because the service is already passive 
     */
    public void passivate() throws ExecutionException;
    
    /**
     * Reconfigures the service. [adaptation]
     * 
     * @param values the (service-specific) name-value mapping that shall lead to a reconfiguration of the service; 
     *   values come either as primitive values or as JSON structures complying with the parameter descriptor. The 
     *   service is responsible for correct JSON de-serialization according to the respective descriptor.
     * @throws ExecutionException if reconfiguration fails
     */
    public void reconfigure(Map<String, String> values) throws ExecutionException;

    @Override
    public default ParameterConfigurer<?> getParameterConfigurer(String paramName) {
        return null;
    }
    
    @Override
    public default Set<String> getParameterNames() {
        return null;
    }

}
