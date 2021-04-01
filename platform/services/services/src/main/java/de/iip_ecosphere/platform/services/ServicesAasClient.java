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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.NetworkManagerAas;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelClient;

import static de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.*;

/**
 * A client for {@link ServicesAas}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServicesAasClient extends SubmodelClient implements ServiceOperations {

    /**
     * Creates a client instance based on a deployed IIP-AAS from {@link AasPartRegistry} based on a submodel with
     * {@link NetworkManagerAas#NAME_SUBMODEL name}.
     * 
     * @throws IOException if retrieving the IIP-AAS or the respective submodel fails
     */
    public ServicesAasClient() throws IOException {
        this(ActiveAasBase.getSubmodel(ServicesAas.NAME_SUBMODEL));
    }
    
    /**
     * Creates a client instance based on the submodel. The submodel shall conform to {@link ServicesAas} with 
     * respect to the operations, signatures, but also the {@link ServicesAas#NAME_SUBMODEL name},
     * 
     * @param submodel the submodel to use
     */
    public ServicesAasClient(Submodel submodel) {
        super(submodel);
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
    public void migrateService(String serviceId, URI location) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_MIGRATE).invoke(serviceId, location.toString()));
    }

    @Override
    public void reconfigureService(String serviceId, Map<String, String> values) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_RECONF).invoke(serviceId, ServicesAas.writeMap(values)));
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
    
    /**
     * Returns the logger instance.
     * 
     * @return the logger
     */
    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }
    
    // getService -> own service descriptor, clone?

}
