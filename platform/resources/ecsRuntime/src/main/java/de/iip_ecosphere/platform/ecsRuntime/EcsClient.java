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

import java.net.URI;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * ECS client operations interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface EcsClient extends ContainerOperations, DeviceManagementOperations {

    /**
     * Returns the collection with all containers of the resources this client was created for.
     * 
     * @return the containers collection, may be <b>null</b>
     */
    public SubmodelElementCollection getContainers();
    
    /**
     * Like {@link #addContainer(String)} but tracked by a {@code taskId}.
     * 
     * @param taskId the task id to report on
     * @param location the location from where to download the container, e.g., an URL
     * @return the id of the container
     * @throws ExecutionException in case that adding the container fails for some reason
     */
    public String addContainerAsTask(String taskId, URI location) throws ExecutionException;

    /**
     * Like {@link #startContainer(String)} but tracked by a {@code taskId}.
     * 
     * @param taskId the task id to report on
     * @param id the id of the container
     * @throws ExecutionException in case that starting the container fails for some reason
     */
    public void startContainerAsTask(String taskId, String id) throws ExecutionException;
    
    /**
     * Like {@link #stopContainer(String)} but tracked by a {@code taskId}.
     * 
     * @param taskId the task id to report on
     * @param id the id of the container to stop, or as fallback the canonical URI of the container descriptor used 
     *     for adding
     * @throws ExecutionException if stopping the container fails
     */
    public void stopContainerAsTask(String taskId, String id) throws ExecutionException;

    
}
