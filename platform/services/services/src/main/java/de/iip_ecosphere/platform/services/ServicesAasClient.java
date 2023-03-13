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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
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

    private String appId = "";
    
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

    /**
     * Creates a client instance based on a deployed IIP-AAS from {@link AasPartRegistry} based on a submodel with
     * {@link ServicesAas#NAME_SUBMODEL_RESOURCES resources}.
     * 
     * @param resourceId the id used as key in {@link ServicesAas#NAME_SUBMODEL_RESOURCES} to denote the resource 
     *   to operate on
     * @param appId optional app id to select the service manager to use, may be empty or null for the 
     *   first/fallback manager
     * @throws IOException if retrieving the IIP-AAS or the respective submodel fails
     */
    public ServicesAasClient(String resourceId, String appId) throws IOException {
        super(ServicesAas.NAME_SUBMODEL_RESOURCES, resourceId);
        this.appId = appId == null ? "" : appId;
    }

    @Override
    protected SubmodelElementCollection getSubmodelElementCollection() {
        SubmodelElementCollection result = super.getSubmodelElementCollection();
        SubmodelElementCollection m = result.getSubmodelElementCollection(ServicesAas.NAME_COLL_SERVICE_MANAGERS);
        if (null != m) {
            SubmodelElementCollection found = null;
            int foundServices = -1;
            SubmodelElementCollection fallback = null;
            int fallbackServices = -1;
            for (SubmodelElement elt : m.elements()) {
                if (elt instanceof SubmodelElementCollection) {
                    SubmodelElementCollection mgr = (SubmodelElementCollection) elt;
                    String[] appIds = AasUtils.getPropertyValueAsStringSafe(mgr, 
                        ServicesAas.NAME_PROP_SUPPORTED_APPIDS, "").split(",");
                    int instances = getServiceStateCount(mgr, ServiceState.RUNNING);
                    if (appIds.length == 0 || contains(appIds, appId)) { // eligible?
                        if (foundServices < 0 || instances < foundServices) {
                            found = mgr;
                        }
                    }
                    if (fallbackServices < 0 || instances < fallbackServices) {
                        fallback = mgr;
                    }
                }
            }
            if (null == found) {
                found = fallback; // CACHE?
            }
            if (found != null) {
                result = found;
            }
        }
        return result;
    }
    
    /**
     * Returns whether {@code array} contains {@code elt}.
     * 
     * @param array the array to search
     * @param elt the element to search for
     * @return {@code true} if contained, {@code false} else
     */
    private static boolean contains(String[] array, String elt) {
        boolean contains = false;
        for (int i = 0; !contains && i < array.length; i++) {
            contains = elt.equals(array[i]);
        }
        return contains;
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
    public void startService(Map<String, String> options, String... serviceId) throws ExecutionException {
        if (null == options) {
            options = new HashMap<>();
        }
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_START_WITH_OPTS).invoke(JsonUtils.toJson(serviceId), 
            writeMap(options)));
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

    @Override
    public void startServiceAsTask(String taskId, String... serviceId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_START_TASK)
            .invoke(JsonUtils.toJson(serviceId), JsonUtils.toJson(taskId)));
    }

    @Override
    public void startServiceAsTask(String taskId, Map<String, String> options, String... serviceId)
        throws ExecutionException {
        if (null == options) {
            options = new HashMap<>();
        }
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_START_WITH_OPTS_TASK)
            .invoke(JsonUtils.toJson(serviceId), JsonUtils.toJson(taskId), writeMap(options)));
    }

    @Override
    public void stopServiceAsTask(String taskId, String... serviceId) throws ExecutionException {
        fromJson(getOperation(ServicesAas.NAME_OP_SERVICE_STOP_TASK)
            .invoke(JsonUtils.toJson(serviceId), JsonUtils.toJson(taskId)));
    }

    /**
     * Requests the number of services in the given {@code state} on the service manager submodel elements
     * collection {@code resource}.
     * 
     * @param resource the submodel elements collection representing a service manager
     * @param state the state to query for
     * @return the number of service instances
     */
    private int getServiceStateCount(SubmodelElementCollection resource, ServiceState state) {
        int result = 0;
        try {
            Operation op = resource.getOperation(ServicesAas.NAME_OP_SERVICE_STATE_COUNT);
            if (null == op) {
                throw new ExecutionException("Operation `" + ServicesAas.NAME_OP_SERVICE_STATE_COUNT 
                    + "` on resource `" + resource.getIdShort() + "` not found.", null); 
            }
            Object tmp = op.invoke(JsonUtils.toJson(state.name()));
            if (tmp instanceof Integer) {
                result = ((Integer) tmp).intValue();
            }
        } catch (ExecutionException | NumberFormatException e) {
            getLogger().error("Requesting state instance count: " + e.getMessage());
        }        
        return result;
    }

    @Override
    public int getServiceInstanceCount(String serviceId) {
        int result = 0;
        try {
            Object tmp = getOperation(ServicesAas.NAME_OP_SERVICE_INSTANCE_COUNT)
                .invoke(JsonUtils.toJson(serviceId));
            if (tmp instanceof Integer) {
                result = ((Integer) tmp).intValue();
            }
        } catch (ExecutionException | NumberFormatException e) {
            getLogger().error("Requesting service instance count: " + e.getMessage());
        }        
        return result;
    }
    
}
