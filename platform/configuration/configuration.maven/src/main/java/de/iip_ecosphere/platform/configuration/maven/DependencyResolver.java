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

package de.iip_ecosphere.platform.configuration.maven;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * Simple Maven (snapshot) dependency resolver. For sure, there are better and easier ways...
 * 
 * @author Holger Eichelberger, SSE
 */
public class DependencyResolver {
    
    private static final ArtifactHandler JAR_HANDLER = new DefaultArtifactHandler("jar");
    private Caller caller;
    private Set<Artifact> done = new HashSet<>();

    /**
     * Information needed for dependency resolution.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Caller {

        /**
         * Returns the maven session.
         * 
         * @return the session
         */
        public MavenSession getSession();

        /**
         * Returns the maven project.
         * 
         * @return the project
         */
        public MavenProject getProject();

        /**
         * Returns the maven project builder.
         * 
         * @return the builder
         */
        public ProjectBuilder getProjectBuilder();

        /**
         * Returns the maven remote repositories.
         * 
         * @return the repositories
         */
        public default List<RemoteRepository> getRemoteRepos() {
            return getProject().getRemoteProjectRepositories();
        }

        /**
         * Returns the maven repository system.
         * 
         * @return the repositories
         */
        public RepositorySystem getRepoSystem();
        
        /**
         * Returns the maven repository session.
         * 
         * @return the repositoriy session
         */
        public default RepositorySystemSession getRepoSession() {
            return getSession().getRepositorySession();
        }

        /**
         * Returns the maven log instance.
         * 
         * @return the log instance
         */
        public Log getLog();
     
    }
    
    /**
     * Creates a dependency resolver with caller information.
     * 
     * @param caller the caller
     */
    public DependencyResolver(Caller caller) {
        this.caller = caller;
    }
    
    /**
     * Tries to resolve {@code artifact}.
     * 
     * @param artifact the artifact to resolve
     * @return the resolved artifact, <b>null</b> for none
     */
    private Artifact resolve(Artifact artifact) {
        DefaultArtifact result = null;
        try {
            ArtifactRequest request = new ArtifactRequest();
            request.setArtifact(new org.eclipse.aether.artifact.DefaultArtifact(artifact.getGroupId(), 
                artifact.getArtifactId(), artifact.getClassifier(), artifact.getType(), artifact.getVersion()));
            request.setRepositories(caller.getRemoteRepos());
            ArtifactResult aetherResult = caller.getRepoSystem().resolveArtifact(caller.getRepoSession(), request);
            if (aetherResult.isResolved()) {
                org.eclipse.aether.artifact.Artifact art = aetherResult.getArtifact();
                if (art.getFile() != null) {
                    result = new DefaultArtifact(art.getGroupId(), art.getArtifactId(), art.getVersion(), 
                        artifact.getScope(), art.getExtension(), art.getClassifier(), JAR_HANDLER);
                    result.setFile(art.getFile());
                }
            }
        } catch (ArtifactResolutionException e) {
            caller.getLog().warn("Artifact resolution problem: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns whether the given (snapshot) {@code artifact} or its dependencies has changed
     * {@code since}. Stops artifact checking/resolution as soon as possible.
     * 
     * @param artifact the artifact
     * @param since the change predicate
     * @return {@code true} if one artifact changed, {@code false} else
     */
    private boolean hasChanged(Artifact artifact, Predicate<File> since) {
        boolean changed = false;
        if (artifact.isSnapshot() && !done.contains(artifact)) {
            done.add(artifact);
            Artifact resolved = resolve(artifact);
            if (null != resolved) {
                if (since.test(resolved.getFile())) {
                    changed = true;
                } else {
                    ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(
                        caller.getSession().getProjectBuildingRequest());
                    buildingRequest.setProject(null);
                    try {
                        MavenProject mavenProject = caller.getProjectBuilder().build(artifact, buildingRequest)
                            .getProject();
                        for (Dependency d : mavenProject.getDependencies()) {
                            DefaultArtifact da = new DefaultArtifact(d.getGroupId(), d.getArtifactId(), 
                                d.getVersion(), d.getScope(), "jar", "", JAR_HANDLER);
                            if (hasChanged(da, since)) {
                                changed = true;
                                break;
                            }
                        }     
                    } catch (ProjectBuildingException e) {
                        caller.getLog().warn("Project resolution problem: " + e.getMessage());
                    }
                }
            }
        }
        return changed;
    }
    
    /**
     * Returns whether at least one of the given (snapshot) {@code artifacts} or its (snapshot) dependencies has changed
     * {@code since}. Stops artifact checking/resolution as soon as possible.
     * 
     * @param artifacts the artifacts
     * @param since the change predicate
     * @return {@code true} if one artifact changed, {@code false} else
     */
    public boolean haveDependenciesChangedSince(List<String> artifacts, Predicate<File> since) {
        boolean changed = false;
        for (String a : artifacts) {
            String[] tmp = a.split(":");
            if (tmp.length >= 3) { //groupId:artifactId[:type[:classifier]]:version
                String groupId = tmp[0];
                String artifactId = tmp[1];
                String version;
                String type = "jar";
                String classifier = "";
                if (tmp.length == 3) {
                    version = tmp[2];
                } else if (tmp.length == 4) {
                    type = tmp[2];
                    version = tmp[3];
                } else {
                    type = tmp[2];
                    classifier = tmp [3];
                    version = tmp[4];
                }
                DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId, version, "compile", 
                    type, classifier, JAR_HANDLER);    
                changed = hasChanged(artifact, since);
            }
        }
        return changed;
    }
    
    /**
     * Clears the resolver for reuse.
     */
    public void clear() {
        done.clear();
    }
    
}
