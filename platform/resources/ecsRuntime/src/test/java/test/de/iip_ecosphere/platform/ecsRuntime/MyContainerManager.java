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

package test.de.iip_ecosphere.platform.ecsRuntime;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerManager;

/**
 * A test container manager.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyContainerManager extends AbstractContainerManager<MyContainerDesciptor> {

    @Override
    public void addContainer(String id, String location) throws ExecutionException {
        super.addContainer(id, new MyContainerDesciptor());
    }

    @Override
    public void startContainer(String id) throws ExecutionException {
    }

    @Override
    public void stopContainer(String id) throws ExecutionException {
    }

    @Override
    public void updateContainer(String id, String location) throws ExecutionException {
    }
    
    @Override
    public void undeployContainer(String id) throws ExecutionException {
        super.undeployContainer(id);
    }

    @Override
    public void migrateContainer(String id, String location) throws ExecutionException {
        super.migrateContainer(id, location);
    }
    
}