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

import javax.inject.Inject;

import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.dependency.utils.CopyUtil;
import org.apache.maven.plugins.dependency.utils.ResolverUtil;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.shared.transfer.artifact.install.ArtifactInstaller;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.sonatype.plexus.build.incremental.BuildContext;

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
    
    private RepositorySystem repoSystem;
    
    // checkstyle: stop parameter number check

    /**
     * Creates a mojo instance.
     * 
     * @param session the session
     * @param buildContext the build context
     * @param project the project
     * @param resolverUtil the resolver utilities
     * @param projectBuilder the project builder
     * @param artifactHandlerManager the artifact handler manager
     * @param installer the installer
     * @param repoSystem the repository system instance
     */
    @Inject
    public CopyDependenciesMojo(
        MavenSession session,
        BuildContext buildContext,
        MavenProject project,
        ResolverUtil resolverUtil,
        RepositoryManager repositoryManager,
        ProjectBuilder projectBuilder,
        ArtifactHandlerManager artifactHandlerManager,
        CopyUtil copyUtil,
        ArtifactInstaller installer,
        RepositorySystem repoSystem) {
        super(session, buildContext, project, resolverUtil, repositoryManager, projectBuilder, artifactHandlerManager, 
            copyUtil, installer);
        this.repoSystem = repoSystem;
    }
    
    // checkstyle: resume parameter number check

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

}
