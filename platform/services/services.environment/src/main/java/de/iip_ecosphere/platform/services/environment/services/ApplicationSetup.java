/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.services;

/**
 * Describes static information about an application, e.g., taken from the configuration. May be read from yaml,
 * therefore already in snakeyaml style.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ApplicationSetup {

    private String id;
    private String name;

    /**
     * For snakeyaml.
     */
    public ApplicationSetup() {
    }

    /**
     * Copy constructor.
     * 
     * @param setup the instance to copy from
     */
    public ApplicationSetup(ApplicationSetup setup) {
        this.id = setup.id;
        this.name = setup.name;
    }

    /**
     * Returns the application id.
     * 
     * @return the application id
     */
    public String getId() {
        return id;
    }
    
    /**
     * Changes the application id. [snakeyaml]
     * 
     * @param id the application id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the application name.
     * 
     * @return the application name
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the application name. [snakeyaml]
     * 
     * @param name the application name
     */
    public void setName(String name) {
        this.name = name;
    } 
    
}
