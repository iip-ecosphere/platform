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

package de.iip_ecosphere.platform.tools.maven.dependencies;

import javax.inject.Inject;

import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.dependency.utils.CopyUtil;
import org.apache.maven.plugins.dependency.utils.ResolverUtil;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Reused copy Mojo.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "copy", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresProject = false, threadSafe = true )
public class CopyMojo extends org.apache.maven.plugins.dependency.fromConfiguration.CopyMojo {

    // checkstyle: stop parameter number check
    
    /**
     * Creates the mojo instance.
     * 
     * @param session the session
     * @param buildContext the build context
     * @param project the project instance
     * @param artifactHandlerManager the artifact handler manager
     * @param copyUtil the copy util instance
     * @param repositorySystem the repository system
     * @param resolverUtil the resolver util
     */
    @Inject
    public CopyMojo(
        MavenSession session,
        BuildContext buildContext,
        MavenProject project,
        ArtifactHandlerManager artifactHandlerManager,
        CopyUtil copyUtil,
        RepositorySystem repositorySystem,
        ResolverUtil resolverUtil) {
        super(session, buildContext, project, artifactHandlerManager, copyUtil, repositorySystem, resolverUtil);
    }

    // checkstyle: resume parameter number check

}
