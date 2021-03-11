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

package de.iip_ecosphere.platform.services.spring;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.AbstractServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceKind;
import de.iip_ecosphere.platform.services.Version;

/**
 * Specific descriptor implementation for spring cloud streams.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SpringCloudServiceDescriptor extends AbstractServiceDescriptor {

    /**
     * Creates an instance. Call {@link #setClassification(ServiceKind, boolean)} afterwards.
     * 
     * @param id the service id
     * @param name the name of this service
     * @param description the description of the service
     * @param version the version
     */
    SpringCloudServiceDescriptor(String id, String name, String description, Version version) {
        super(id, name, description, version);
    }
    
    @Override
    public void passivate() throws ExecutionException {
        // TODO
    }

    @Override
    public void activate() throws ExecutionException {
        // TODO
    }
    
    @Override
    public void reconfigure(Map<String, Object> values) throws ExecutionException {
        // TODO
    }

}
