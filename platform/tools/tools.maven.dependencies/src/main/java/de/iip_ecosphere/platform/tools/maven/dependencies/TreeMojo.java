/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyCollectorBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;

/**
 * Just maps tree into this plugin.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "tree", requiresDependencyCollection = ResolutionScope.TEST, threadSafe = true )
public class TreeMojo extends org.apache.maven.plugins.dependency.tree.TreeMojo {

    /**
     * Creates a mojo instance.
     * 
     * @param project the project
     * @param session the session
     * @param dependencyCollectorBuilder the dependency collector builder
     * @param dependencyGraphBuilder the dependency graph builder
     */
    @Inject
    public TreeMojo(
        MavenProject project,
        MavenSession session,
        DependencyCollectorBuilder dependencyCollectorBuilder,
        DependencyGraphBuilder dependencyGraphBuilder) {
        super(project, session, dependencyCollectorBuilder, dependencyGraphBuilder);
    }    

}
