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

package test.de.iip_ecosphere.platform.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.ServiceState;
import de.iip_ecosphere.platform.services.Version;

/**
 * A test service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyServiceManager extends AbstractServiceManager<MyArtifactDescriptor, MyServiceDesciptor> {

    private int artifactId;
    private int serviceId;
    
    /**
     * Prevents external creation.
     */
    MyServiceManager() {
    }
    
    /**
     * Creates an artifact id.
     * 
     * @return the artifact id
     */
    private String createArtifactId() {
        return "art_" + artifactId++;
    }
    
    /**
     * Creates a service id.
     * 
     * @return the service id
     */
    private String createServiceId() {
        return "service_" + serviceId++;
    }
    
    @Override
    public String addArtifact(String location) throws ExecutionException {
        if (null == location) {
            throw new ExecutionException("location must not be null", null);
        }
        String aId = createArtifactId();
        List<MyServiceDesciptor> services = new ArrayList<>();
        services.add(new MyServiceDesciptor(createServiceId(), location, location, new Version(1, 0)));
        services.add(new MyServiceDesciptor(createServiceId(), location, location, new Version(1, 1)));
        super.addArtifact(aId, new MyArtifactDescriptor(aId, location, services));
        return aId;
    }

    @Override
    public void startService(String serviceId) throws ExecutionException {
        getServiceDescriptor(serviceId, "serviceId", "start").setState(ServiceState.RUNNING);
    }

    @Override
    public void stopService(String serviceId) throws ExecutionException {
        getServiceDescriptor(serviceId, "serviceId", "stop").setState(ServiceState.STOPPED);
    }

    @Override
    public void updateService(String id, String location) throws ExecutionException {
    }
    
    @Override
    public void removeArtifact(String id) throws ExecutionException {
        super.removeArtifact(id);
    }
    
    @Override
    public void switchToService(String id, String targetId) throws ExecutionException {
        super.switchToService(id, targetId);
    }

    @Override
    public void migrateService(String id, String location) throws ExecutionException {
        super.migrateService(id, location);
    }

    @Override
    public void cloneArtifact(String artifactId, String location) throws ExecutionException {
        // TODO Auto-generated method stub
    }
    
}