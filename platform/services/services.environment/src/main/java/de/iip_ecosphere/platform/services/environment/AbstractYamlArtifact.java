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

import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Basic information about an artifact containing services, abstract because template-based service objects do not
 * work with SnakeYaml. By default, reference types are created based on the attribute definition in the class. As
 * soon as mechanisms are available to handle this, these additional classes may collapse into a more simple hierarchy.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractYamlArtifact {

    private String id; // duplicate in application, may be removed
    private String name;
    private Version version;
    private ApplicationSetup application;

    /**
     * Returns the application setup.
     * 
     * @return the application setup
     */
    public ApplicationSetup getApplication() {
        return application;
    }

    /**
     * Returns the name of the service.
     * 
     * @return the name
     */
    public String getId() {
        return id;
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
     * Changes the application setup.
     * 
     * @param application the application setup
     */
    public void setApplication(ApplicationSetup application) {
        this.application = application;
    }
    
    /**
     * Defines the id of the service. [required by SnakeYaml]
     * 
     * @param id the id
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
     * Defines the version of the service. [required by SnakeYaml]
     * 
     * @param version the version
     */
    public void setVersion(Version version) {
        this.version = version;
    }
    
}
