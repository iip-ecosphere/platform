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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.Version;

/**
 * Describes a service. Intentionally, a service descriptor does not contain administrative operations form 
 * {@link Service} as those operations shall be handled by the {@link ServiceManager} in consistent manner, 
 * e.g., across services.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServiceDescriptor {
    
    /**
     * Returns the unique id of the service.
     * 
     * @return the id (may contain the {@link #getApplicationId() application id} and the 
     *     {@link #getApplicationInstanceId() application instance id} if specified)
     */
    public String getId();

    /**
     * Returns the application id this service is assigned to (without 
     * {@link #getApplicationInstanceId() application instance id}).
     * 
     * @return the application id
     */
    public String getApplicationId();

    /**
     * Returns the service id of the service, i.e. {@link #getId()} without {@link #getApplicationId()} 
     * and {@link #getApplicationInstanceId()}.
     * 
     * @return the id
     */
    public default String getServiceId() {
        return ServiceBase.getServiceId(getId());
    }

    /**
     * Returns the application instance id this service is running within. Usually, the 
     * {@link #getApplicationId() application id} shall be a prefix of this id.
     * 
     * @return the application instance id (may be empty for the default application instance)
     */
    public default String getApplicationInstanceId() {
        return ServiceBase.getApplicationInstanceId(getId());
    }
    
    /**
     * The name of the service.
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * The version of the service.
     * 
     * @return the version
     */
    public Version getVersion();
    
    /**
     * The description of the service.
     * 
     * @return the description, may be empty
     */
    public String getDescription();

    /**
     * Returns the state the service is currently in. [R4c]
     * 
     * @return the state
     */
    public ServiceState getState();
    
    /**
     * Returns the ensemble leader service, i.e., if multiple services are packaged together and shall be executed
     * in the same process, it is important to synchronize aspects (via the ensemble leader service).
     * 
     * @return the ensemble leader, may be <b>null</b> if there is none
     */
    public ServiceDescriptor getEnsembleLeader();

    /**
     * Changes the state. [R133c]
     * 
     * @param state the new state
     * @throws ExecutionException if changing the state fails for some reason
     */
    public void setState(ServiceState state) throws ExecutionException;
    
    /**
     * Returns whether the service is deployable in distributable manner or fixed in deployment location.
     * 
     * @return {@code true} for deployable, {@code false} for fixed
     */
    public boolean isDeployable();

    /**
     * Returns whether the service is top-level or nested, e.g., as family member.
     * 
     * @return {@code true} for top-level, {@code false} else
     */
    public boolean isTopLevel();

    /**
     * Returns the service kind.
     * 
     * @return the service kind
     */
    public ServiceKind getKind();
    
    /**
     * Returns the containing artifact.
     * 
     * @return the containing artifact (descriptor)
     */
    public ArtifactDescriptor getArtifact();
    
    /**
     * Returns all information about parameter for {@link ServiceManager#reconfigureService(String, Map)}.
     * 
     * @return the name-descriptor mapping for all supported parameters
     */
    public List<TypedDataDescriptor> getParameters();
    
    /**
     * Returns all (asynchronous) input connectors into this service.
     * 
     * @return all input channels, may contain other-sided connectors where 
     *     {@link TypedDataConnectorDescriptor#getService()} is not {@link #getId()}
     */
    public List<TypedDataConnectorDescriptor> getInputDataConnectors();

    /**
     * Returns all (asynchronous) output connectors from this service.
     * 
     * @return all input channels, may contain other-sided connectors where 
     *     {@link TypedDataConnectorDescriptor#getService()} is not {@link #getId()}
     */
    public List<TypedDataConnectorDescriptor> getOutputDataConnectors();

    /**
     * Returns all (asynchronous) connectors from this service.
     * 
     * @return all channels, may contain other-sided connectors where 
     *     {@link TypedDataConnectorDescriptor#getService()} is not {@link #getId()}
     * @see #getInputDataConnectors()
     * @see #getOutputDataConnectors()
     */
    public List<TypedDataConnectorDescriptor> getDataConnectors();

    /**
     * Returns the invocables creator of this services, e.g., to connect metrics access from an AAS via this creator.
     * Depending on the service manager, there may be prerequisites that this method returns a creator instance.
     * 
     * @return the creator, may return {@link InvocablesCreator#NULL_CREATOR} but not <b>null</b>
     */
    public InvocablesCreator getInvocablesCreator();
    
    /**
     * Defines additional arguments to be passed on during service start.
     * 
     * @param args the arguments, may be empty or <b>null</b> for none
     */
    public void setAdditionalArguments(List<String> args);
    
    /**
     * Returns the additional arguments to be passed on during service start.
     * 
     * @return the arguments, may be empty or <b>null</b> for none
     */
    public List<String> getAdditionalArguments();
    
    /**
     * Denotes attachable actions.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum Action {
        
        /**
         * When communication parameters like IP address/port are (dynamically) determined and subsequent actions
         * such as adjusting AAS operations can happen.
         */
        COMMUNICATION_DETERMINED
    }
    
    /**
     * Attaches a runnable to an action.
     * 
     * @param action the action
     * @param run the runnable
     */
    public default void attachAction(Action action, Runnable run) {
    }
    
}
