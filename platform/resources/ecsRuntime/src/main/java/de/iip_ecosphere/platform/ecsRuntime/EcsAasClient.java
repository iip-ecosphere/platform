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

package de.iip_ecosphere.platform.ecsRuntime;

import static de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.fromJson;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.deviceMgt.Credentials;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;

/**
 * A client for {@link EcsAas} for accessing the operations provided by a certain resource.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsAasClient extends SubmodelElementsCollectionClient implements EcsClient {

    /**
     * Creates a client instance based on a deployed IIP-AAS from {@link AasPartRegistry} based on a submodel with
     * {@link EcsAas#NAME_SUBMODEL resources}.
     * 
     * @param resourceId the id used as key in {@link EcsAas#NAME_SUBMODEL} to denote the resource 
     *   to operate on
     * @throws IOException if retrieving the IIP-AAS or the respective submodel fails
     */
    public EcsAasClient(String resourceId) throws IOException {
        super(EcsAas.NAME_SUBMODEL, resourceId);
    }

    @Override
    public String addContainer(URI location) throws ExecutionException {
        return fromJson(getOperation(EcsAas.NAME_OP_CONTAINER_ADD).invoke(location.toString()));
    }

    @Override
    public void startContainer(String id) throws ExecutionException {
        fromJson(getOperation(EcsAas.NAME_OP_CONTAINER_START).invoke(id));
    }

    @Override
    public void stopContainer(String id) throws ExecutionException {
        fromJson(getOperation(EcsAas.NAME_OP_CONTAINER_STOP).invoke(id));
    }

    @Override
    public void migrateContainer(String id, String resourceId) throws ExecutionException {
        fromJson(getOperation(EcsAas.NAME_OP_CONTAINER_MIGRATE).invoke(id, resourceId));
    }

    @Override
    public void undeployContainer(String id) throws ExecutionException {
        fromJson(getOperation(EcsAas.NAME_OP_CONTAINER_UNDEPLOY).invoke(id));
    }
    
    @Override
    public Credentials createRemoteConnectionCredentials() throws ExecutionException {
        String result = (String) getOperation(EcsAas.NAME_OP_CREATE_REMOTE_CONNECTION_CREDENTIALS).invoke();
        ObjectMapper mapper = new ObjectMapper();
        Credentials credentials = null;
        try {
            credentials = mapper.readValue(result, Credentials.class);
        } catch (JsonProcessingException ignore) {
            // should not happen
        }

        return credentials;
    }

    @Override
    public String getRuntimeName() throws ExecutionException {
        return getPropertyStringValue(EcsAas.NAME_PROP_RUNTIME_NAME, "");
    }

    @Override
    public Integer getRuntimeVersion() throws ExecutionException {
        return Integer.parseInt(getPropertyStringValue(EcsAas.NAME_PROP_RUNTIME_VERSION, "-1"));
    }

    @Override
    public void updateContainer(String id, URI location) throws ExecutionException {
        fromJson(getOperation(EcsAas.NAME_OP_CONTAINER_UPDATE).invoke(id, location.toString()));
    }

    @Override
    public ContainerState getState(String id) {
        ContainerState result = ContainerState.UNKNOWN;
        try {
            String tmp = fromJson(getOperation(EcsAas.NAME_OP_GET_STATE).invoke(id));
            if (null != tmp) {
                result = ContainerState.valueOf(tmp);
            }
        } catch (ExecutionException e) {
            getLogger().error("Requesting service state: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            getLogger().error("Requesting service state, illegal response value: " + e.getMessage());
        }
        return result;
    }

    @Override
    public String getContainerSystemName() {
        return getPropertyStringValue(EcsAas.NAME_PROP_CSYS_NAME, "");
    }

    @Override
    public String getContainerSystemVersion() {
        return getPropertyStringValue(EcsAas.NAME_PROP_CSYS_VERSION, "");
    }

    @Override
    public SubmodelElementCollection getContainers() {
        return getSubmodel().getSubmodelElementCollection(EcsAas.NAME_COLL_CONTAINERS);
    }

    @Override
    public String addContainerAsTask(String taskId, URI location) throws ExecutionException {
        return fromJson(getOperation(EcsAas.NAME_OP_CONTAINER_ADD_TASK).invoke(location.toString(), taskId));
    }

    @Override
    public void startContainerAsTask(String taskId, String id) throws ExecutionException {
        fromJson(getOperation(EcsAas.NAME_OP_CONTAINER_START_TASK).invoke(id, taskId));
    }

    @Override
    public void stopContainerAsTask(String taskId, String id) throws ExecutionException {
        fromJson(getOperation(EcsAas.NAME_OP_CONTAINER_STOP_TASK).invoke(id, taskId));
    }

}
