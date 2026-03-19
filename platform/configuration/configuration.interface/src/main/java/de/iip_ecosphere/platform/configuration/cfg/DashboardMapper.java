/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.cfg;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Maps the specified configuration to a dashboard specification.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DashboardMapper {

    // checkstyle: stop parameter number check

    /**
     * Maps the specified configuration to a dashboard specification.
     * 
     * @param mainModelName the name of the configuration model to load/parse
     * @param modelFolder the folder where the configuration model can be found
     * @param metaModelFolder optional folder where the meta model can be found, may be <b>null</b> for none
     * @param outputFile optional output file where the dashboard specification shall be wrote to, if <b>null</b> a 
     *     default location will be assumed
     * @param pluginId the optional plugin id to use (may be <b>null</b> for the default, must be loaded before)
     * @param postUrl post the result directly to a HTTP/REST API, may be <b>null</b> or empty for none
     * @return the actual output location, may be {@code outputFile} if that was given before
     * @throws ExecutionException if mapping fails
     */
    public File mapConfigurationToDashboard(String mainModelName, File modelFolder, File metaModelFolder, 
        File outputFile, String pluginId, String postUrl) throws ExecutionException;
    
    /**
     * Maps the specified configuration to a dashboard specification as process.
     * 
     * @param mainModelName the name of the configuration model to load/parse
     * @param modelFolder the folder where the configuration model can be found
     * @param metaModelFolder optional folder where the meta model can be found, may be <b>null</b> for none
     * @param outputFile optional output file where the dashboard specification shall be wrote to, if <b>null</b> a 
     *     default location will be assumed
     * @param plugins the folders of the plugins to load
     * @param pluginId the optional plugin id to use (may be <b>null</b> for the default, must be loaded before)
     * @param postUrl post the result directly to a HTTP/REST API, may be <b>null</b> or empty for none
     */
    public void mapConfigurationToDashboardAsProcess(String mainModelName, File modelFolder, File metaModelFolder, 
        File outputFile, List<File> plugins, String pluginId, String postUrl) throws ExecutionException;

    // checkstyle: resume parameter number check

}
