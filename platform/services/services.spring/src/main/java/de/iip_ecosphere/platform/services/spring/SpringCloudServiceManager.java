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

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.ServiceFactoryDescriptor;
import de.iip_ecosphere.platform.services.ServiceManager;

/**
 * Service manager for Spring Cloud Stream.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringCloudServiceManager extends AbstractServiceManager<SpringCloudServiceDescriptor> {

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
    
    @Override
    public void addService(String name, String location) throws ExecutionException {
        super.addService(location, new SpringCloudServiceDescriptor());  // TODO fill with data
        throw new ExecutionException("not implemented", null);  // TODO
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
    public void removeService(String name) throws ExecutionException {
        super.removeService(name);
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

}
