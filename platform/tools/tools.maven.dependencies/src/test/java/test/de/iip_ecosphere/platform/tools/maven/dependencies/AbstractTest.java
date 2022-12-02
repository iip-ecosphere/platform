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

package test.de.iip_ecosphere.platform.tools.maven.dependencies;

import java.io.File;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;

/**
 * Abstract basic Mojo test for missing functionality in Mvn test harness.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractTest extends AbstractMojoTestCase {

    // checkstyle: stop exception type check
    
    // https://stackoverflow.com/questions/70921236/how-to-make-mavenproject-injected-into-the-mojo-during-test-lookup
    
    /**
     * Reads the maven project in {@code basedir}.
     * 
     * @param basedir the project base dir
     * @return the maven project instance
     * @throws ProjectBuildingException if the project cannot be build
     * @throws Exception if the project cannot be build
     */
    protected MavenProject readMavenProject(File basedir) throws ProjectBuildingException, Exception {
        File pom = new File( basedir, "pom.xml" );
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setBaseDirectory(basedir);
        ProjectBuildingRequest configuration = request.getProjectBuildingRequest();
        configuration.setRepositorySession(new DefaultRepositorySystemSession());
        MavenProject project = lookup(ProjectBuilder.class).build(pom, configuration).getProject();
        assertNotNull(project);
        return project;
    }
    
    // checkstyle: resume exception type check
    
}
