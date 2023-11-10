
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

package de.iip_ecosphere.platform.configuration.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.iip_ecosphere.platform.support.collector.Collector;
/**
 * Generates the configured applications including dependencies to the implementing services.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "generateApps", defaultPhase = LifecyclePhase.PACKAGE)
public class GenerateAppsMojo extends AbstractAppsConfigurationMojo {

    @Parameter(property = "configuration.checkChanged", required = false, defaultValue = "false")
    private boolean checkChanged;
    
    @Override
    public String getStartRule() {
        return "generateApps";
    }
    
    @Override
    protected boolean enableRun(String metaModelDir, String modelDir, String outputDir) {
        return !checkChanged() || super.enableRun(metaModelDir, modelDir, outputDir);
    }

    /**
     * Called to record the execution time.
     * 
     * @param time the passed time in ms
     */
    protected void recordExecutionTime(long time) {
        Collector.collect(getProject().getArtifactId() + "-" + getModel() + "-generateApps")
            .addExecutionTimeMs(time)
            .close();
    }

}
