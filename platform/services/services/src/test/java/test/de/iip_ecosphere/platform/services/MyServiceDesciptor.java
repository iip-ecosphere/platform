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

import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.AbstractServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceKind;
import de.iip_ecosphere.platform.services.ServiceState;
import de.iip_ecosphere.platform.services.Version;

/**
 * A test service descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyServiceDesciptor extends AbstractServiceDescriptor {

    /**
     * Creates an instance. Call {@link #setClassification(ServiceKind, boolean)} afterwards.
     * 
     * @param id the service id
     * @param name the name of this service
     * @param description the description of the service
     * @param version the version
     */
    protected MyServiceDesciptor(String id, String name, String description, Version version) {
        super(id, name, description, version);
    }
    
    @Override
    public void passivate() throws ExecutionException {
        if (ServiceState.RUNNING == getState()) {
            setState(ServiceState.PASSIVATING);
            setState(ServiceState.PASSIVATED);
        } else {
            throw new ExecutionException("Cannot passivate as service is in state " + getState(), null);
        }
    }

    @Override
    public void activate() throws ExecutionException {
        if (ServiceState.PASSIVATED == getState()) {
            setState(ServiceState.RUNNING);
        } else {
            throw new ExecutionException("Cannot passivate as service is in state " + getState(), null);
        }
    }
    
    @Override
    public void reconfigure(Map<String, Object> values) throws ExecutionException {
        ServiceState state = getState();
        setState(ServiceState.RECONFIGURING);
        // reconfigure
        setState(state);
    }
    
}