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

package de.iip_ecosphere.platform.services.environment;

import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Basic information about a service, abstract because template-based service objects do not
 * work with SnakeYaml. By default, reference types are created based on the attribute definition in the class. As
 * soon as mechanisms are available to handle this, these additional classes may collapse into a more simple hierarchy.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractYamlService {

    private String id;
    private String applicationId = "";
    private String name;
    private Version version;
    private String description = "";
    private ServiceKind kind;
    private boolean deployable = false;
    private boolean topLevel = true;
    private String netMgtKey;

    /**
     * Returns the unique id of the service.
     * 
     * @return the id (may contain the {@link #getApplicationId() application id} and the 
     *     {@link #getApplicationInstanceId() application instance id} if specified)
     */
    public String getId() {
        return id;
    }
    
    /**
     * Returns the application id this service is assigned to (without 
     * {@link #getApplicationInstanceId() application instance id}).
     * 
     * @return the application id
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Returns the service id of the service, i.e. {@link #getId()} without {@link #getApplicationId()} 
     * and {@link #getApplicationInstanceId()}.
     * 
     * @return the id
     */
    public String getServiceId() {
        return ServiceBase.getServiceId(id);
    }

    /**
     * Returns the application instance id this service is running within. Usually, the 
     * {@link #getApplicationId() application id} shall be a prefix of this id.
     * 
     * @return the application instance id (may be empty for the default application instance)
     */
    public String getApplicationInstanceId() {
        return ServiceBase.getApplicationInstanceId(id);
    }

    /**
     * Returns the network management key of a service instance this service is relying on.
     * 
     * @return the network management key, may be empty or <b>null</b> for none
     */
    public String getNetMgtKey() {
        return netMgtKey;
    }

    /**
     * Returns the name of the service.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of the service.
     * 
     * @return the version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Returns the description of the service.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the service kind.
     * 
     * @return the service kind
     */
    public ServiceKind getKind() {
        return kind;
    }
    
    /**
     * Returns whether this service is decentrally deployable.
     * 
     * @return {@code true} for deployable, {@code false} for not deployable 
     */
    public boolean isDeployable() {
        return deployable;
    }

    /**
     * Returns whether this service is top-level.
     * 
     * @return {@code true} for top-level, {@code false} for nested
     */
    public boolean isTopLevel() {
        return topLevel;
    }

    /**
     * Defines the id of the service. [required by SnakeYaml]
     * 
     * @param id the id (may contain the {@link #getApplicationId() application id} if specified)
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Defines the name of the service. [required by SnakeYaml]
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Defines the application id this service is assigned to. [required by SnakeYaml]
     * 
     * @param applicationId the application id
     */
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * Defines the network management key of a service instance this service is relying on.
     * 
     * @param netMgtKey the network management key, may be empty or <b>null</b> for none
     */
    public void setNetMgtKey(String netMgtKey) {
        this.netMgtKey = netMgtKey;
    }
    
    /**
     * Defines the version of the service. [required by SnakeYaml]
     * 
     * @param version the version
     */
    public void setVersion(Version version) {
        this.version = version;
    }

    /**
     * Defines the description of the service. [required by SnakeYaml]
     * 
     * @param description the description (<b>null</b> is ignored, default is empty)
     */
    public void setDescription(String description) {
        if (null != description) {
            this.description = description;
        }
    }
    
    /**
     * Sets whether this service is decentrally deployable.
     * 
     * @param deployable {@code true} for deployable, {@code false} for not deployable 
     */
    public void setDeployable(boolean deployable) {
        this.deployable = deployable;
    }

    /**
     * Sets whether this service is a top-level service.
     * 
     * @param topLevel {@code true} for topLevel, {@code false} for nested
     */
    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    /**
     * Sets the service kind. [required by SnakeYaml]
     * 
     * @param kind the service kind
     */
    public void setKind(ServiceKind kind) {
        this.kind = kind;
    }

}
