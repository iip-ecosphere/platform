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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.*;
import static de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.*;

/**
 * A client for {@link ServicesAas} for accessing the operations provided by a certain resource.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServicesAasClient extends SubmodelElementsCollectionClient implements ServicesClient {

    
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
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_START).invoke(JsonUtils.toJson(serviceId)));
    }
    
    @Override
    public void stopService(String... serviceId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_STOP).invoke(JsonUtils.toJson(serviceId)));
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
        ServiceState result = ServiceState.UNKNOWN;
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
    
    @Override
    public String[] getServices(String artifactId, boolean topLevelOnly) {
        List<String> result = new ArrayList<String>();
        SubmodelElementCollection coll = getServices();
        if (null != coll) {
            getServices(coll, artifactId, result, topLevelOnly);
        }
        String[] tmp = new String[result.size()];
        return result.toArray(tmp);
    }

    /**
     * Collects the services associated to a given {@code artifactId}.
     * 
     * @param coll the collection containing the services
     * @param artifactId the artifactId to search for
     * @param serviceIds the service ids to be modified as a side effect
     * @param topLevelOnly whether only top-level services or all services shall be returned
     */
    private void getServices(SubmodelElementCollection coll, String artifactId, List<String> serviceIds, 
        boolean topLevelOnly) {
        for (SubmodelElement elt : coll.elements()) {
            if (elt instanceof SubmodelElementCollection) {
                SubmodelElementCollection service = (SubmodelElementCollection) elt;
                Property id = service.getProperty(ServicesAas.NAME_PROP_ID);
                Property art = service.getProperty(ServicesAas.NAME_PROP_ARTIFACT);
                Property topLevel = null;
                if (topLevelOnly) {
                    topLevel = service.getProperty(ServicesAas.NAME_PROP_TOPLEVEL);
                }
                if (null != id && null != art) {
                    try {
                        Object artId = art.getValue();
                        if (artifactId.equals(artId)) {
                            boolean tlOk = true;
                            if (topLevel != null) {
                                tlOk = Boolean.TRUE.equals(topLevel.getValue());
                            }
                            Object serId = id.getValue();
                            if (null != serId && tlOk) {
                                serviceIds.add(serId.toString());
                            }
                        }
                    } catch (ExecutionException e) {
                    }
                }
            }
        }
    }
    
    @Override
    public SubmodelElementCollection getArtifacts() {
        SubmodelElementCollection result;
        try {
            result = ActiveAasBase.getSubmodel(ServicesAas.NAME_SUBMODEL)
                .getSubmodelElementCollection(ServicesAas.NAME_COLL_ARTIFACTS);
        } catch (IOException e) {
            result = null;
        }
        return result;
    }

    @Override
    public SubmodelElementCollection getServices() {
        SubmodelElementCollection result;
        try {
            result = ActiveAasBase.getSubmodel(ServicesAas.NAME_SUBMODEL)
                .getSubmodelElementCollection(ServicesAas.NAME_COLL_SERVICES);
        } catch (IOException e) {
            result = null;
        }
        return result;
    }

    /**
     * Returns the collection with all relations of the resources this client was created for.
     * 
     * @return the relations collection, may be <b>null</b> for none
     */
    public SubmodelElementCollection getRelations() {
        SubmodelElementCollection result;
        try {
            result = ActiveAasBase.getSubmodel(ServicesAas.NAME_SUBMODEL)
                .getSubmodelElementCollection(ServicesAas.NAME_COLL_RELATIONS);
        } catch (IOException e) {
            result = null;
        }
        return result;
    }
    
}
