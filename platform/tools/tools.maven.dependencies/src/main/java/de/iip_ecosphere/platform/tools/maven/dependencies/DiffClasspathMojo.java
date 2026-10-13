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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.dependency.AbstractDependencyMojo;
import org.apache.maven.plugins.dependency.utils.DependencyStatusSets;
import org.apache.maven.plugins.dependency.utils.DependencyUtil;
import org.apache.maven.plugins.dependency.utils.ResolverUtil;
import org.apache.maven.plugins.dependency.utils.translators.ArtifactTranslator;
import org.apache.maven.plugins.dependency.utils.translators.ClassifierTypeTranslator;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ClassifierFilter;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.GroupIdFilter;
import org.apache.maven.shared.artifact.filter.collection.ProjectTransitivityFilter;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Reused build-classpath Mojo to write a diffed classpath from {@link DiffClasspathMojo#rootCoordinates} and the 
 * containing project.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "diff-classpath", requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME, 
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, 
    defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class DiffClasspathMojo extends org.apache.maven.plugins.dependency.fromDependencies.BuildClasspathMojo {

    private ArtifactResolver artifactResolver;
    
    private ProjectBuilder projectBuilder;

    @Parameter( property = "mdep.outputFile", required = true )
    private File outputFile;
    
    @Parameter( property = "mdep.pathSeparator", defaultValue = "", required = true )
    private String pathSeparator;

    @Parameter( property = "mdep.rootCoordinates", required = true )
    private Set<String> rootCoordinates;
    
    @Parameter( defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true )
    private List<ArtifactRepository> remoteArtifactRepositories;
    
    private ArtifactHandlerManager artifactHandlerManager;
    
    private ResolverUtil resolverUtil;

    // checkstyle: stop parameter number check
    
    /**
     * Creates a mojo instance.
     * 
     * @param session the maven session
     * @param buildContext the build context
     * @param project the project instance
     * @param resolverUtil the resolver util instance
     * @param projectBuilder the project builder
     * @param artifactHandlerManager the artifact handler manager
     * @param projectHelper the project helper
     * @param artifactResolver the artifact resolver instance
     */    
    @Inject
    protected DiffClasspathMojo(
        MavenSession session,
        BuildContext buildContext,
        MavenProject project,
        ResolverUtil resolverUtil,
        ProjectBuilder projectBuilder,
        ArtifactHandlerManager artifactHandlerManager,
        MavenProjectHelper projectHelper,
        ArtifactResolver artifactResolver) {
        super(session, buildContext, project, resolverUtil, projectBuilder, artifactHandlerManager, projectHelper);
        this.artifactHandlerManager = artifactHandlerManager;
        this.resolverUtil = resolverUtil;
        this.projectBuilder = projectBuilder;
        this.artifactResolver = artifactResolver;
    }

    // checkstyle: resume parameter number check

    @Override
    public void setOutputFile(File outputFile) {
        super.setOutputFile(outputFile);
        this.outputFile = outputFile;
    }
    
    @Override
    public void setPathSeparator(String thePathSeparator) {
        super.setPathSeparator(thePathSeparator);
        this.pathSeparator = thePathSeparator;
    }
    
    /**
     * Returns the coordinate of {@code artifact}.
     * 
     * @param artifact the artifact
     * @return the coordinate
     */
    private String getCoordinate(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getBaseVersion();
    }

    /**
     * Returns the coordinate of {@code dependency}.
     * 
     * @param dependency the dependency
     * @return the coordinate
     */
    private String getCoordinate(Dependency dependency) {
        return dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
    }
    
    /**
     * Collects all transitive dependencies of {@code art} with artifacts taken from {@code coordMapping} writing
     * not so far known results to {@code artifacts}. There are for sure better ways...
     *  
     * @param artifact the artifact to resolve
     * @param coordMapping the coordinate-to-artifact mapping
     * @param artifacts the resulting artifacts containing the transitive dependencies, to be modified as a side effect
     * @throws MojoExecutionException if maven projects cannot be built from artifact information
     */
    private void collectDependencies(Artifact artifact, Map<String, Artifact> coordMapping, Set<Artifact> artifacts) 
        throws MojoExecutionException {
        MavenProject prj = buildProjectFromArtifact(artifact);
        for (Dependency d : prj.getDependencies()) {
            Artifact a = coordMapping.get(getCoordinate(d));
            if (null != a && !artifacts.contains(a)) {
                artifacts.add(a);
                collectDependencies(a, coordMapping, artifacts);
            }
        }
    }

    // taken over from {@link AbstractDependencyMojo} with replaced dependency resolution
    @SuppressWarnings("deprecation")
    @Override
    protected DependencyStatusSets getDependencySets(boolean stopOnFailure, boolean includeParents) 
        throws MojoExecutionException {
        // add filters in well known order, least specific to most specific
        FilterArtifacts filter = new FilterArtifacts();

        filter.addFilter(new ProjectTransitivityFilter(getProject().getDependencyArtifacts(), this.excludeTransitive));

        if ("test".equals(this.excludeScope)) {
            throw new MojoExecutionException("Excluding every artifact inside 'test' resolution scope means "
                    + "excluding everything: you probably want includeScope='compile', "
                    + "read parameters documentation for detailed explanations");
        }
        filter.addFilter(new ScopeFilter(DependencyUtil.cleanToBeTokenizedString(this.includeScope),
                DependencyUtil.cleanToBeTokenizedString(this.excludeScope)));

        filter.addFilter(new TypeFilter(DependencyUtil.cleanToBeTokenizedString(this.includeTypes),
                DependencyUtil.cleanToBeTokenizedString(this.excludeTypes)));

        filter.addFilter(new ClassifierFilter(DependencyUtil.cleanToBeTokenizedString(this.includeClassifiers),
                DependencyUtil.cleanToBeTokenizedString(this.excludeClassifiers)));

        filter.addFilter(new GroupIdFilter(DependencyUtil.cleanToBeTokenizedString(this.includeGroupIds),
                DependencyUtil.cleanToBeTokenizedString(this.excludeGroupIds)));

        filter.addFilter(new ArtifactIdFilter(DependencyUtil.cleanToBeTokenizedString(this.includeArtifactIds),
                DependencyUtil.cleanToBeTokenizedString(this.excludeArtifactIds)));

        // start with all artifacts.
        Set<Artifact> artifacts = getProject().getArtifacts();
        
        // -> different from original, filtering out irrelevant dependencies 
        getLog().info("Resolving dependencies of " + rootCoordinates);
        Map<String, Artifact> coordMapping = new HashMap<>();
        artifacts.forEach(a -> {
            coordMapping.put(getCoordinate(a), a);
        });
        artifacts.removeIf(a -> {
            String coordinate = getCoordinate(a);
            return !rootCoordinates.contains(coordinate);
        });
        for (Artifact dep : new ArrayList<>(artifacts)) {
            collectDependencies(dep, coordMapping, artifacts);
        }
        // -> same as before

        if (includeParents) {
            // add dependencies parents
            for (Artifact dep : new ArrayList<>(artifacts)) {
                addParentArtifacts(buildProjectFromArtifact(dep), artifacts);
            }

            // add current project parent
            addParentArtifacts(getProject(), artifacts);
        }

        // perform filtering
        try {
            artifacts = filter.filter(artifacts);
        } catch (ArtifactFilterException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // transform artifacts if classifier is set
        DependencyStatusSets status;
        if (StringUtils.isNotEmpty(classifier)) {
            status = getClassifierTranslatedDependencies(artifacts, stopOnFailure);
        } else {
            status = filterMarkedDependencies(artifacts);
        }

        return status;
    }
    
    // >>> taken over as private now, may need updating

    /**
     * Transform artifacts.
     *
     * @param artifacts set of artifacts {@link Artifact}
     * @param stopOnFailure true/false
     * @return DependencyStatusSets - Bean of TreeSets that contains information on the projects dependencies
     * @throws MojoExecutionException in case of an error
     */
    private DependencyStatusSets getClassifierTranslatedDependencies(Set<Artifact> artifacts, boolean stopOnFailure)
            throws MojoExecutionException {
        Set<Artifact> unResolvedArtifacts = new LinkedHashSet<>();
        Set<Artifact> resolvedArtifacts = artifacts;
        DependencyStatusSets status = new DependencyStatusSets();

        // possibly translate artifacts into a new set of artifacts based on the
        // classifier and type
        // if this did something, we need to resolve the new artifacts
        if (classifier != null && !classifier.isEmpty()) {
            ArtifactTranslator translator =
                    new ClassifierTypeTranslator(artifactHandlerManager, this.classifier, this.type);
            Collection<org.eclipse.aether.artifact.Artifact> coordinates = translator.translate(artifacts, getLog());

            status = filterMarkedDependencies(artifacts);

            // the unskipped artifacts are in the resolved set.
            artifacts = status.getResolvedDependencies();

            // resolve the rest of the artifacts
            resolvedArtifacts = resolve(new LinkedHashSet<>(coordinates), stopOnFailure);

            // calculate the artifacts not resolved.
            unResolvedArtifacts.addAll(artifacts);
            unResolvedArtifacts.removeAll(resolvedArtifacts);
        }

        // return a bean of all 3 sets.
        status.setResolvedDependencies(resolvedArtifacts);
        status.setUnResolvedDependencies(unResolvedArtifacts);

        return status;
    }
    
    /**
     * Resolves artifacts.
     * 
     * @param artifacts the set of artifacts
     * @param stopOnFailure <code>true</code> if we should fail with exception if an artifact couldn't be resolved
     *            <code>false</code> otherwise
     * @return the resolved artifacts. {@link Artifact}.
     * @throws MojoExecutionException in case of error
     */
    private Set<Artifact> resolve(Set<org.eclipse.aether.artifact.Artifact> artifacts, boolean stopOnFailure)
            throws MojoExecutionException {

        Set<Artifact> resolvedArtifacts = new LinkedHashSet<>();
        for (org.eclipse.aether.artifact.Artifact artifact : artifacts) {
            try {
                org.eclipse.aether.artifact.Artifact resolveArtifact =
                        resolverUtil.resolveArtifact(artifact, getProject().getRemoteProjectRepositories());
                resolvedArtifacts.add(RepositoryUtils.toArtifact(resolveArtifact));
            } catch (ArtifactResolutionException | ArtifactDescriptorException ex) {
                // an error occurred during resolution, log it and continue
                getLog().debug("error resolving: " + artifact, ex);
                if (stopOnFailure) {
                    throw new MojoExecutionException("error resolving: " + artifact, ex);
                }
            }
        }
        return resolvedArtifacts;
    }
    
    // <<< taken over as private now, may need updating
    
    /**
     * Creates a maven project instance from the given artifact. Taken over from {@link AbstractDependencyMojo} as
     * not accessible.
     * 
     * @param artifact the artifact
     * @return the maven project
     * @throws MojoExecutionException the project instance cannot be built
     */
    private MavenProject buildProjectFromArtifact(Artifact artifact) throws MojoExecutionException {
        try {
            return projectBuilder.build(artifact, session.getProjectBuildingRequest()).getProject();
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
    
    /**
     * Adds parent artifacts. Taken over from {@link AbstractDependencyMojo} as not accessible.
     * 
     * @param project the project to take the artifacts from
     * @param artifacts the artifacts set to be modified
     * @throws MojoExecutionException in case that artifact resolution fails
     */
    private void addParentArtifacts(MavenProject project, Set<Artifact> artifacts) throws MojoExecutionException {
        while (project.hasParent()) {
            project = project.getParent();
            if (artifacts.contains(project.getArtifact())) {
                // artifact already in the set
                break;
            }
            try {
                ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(
                    session.getProjectBuildingRequest());
                buildingRequest.setRemoteRepositories(remoteArtifactRepositories);
                Artifact resolvedArtifact = artifactResolver.resolveArtifact(buildingRequest, project.getArtifact())
                    .getArtifact();
                artifacts.add(resolvedArtifact);
            } catch (ArtifactResolverException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }    

}
