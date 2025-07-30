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

package de.iip_ecosphere.platform.tools.maven.dependencies;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Reused build-classpath Mojo.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "copy-plugin-dependencies", requiresDependencyResolution = ResolutionScope.TEST, 
    defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true )
public class CopyPluginDependenciesMojo extends CopyDependenciesMojo {

    @Parameter( property = "outputDirectory", defaultValue = "" )
    protected File outputDirectory;
    
    @Parameter( property = "mdep.addTestArtifact", defaultValue = "false" )
    private boolean addTestArtifact;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File targetDirectory;

    @Override
    protected void doExecute() throws MojoExecutionException {
        excludeArtifactIds = Layers.getExcludeArtifactIds(getProject().getArtifactId(), excludeArtifactIds, getLog());
        setPrependGroupId(true);
        if (outputDirectory == null || outputDirectory.getName().equals("")) {
            setOutputDirectory(new File(targetDirectory, "jars"));
        }
        overWriteReleases = false;
        overWriteSnapshots = true;
        overWriteIfNewer = true;
        if (null == includeScope || includeScope.length() == 0) { // if not defined, default it
            if (addTestArtifact) {
                includeScope = "test";
            } else {
                includeScope = "runtime";
            }
        }
        super.doExecute();
    }

}
