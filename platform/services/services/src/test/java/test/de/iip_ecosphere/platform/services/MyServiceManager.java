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

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.AbstractServiceManager;

/**
 * A test service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyServiceManager extends AbstractServiceManager<MyServiceDesciptor> {

    @Override
    public void addService(String id, String location) throws ExecutionException {
        super.addService(id, new MyServiceDesciptor());
    }

    @Override
    public void startService(String id) throws ExecutionException {
    }

    @Override
    public void stopService(String id) throws ExecutionException {
    }

    @Override
    public void updateService(String id, String location) throws ExecutionException {
    }
    
    @Override
    public void removeService(String id) throws ExecutionException {
        super.removeService(id);
    }
    
    @Override
    public void switchToService(String id, String targetId) throws ExecutionException {
        super.switchToService(id, targetId);
    }

    @Override
    public void migrateService(String id, String location) throws ExecutionException {
        super.migrateService(id, location);
    }
    
}