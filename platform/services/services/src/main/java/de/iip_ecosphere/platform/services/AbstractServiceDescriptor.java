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

package de.iip_ecosphere.platform.services;

import java.util.concurrent.ExecutionException;

/**
 * Abstract {@link ServiceDescriptor} implementation, e.g., including a representation of the {@link ServiceState} 
 * statemachine.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractServiceDescriptor implements ServiceDescriptor {
    
    // TODO state transition checks
    // TODO basic implementation

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Version getVersion() {
        return new Version();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public ServiceState getState() {
        return ServiceState.UNKOWN;
    }

    @Override
    public void setState(ServiceState state) throws ExecutionException {
    }

    @Override
    public boolean isDeployable() {
        return false;
    }

    @Override
    public ServiceKind getKind() {
        return null;
    }

    @Override
    public void passivate() throws ExecutionException {
    }

    @Override
    public void activate() throws ExecutionException {
    }

}
