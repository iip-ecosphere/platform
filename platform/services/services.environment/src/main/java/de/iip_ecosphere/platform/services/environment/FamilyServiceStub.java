/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;

/**
 * Family service implementations are supposed to delegate their operations to the (alternative) active family member.
 * Thus, there may not be a fully fledged service implementation for the family and management operations may just not
 * work. This class is a basis for a no-operation service (stub) that delegates relevant operations to its active 
 * service. To not further interfere with the switching mechanism, the active service is provided via a supplier.
 * 
 * Currently, not all management operations are supported, e.g., migrate, switch etc. are missing (in the 
 * {@link ServiceBase} interface).
 * 
 * @author Holger Eichelberger, SSE
 *
 */
public class FamilyServiceStub extends DefaultServiceImpl {

    private Supplier<ServiceBase> active;
    
    /**
     * Creates a family service stub from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public FamilyServiceStub(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    /**
     * Changes the active service member.
     * 
     * @param active the new active member
     */
    public void setActiveMemberSupplier(Supplier<ServiceBase> active) {
        this.active = active;
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        super.setState(state);
        ServiceBase active = this.active.get();
        if (null != active) {
            active.setState(state);
        }
    }
    
    @Override
    public ServiceState getState() {
        return super.getState(); // preliminary, influence of active
    }
    
    // TODO migrate, update, switch?

}
