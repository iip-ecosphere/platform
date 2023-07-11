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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * Services client operations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServicesClient extends ServiceOperations {

    /**
     * Returns the services associated to a given {@code artifactId}.
     * 
     * @param artifactId the artifactId to search for
     * @param topLevelOnly whether only top-level services or all shall be returned
     * @return the associated service ids, empty if none was found
     */
    public String[] getServices(String artifactId, boolean topLevelOnly);
    
    /**
     * Returns the collection with all services of the resources this client was created for.
     * 
     * @return the services collection, may be <b>null</b> for none
     */
    public SubmodelElementCollection getServices();
    
    /**
     * Returns the collection with all artifacts of the resources this client was created for.
     * 
     * @return the artifacts collection, may be <b>null</b> for none
     */
    public SubmodelElementCollection getArtifacts();
    
    /**
     * Like {@link #startService(String...)} but reporting on the given {@code taskId}.
     * 
     * @param taskId the task id to report on (may be <b>null</b>, leads to {@link #startService(String...)})
     * @param serviceId the id(s) of the service(s)
     * @throws ExecutionException in case that starting the service fails for some reason
     */
    public void startServiceAsTask(String taskId, String... serviceId) throws ExecutionException;

    /**
     * Like {@link #startService(Map, String...)} but reporting on the given {@code taskId}.
     * 
     * @param taskId the task id to report on (may be <b>null</b>, leads to {@link #startService(Map, String...)})
     * @param options optional map of optional options, see {@link #startService(Map, String...)}
     * @param serviceId the id(s) of the service(s)
     * @throws ExecutionException in case that starting the service fails for some reason
     */
    public void startServiceAsTask(String taskId, Map<String, String> options, String... serviceId) 
        throws ExecutionException;

    /**
     * Like {@link #stopService(String...)} but reporting on the given {@code taskId}.
     * 
     * @param taskId the task id to report on (may be <b>null</b>, leads to {@link #stopService(String...)})
     * @param serviceId the id(s) of the service(s) to stop
     * @throws ExecutionException if stopping the service fails
     */
    public void stopServiceAsTask(String taskId, String... serviceId) throws ExecutionException;

    /**
     * Adds an artifact (and transitively the contained services) to the management domain of this instance, e.g., 
     * by downloading it from an artifact/service store. This defines the {@code id} of the service within the 
     * management domain of this instance. After a successful execution, the artifact {@code id} is returned, artifact 
     * and service(s) shall be available and the service(s) shall be in state {@link ServiceState#AVAILABLE}.
     * 
     * @param taskId the task id to report on (may be <b>null</b>, leads to {@link #addArtifact(URI)})
     * @param location the location from where to download the service, e.g., an URL
     * @return the id of the artifact
     * @throws ExecutionException in case that adding the service fails for some reason
     */
    public String addArtifactAsTask(String taskId, URI location) throws ExecutionException;

    /**
     * Removes the artifact (and transitively its services) from the management domain of this instance. This operation 
     * shall only remove the implementation of non-operational services and, thus, perform a state transition to 
     * {@link ServiceState#UNDEPLOYING} and ultimately the service(s) shall be removed and their descriptors (including 
     * the artifact descriptor) shall not be available anymore.
     * 
     * @param taskId the task id to report on (may be <b>null</b>, leads to {@link #removeArtifact(String)})
     * @param artifactId the id of the artifact to remove, or as fallback the canonical URI of the artifact
     * @throws ExecutionException if removing the service fails, e.g., because it is still running
     */
    public void removeArtifactAsTask(String taskId, String artifactId) throws ExecutionException;

}
