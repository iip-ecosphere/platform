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

package de.iip_ecosphere.platform.services;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.*;
import static de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.*;

/**
 * A client for {@link ServicesAas} for accessing the operations provided by a certain resource.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServicesAasClient extends SubmodelElementsCollectionClient implements ServiceOperations {

    
    /**
     * Creates a client instance based on a deployed IIP-AAS from {@link AasPartRegistry} based on a submodel with
     * {@link ServicesAas#NAME_SUBMODEL_RESOURCES resources}.
     * 
     * @param resourceId the id used as key in {@link ServicesAas#NAME_SUBMODEL_RESOURCES} to denote the resource 
     *   to operate on
     * @throws IOException if retrieving the IIP-AAS or the respective submodel fails
     */
    public ServicesAasClient(String resourceId) throws IOException {
        super(ServicesAas.NAME_SUBMODEL_RESOURCES, resourceId);
    }

    @Override
    public String addArtifact(URI location) throws ExecutionException {
        return fromJson(getOperation(ServicesAas.NAME_OP_ARTIFACT_ADD).invoke(location.toString()));
    }

    @Override
    public void removeArtifact(String artifactId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_ARTIFACT_REMOVE).invoke(artifactId));
    }

    @Override
    public void updateService(String serviceId, URI location) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_UPDATE).invoke(serviceId, location.toString()));
    }
    
    @Override
    public void switchToService(String serviceId, String targetId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_SWITCH).invoke(serviceId, targetId));
    }
    
    @Override
    public void activateService(String serviceId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_ACTIVATE).invoke(serviceId));
    }

    @Override
    public void passivateService(String serviceId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_PASSIVATE).invoke(serviceId));
    }

    @Override
    public void startService(String... serviceId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_START).invoke((Object[]) serviceId));
    }
    
    @Override
    public void stopService(String... serviceId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_STOP).invoke((Object[]) serviceId));
    }
    
    @Override
    public void migrateService(String serviceId, String resourceId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_MIGRATE).invoke(serviceId, resourceId));
    }

    @Override
    public void reconfigureService(String serviceId, Map<String, String> values) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_RECONF).invoke(serviceId, writeMap(values)));
    }

    @Override
    public void setServiceState(String serviceId, ServiceState state) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_SET_STATE).invoke(serviceId, state.toString()));
    }

    @Override
    public ServiceState getServiceState(String serviceId) {
        ServiceState result = ServiceState.UNKOWN;
        try {
            String tmp = fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_GET_STATE).invoke(serviceId));
            if (null != tmp) {
                result = ServiceState.valueOf(tmp);
            }
        } catch (ExecutionException e) {
            getLogger().error("Requesting service state: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            getLogger().error("Requesting service state, illegal response value: " + e.getMessage());
        }
        return result;
    }
    
    // getService -> own service descriptor, clone?

}
