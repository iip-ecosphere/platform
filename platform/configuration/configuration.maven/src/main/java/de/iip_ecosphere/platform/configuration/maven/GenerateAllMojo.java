
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

import javax.inject.Inject;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.ProjectBuilder;
import org.eclipse.aether.RepositorySystem;

/**
 * Generates everything.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "generateAll", defaultPhase = LifecyclePhase.PACKAGE)
public class GenerateAllMojo extends AbstractConfigurationMojo {

    /**
     * Creates an instance.
     * 
     * @param projectBuilder the project builder
     * @param repoSystem the repo system
     */
    @Inject
    public GenerateAllMojo(ProjectBuilder projectBuilder, 
        RepositorySystem repoSystem) {
        super(projectBuilder, repoSystem);
    }
    
    @Override
    public String getStartRule() {
        return "mainCli"; // intentionally different from goal
    }
    
    @Override
    protected boolean enableRun(String metaModelDir, String modelDir, String outputDir) {
        return !checkChanged() || super.enableRun(metaModelDir, modelDir, outputDir);
    }

}
