/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import de.iip_ecosphere.platform.configuration.PlatformInstantiator;

/**
 * Extends the basic configuration mojo of this plugin to handle the apps to be generated.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractAppsConfigurationMojo extends AbstractConfigurationMojo {

    @Parameter(property = "configuration.apps", required = false, defaultValue = "")
    private String apps = "";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.setProperty(PlatformInstantiator.KEY_PROPERTY_APPS, apps);
        super.execute();
    }

}
