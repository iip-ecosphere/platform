/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
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
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * A service that delegates it's core operations to a nested service.
 * 
 * @param <S> the type of service to delegate to
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractDelegatingService<S extends Service> implements Service {
    
    private S service;
    
    /**
     * Creates an instance.
     * 
     * @param yaml the service description YAML
     * @see #initService()
     */
    public AbstractDelegatingService(YamlService yaml) {
        initService(yaml);
    }

    /**
     * Assigns the service by calling {@link #assignService(YamlService)}. May be overridden to do nothing
     * if {@link #assignService(YamlService)} is called later.
     * 
     * @param yaml the service description YAML
     * @see #assignService(YamlService)
     */
    protected void initService(YamlService yaml) {
        assignService(yaml);
    }
    
    /**
     * Assigns the service by calling {@link #createService(YamlService)}.
     * 
     * @param yaml the service description YAML
     */
    protected final void assignService(YamlService yaml) {
        service = createService(yaml);
    }

    /**
     * Creates the nested service instance to delegate to.
     * 
     * @param yaml the service description YAML
     * @return the service instance
     */
    protected abstract S createService(YamlService yaml);

    @Override
    public String getName() {
        return service.getName();
    }

    @Override
    public Version getVersion() {
        return service.getVersion();
    }

    @Override
    public String getDescription() {
        return service.getDescription();
    }

    @Override
    public boolean isDeployable() {
        return service.isDeployable();
    }

    @Override
    public boolean isTopLevel() {
        return service.isTopLevel();
    }

    @Override
    public ServiceKind getKind() {
        return service.getKind();
    }

    @Override
    public void migrate(String resourceId) throws ExecutionException {
        service.migrate(resourceId);
    }

    @Override
    public void update(URI location) throws ExecutionException {
        service.update(location);
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        service.switchTo(targetId);
    }

    @Override
    public void activate() throws ExecutionException {
        service.activate();
    }

    @Override
    public void passivate() throws ExecutionException {
        service.passivate();
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        service.reconfigure(values);
    }

    @Override
    public String getId() {
        return service.getId();
    }

    @Override
    public ServiceState getState() {
        return service.getState();
    }

    @Override
    public void setState(ServiceState state) throws ExecutionException {
        service.setState(state);
    }

    /**
     * Returns the service the operations are delegated to.
     * 
     * @return the service instance
     */
    protected S getService() {
        return service;
    }
    
}
