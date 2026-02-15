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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
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

    public static final String DEFAULT_UNPACK_MODE = "JARS"; // -> TODO "resolve", see toUnpackMode()

    private Log log;
    private List<RemoteRepository> remoteRepositories;
    private RepositorySystemSession repoSession;
    private RepositorySystem repoSystem;
    
    enum UnpackMode {
        JARS,
        SNAPSHOTS,
        RESOLVE
    }
    
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
        String url = null;
        if (null != result) {
            ArtifactRepository repo = result.getRepository();
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
    
    // checkstyle: stop parameter number check
    
    /**
     * Writes the dependency resolution file.
     * 
     * @param <T> the type of dependency item
     * @param file the file to write
     * @param items the dependency items
     * @param unpackModeProvider provides the unpack mode for an item (use {@link UnpackMode#RESOLVE} for all, 
     *     {@link UnpackMode#JARS} for none)
     * @param resolutionProvider resolves an item to its dependency URL (may lead to an empty or <b>null</b> value, 
     *     ignored then)
     * @param artifactIdProvider resolves an item to its artifact id
     * @param versionProvider resolves an item to its version
     */
    <T> void writeResolvedFile(File file, Collection<T> items, Function<T, UnpackMode> unpackModeProvider, 
        Function<T, String> resolutionProvider, Function <T, String> artifactIdProvider, Function <T, 
        String> versionProvider, boolean plugin) {
        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            out.println("[");
            boolean first = true;
            for (T p : items) {
                String artifactId = artifactIdProvider.apply(p);
                String name = artifactId + "-" + versionProvider.apply(p);
                UnpackMode unpackMode = unpackModeProvider.apply(p);
                if (null == unpackMode) {
                    unpackMode = toUnpackMode(DEFAULT_UNPACK_MODE);
                }
                if (include(unpackMode, name)) {
                    String urlPath = resolutionProvider.apply(p);
                    if (null != urlPath && urlPath.length() > 0) {
                        if (!first) {
                            out.println(",");
                        }
                        out.print("  {\"url\":\"");
                        out.print(urlPath);
                        out.print("\", \"plugin\":");
                        out.print(plugin);
                        out.print(", \"name\":\"");
                        out.print(name);
                        out.print("\"}");
                        first = false;
                    }
                }
            }
            if (!first) {
                out.println();
            }
            out.println("]");
            getLog().info("Wrote resolution file " + file);
        } catch (IOException e) {
            getLog().error("While writing resolution file " + file + ": " + e.getMessage());
        }
    }

    // checkstyle: resume parameter number check

    /**
     * The log.
     * 
     * @return the log
     */
    private Log getLog() {
        return log;
    }

    /**
     * Returns whether the given file/artifact name is a snapshot.
     *
     * @param name the name
     * @return {@code true} for snapshot, {@code false} else
     */
    static boolean isSnapshot(String name) {
        return name.contains("-SNAPSHOT"); // simplistic
    }

    /**
     * Determines whether an artifact/file with given {@code name} shall be included (to a Jar, the resolved file).
     * 
     * @param file the file to take the name from
     * @return {@code true} for inclusion, {@code false} else
     */
    static boolean include(UnpackMode unpackMode, File file) {
        return include(unpackMode, file.getName());
    }

    /**
     * Determines whether an artifact/file with given {@code name} shall be included (to a Jar, the resolved file).
     * 
     * @param name the name (prefix)
     * @return {@code true} for inclusion, {@code false} else
     */
    static boolean include(UnpackMode unpackMode, String name) {
        boolean add;
        switch (unpackMode) {
        case JARS:
            add = true;
            break;
        case RESOLVE:
            add = false;
            break;
        case SNAPSHOTS:
            add = Resolver.isSnapshot(name);
            break;
        default:
            add = false;
            break;
        }
        return add;
    }
    
    /**
     * Turns a string to an unpack mode.
     * 
     * @param mode the string, may be <b>null</b> then the unpack mode of {@link #DEFAULT_UNPACK_MODE}
     * @return the unpack mode
     */
    static UnpackMode toUnpackMode(String mode) {
        UnpackMode result;
        if (null == mode) {
            mode = DEFAULT_UNPACK_MODE;
        }
        try {
            result = UnpackMode.valueOf(mode);
        } catch (IllegalArgumentException ex) {
            result = UnpackMode.JARS;
        }
        return result;
    }

}
