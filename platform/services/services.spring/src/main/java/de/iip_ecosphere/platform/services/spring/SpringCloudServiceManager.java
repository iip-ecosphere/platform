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

package de.iip_ecosphere.platform.services.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.ServiceFactoryDescriptor;
import de.iip_ecosphere.platform.services.ServiceManager;

/**
 * Service manager for Spring Cloud Stream.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringCloudServiceManager 
    extends AbstractServiceManager<SpringCloudArtifactDescriptor, SpringCloudServiceDescriptor> {

    private int artifactId;
    private int serviceId;
    
    // do not rename this class or the following descriptor class! Java Service Loader
    
    /**
     * Descriptor for creating the service manager instance.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class SpringCloudServiceFactoryDescriptor implements ServiceFactoryDescriptor {

        @Override
        public ServiceManager createInstance() {
            return new SpringCloudServiceManager();
        }
        
    }
    
    /**
     * Prevents external creation.
     */
    private SpringCloudServiceManager() {
    }
    
    /**
     * Returns the id of the own resource.
     * 
     * @return the id
     */
    private String getResourceId() {
        return "<resource-id>_"; // TODO preliminary, security!
    }

    /**
     * Creates an artifact id.
     * 
     * @return the artifact id
     */
    private String createArtifactId() {
        return getResourceId() + artifactId++; // TODO preliminary, security!
    }
    
    /**
     * Creates a service id.
     * 
     * @return the service id
     */
    @SuppressWarnings("unused")
    private String createServiceId() {
        return getResourceId() + serviceId++; // TODO preliminary, security!
    }
    
    @Override
    public String addArtifact(String location) throws ExecutionException {
        String aId = createArtifactId();
        // DOWNLOAD, Folder dependent on location
        // read in deployment descriptor
        List<SpringCloudServiceDescriptor> services = new ArrayList<>();
        // parse in services
        File jarFile = new File("");
        SpringCloudArtifactDescriptor artifact = new SpringCloudArtifactDescriptor(aId, location, jarFile, services);
        return super.addArtifact(aId, artifact);
    }

    @Override
    public void startService(String name) throws ExecutionException {
        throw new ExecutionException("not implemented", null);  // TODO
    }

    @Override
    public void stopService(String name) throws ExecutionException {
        throw new ExecutionException("not implemented", null);  // TODO
    }

    @Override
    public void migrateService(String name, String location) throws ExecutionException {
        super.migrateService(name, location);
        throw new ExecutionException("not implemented", null);  // TODO
    }

    @Override
    public void removeArtifact(String name) throws ExecutionException {
        super.removeArtifact(name);
        throw new ExecutionException("not implemented", null);  // TODO
    }

    @Override
    public void updateService(String name, String location) throws ExecutionException {
        throw new ExecutionException("not implemented", null);  // TODO
    }

    @Override
    public void switchToService(String name, String target) throws ExecutionException {
        super.switchToService(name, target);
        throw new ExecutionException("not implemented", null); // TODO
    }

    @Override
    public void cloneArtifact(String artifactId, String location) throws ExecutionException {
        throw new ExecutionException("not implemented", null);  // TODO
    }

}
