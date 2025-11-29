/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.dependency.fromConfiguration.ArtifactItem;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.ArtifactRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

/**
 * Simple re-usable Maven artifact resolver.
 * 
 * @author Holger Eichelberger, SSE
 */
class Resolver {

    private Log log;
    private List<RemoteRepository> remoteRepositories;
    private RepositorySystemSession repoSession;
    private RepositorySystem repoSystem;
    
    /**
     * Creates a resolver instance.
     * 
     * @param repoSystem the repo system
     * @param repoSession the repo session
     * @param remoteRepositories the remote repositories
     * @param log the logger
     */
    Resolver(RepositorySystem repoSystem, RepositorySystemSession repoSession, 
        List<RemoteRepository> remoteRepositories, Log log) {
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.remoteRepositories = remoteRepositories;
        this.log = log;
    }
    
    /**
     * Translates a maven artifact to an Aether artifact.
     * 
     * @param artifact the maven artifact
     * @return the aether artifact
     */
    DefaultArtifact translate(Artifact artifact) {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), 
            artifact.getType(), artifact.getVersion());            
    }

    /**
     * Translates a maven artifact item to an Aether artifact.
     * 
     * @param artifact the maven artifact
     * @return the aether artifact
     */
    DefaultArtifact translate(ArtifactItem artifact) {
        return new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), 
            artifact.getType(), artifact.getVersion());            
    }

    /**
     * Resolves a single artifact.
     * 
     * @param artifact the artifact
     * @return the resolved path, empty if not resolvable
     */
    String resolve(Artifact artifact) {
        String result;
        try {
            result = resolve(translate(artifact)).getAbsolutePath();
        } catch (ArtifactResolutionException e) {
            getLog().warn("Cannot resolve to artifact, ignoring: " + artifact);
            result = "";
        }
        return result;
    }

    /**
     * Resolves an artifact via the repository system.
     * 
     * @param artifact the artifact to resolve
     * @return the resolved path
     * @throws ArtifactResolutionException if resolution fails
     */
    File resolve(DefaultArtifact artifact) throws ArtifactResolutionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepositories);
        ArtifactResult result = repoSystem.resolveArtifact(repoSession, request);
        return result.getArtifact().getFile();
    }

    /**
     * Resolves a single artifact.
     * 
     * @param artifact the artifact
     * @return the resolved path, empty if not resolvable
     */
    String resolveToUrl(Artifact artifact) {
        String result;
        try {
            result = resolveToUrl(translate(artifact));
        } catch (ArtifactResolutionException e) {
            getLog().warn("Cannot resolve to artifact, ignoring: " + artifact);
            result = "";
        }
        return result;
    }

    /**
     * Resolves a single artifact item.
     * 
     * @param artifact the artifact
     * @return the resolved path, empty if not resolvable
     */
    String resolveToUrl(ArtifactItem artifact) {
        String result;
        try {
            result = resolveToUrl(translate(artifact));
        } catch (ArtifactResolutionException e) {
            getLog().warn("Cannot resolve to artifact, ignoring: " + artifact);
            result = "";
        }
        return result;
    }

    /**
     * Resolves an artifact to a download URL.
     * 
     * @param artifact the artifact to resolve
     * @return the download URL, may be <b>null</b> for none
     * @throws ArtifactResolutionException
     */
    String resolveToUrl(DefaultArtifact artifact) throws ArtifactResolutionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepositories);
        ArtifactResult result = repoSystem.resolveArtifact(repoSession, request);
        ArtifactRepository repo = result.getRepository();
        String url = null;
        if (repo instanceof RemoteRepository) {
            RemoteRepository r = (RemoteRepository) repo;
            url = r.getUrl();
        } // Local and Workspace repo do not need downloads
        if (null != url) {
            org.eclipse.aether.artifact.Artifact art = result.getArtifact();
            url += "/" + art.getGroupId().replace('.', '/') + "/" 
                + art.getArtifactId() + "/" 
                + art.getBaseVersion() + "/" 
                + art.getArtifactId() + "-" 
                + art.getVersion()
                + (art.getClassifier() != null && !art.getClassifier().isEmpty()
                        ? "-" + art.getClassifier()
                        : "") 
                + "." + art.getExtension();
        }
        return url;
    }

    /**
     * Resolves a list of artifacts to a composed path.
     * 
     * @param artifacts the artifacts, may be <b>null</b>
     * @return the path, may be <b>empty</b>
     */
    String resolve(List<Artifact> artifacts) {
        return null == artifacts ? "" : artifacts.stream()
            .map(a -> resolve(a))
            .filter(p -> p != null)
            .collect(Collectors.joining(":", ":", ""));
    }

    /**
     * Resolves the spring boot loader from the oktoflow spring property {@code org.springframework.boot.version}.
     * 
     * @param project the Maven project to resolve on
     * @return the resolved file, <b>null</b> if there is none
     */
    File resolveSpringBootLoader(MavenProject project) {
        File result = null;
        Object springVersion = project.getProperties().get("org.springframework.boot.version");
        if (null != springVersion) {
            DefaultArtifact art = new DefaultArtifact("org.springframework.boot", "spring-boot-loader", "jar", 
                springVersion.toString());
            try {
                result = resolve(art);
            } catch (ArtifactResolutionException e) {
                getLog().error("Cannot resolve: " + e.getMessage());
            }
        }  else {
            getLog().error("Cannot find spring version!");
        }
        return result;
    }

    /**
     * The log.
     * 
     * @return the log
     */
    private Log getLog() {
        return log;
    }

}
