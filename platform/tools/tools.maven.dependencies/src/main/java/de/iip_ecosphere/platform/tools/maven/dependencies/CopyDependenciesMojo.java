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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * Reused build-classpath Mojo.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "copy-dependencies", requiresDependencyResolution = ResolutionScope.TEST, 
    defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true )
public class CopyDependenciesMojo extends org.apache.maven.plugins.dependency.fromDependencies.CopyDependenciesMojo {

    @Parameter( property = "mdep.addAppLoader", defaultValue = "false" )
    private boolean addAppLoader;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepositories;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Parameter( defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true )
    private List<ArtifactRepository> remoteArtifactRepositories;
    
    @Component
    private RepositorySystem repoSystem;

    @Override
    protected void doExecute() throws MojoExecutionException {
        super.doExecute();
        if (addAppLoader) {
            Resolver resolver = new Resolver(repoSystem, repoSession, remoteRepositories, getLog());
            File file = resolver.resolveSpringBootLoader(getProject());
            if (null != file) {
                try {
                    Files.copy(file.toPath(), new File(outputDirectory, file.getName()).toPath(), 
                        StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    getLog().error("Cannot copy: " + e.getMessage());
                }
            }
        }
    }
    
    @Override
    public ProjectBuildingRequest newResolveArtifactProjectBuildingRequest() {
        // no artifact resolution so far
        ProjectBuildingRequest buildingRequest =
            new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        buildingRequest.setRemoteRepositories(remoteArtifactRepositories);
        return buildingRequest;
    }

}
