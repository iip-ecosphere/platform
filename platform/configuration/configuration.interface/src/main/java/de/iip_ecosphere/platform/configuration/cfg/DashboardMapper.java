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
import java.util.concurrent.ExecutionException;

/**
 * Maps the specified configuration to a dashboard specification.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface DashboardMapper {

    /**
     * Maps the specified configuration to a dashboard specification.
     * 
     * @param mainModelName the name of the configuration model to load/parse
     * @param modelFolder the folder where the configuration model can be found
     * @param metaModelFolder optional folder where the meta model can be found, may be <b>null</b> for none
     * @param outputFile optional output file where the dashboard specification shall be wrote to, if <b>null</b> a 
     *     default location will be assumed
     * @param pluginId the optional plugin id to use (may be <b>null</b> for the default, must be loaded before)
     * @return the actual output location, may be {@code outputFile} if that was given before
     * @throws ExecutionException if mapping fails
     */
    public File mapConfigurationToDashboard(String mainModelName, File modelFolder, File metaModelFolder, 
        File outputFile, String pluginId) throws ExecutionException;
    
}
