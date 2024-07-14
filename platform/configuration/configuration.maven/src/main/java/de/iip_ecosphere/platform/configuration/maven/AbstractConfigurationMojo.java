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

package de.iip_ecosphere.platform.configuration.maven;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

import de.iip_ecosphere.platform.configuration.PlatformInstantiatorExecutor;
import de.iip_ecosphere.platform.configuration.maven.DependencyResolver.Caller;
import de.iip_ecosphere.platform.tools.maven.python.AbstractLoggingMojo;
import de.iip_ecosphere.platform.tools.maven.python.FileChangeDetector;

/**
 * Abstract configuration Mojo with settings for all configuration Mojos.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractConfigurationMojo extends AbstractLoggingMojo implements Caller {
    
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;
    
    @Component
    private ProjectBuilder projectBuilder;

    @Component
    private ArtifactResolver resolver;
    
    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepos;
    
    @Component
    private RepositorySystem repoSystem;
    
    @Parameter(defaultValue = "${mojoExecution}", readonly = true, required = true)
    private MojoExecution mojoExecution;
    
    @Parameter(property = "configuration.model", required = true)
    private String model;

    @Parameter(property = "configuration.modelDirectory", required = true, defaultValue = "src/test/easy")
    private String modelDirectory;

    @Parameter(property = "configuration.metaModelDirectory", required = true, defaultValue = "src/main/easy")
    private String metaModelDirectory;

    @Parameter(property = "configuration.outputDirectory", required = true, defaultValue = "gen")
    private String outputDirectory;

    @Parameter(property = "configuration.resourcesDirectory", required = false, defaultValue = "resources.ipr")
    private String resourcesDirectory;

    @Parameter(property = "configuration.fallbackResourcesDirectory", required = false, defaultValue = "resources")
    private String fallbackResourcesDirectory;

    @Parameter(property = "configuration.tracingLevel", required = false, defaultValue = "TOP")
    private String tracingLevel;

    @Parameter(property = "configuration.adjustOutputDirectoryIfGenBroker", required = false, defaultValue = "trues")
    private boolean adjustOutputDirectoryIfGenBroker;

    @Parameter(property = "configuration.apps", required = false, defaultValue = "")
    private String apps;
    
    @Parameter(property = "configuration.force", required = false, defaultValue = "")
    private boolean force;
    
    @Parameter(property = "configuration.checkChanged", required = false, defaultValue = "false")
    private boolean checkChanged;

    @Parameter(property = "configuration.changeCheckArtifacts", required = false, defaultValue = "")
    private String changeCheckArtifacts;

    // different name, hook up with unpack
    @Parameter(property = "unpack.force", required = false, defaultValue = "false") 
    private boolean unpackForce;

    @Parameter(required = false, defaultValue = "true")
    private boolean asProcess;

    @Override
    public MavenProject getProject() {
        return project;
    }

    /**
     * Returns the model (file) name.
     * 
     * @return the model (file) name
     */
    public String getModel() {
        return model;
    }

    /**
     * Returns the directory containing the model (to be set up as configuration EASy path).
     * 
     * @return the model directory
     */
    public String getModelDirectory() {
        return modelDirectory;
    }

    /**
     * Returns the directory containing the meta model.
     * 
     * @return the meta model directory
     */
    public String getMetaModelDirectory() {
        return metaModelDirectory;
    }

    /**
     * Returns the output directory for generated code.
     * 
     * @return the output directory the output directory
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Returns the path to the optional resources directory.
     * 
     * @return the resources directory
     */
    public String getResourcesDirectory() {
        return resourcesDirectory;
    }

    /**
     * Returns the path to the optional fallback resources directory to be used if {@link #getResourcesDirectory()}
     * does not exist. This allows, e.g., to use {@link #getResourcesDirectory()} for IPR protected resources and the 
     * fallback for open resources.
     * 
     * @return the resources directory
     */
    public String getFallbackResourcesDirectory() {
        return fallbackResourcesDirectory;
    }

    /**
     * Returns the tracing level.
     * 
     * @return the tracing level (ALL, TOP, FUNC)
     */
    public String getTracingLevel() {
        return tracingLevel;
    }
    
    /**
     * A force through the IIP-Ecosphere unpacking plugin is requested.
     * 
     * @return {@code true} for force through unpacking, {@code false} else
     */
    protected boolean getUnpackForce() {
        return unpackForce;
    }
    
    /**
     * Returns whether changed models shall be considered.
     * 
     * @return {@code true} for consider, {@code false} else (default, depends on goal)
     */
    protected boolean checkChanged() {
        return checkChanged;
    }
    
    /**
     * Returns the start rule name.
     * 
     * @return the start rule name
     */
    public abstract String getStartRule();

    /**
     * Returns whether {@link #getModelDirectory()} is valid, i.e., contains at least an IVML file.
     * 
     * @return the model directory
     */
    protected boolean isModelDirectoryValid() {
        boolean result = false;
        File modelDir = new File(getModelDirectory());
        if (modelDir.exists() ) {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.ivml");
            try (Stream<Path> stream = Files.walk(modelDir.toPath())) {
                result = stream.anyMatch(f -> matcher.matches(f.getFileName()));
            } catch (IOException e) {
                // ignore
            }
        }
        return result;
    }
    
    /**
     * Turns {@code directory} into an absolute directory name. If {@code directory} is yet absolute, 
     * return {@code directory}, if not, prepend the project base directory.
     * 
     * @param directory the directory (may be <b>null</b> or empty)
     * @return if {@code directory} is<b>null</b>, empty, or absolute, return {@code directory}. If {@code directory}
     *   is not absolute, prepend {@link MavenProject#getBasedir()} from {@link #project}.
     */
    private String makeAbsolute(String directory) {
        String result = directory;
        if (null != directory && directory.length() > 0) {
            File f = new File(directory);
            if (!f.isAbsolute()) {
                f = new File(project.getBasedir(), directory);
            }
            result = f.getAbsolutePath();
        } 
        return result;
    }

    /**
     * Validates the given string as existing directory.
     * 
     * @param directory the directory to validate, may be <b>null</b>
     * @return if {@code directory} is <b>null</b> or not existing, return <b>null</b> else {@code directory}
     */
    private String validateDirectory(String directory) {
        String result = directory;
        if (null != directory) {
            if (!new File(directory).exists()) {
                result = null;
            }
        }
        return result;
    }
    
    /**
     * Adjusts the output directory if necessary.
     * 
     * @param outputDir the output directory
     * @return the adjusted output directory, by default just {@code outputDir}
     */
    protected String adjustOutputDir(String outputDir) {
        return outputDir;
    }
    
    /**
     * Returns the "gen" parent folder if it exists, if not just {@code outputDir}.
     * 
     * @param outputDir the output directory where to start finding the "gen" parent folder
     * @return the default gen parent folder or {@code outputDir} if not found
     */
    protected File findGenParent(String outputDir) {
        File parent = new File(outputDir);
        File iter = parent;
        boolean genFound = false;
        while (null != iter) {
            if (iter.getName().equals("gen")) {
                genFound = true;
                break;
            }
            iter = iter.getParentFile();
        }
        if (null != iter && genFound) {
            parent = iter;
        }
        return parent;
    }
    
    /**
     * Returns whether {@code modelDir} is considered to be newer than {@code outDir}.
     * 
     * @param metaModelDir the meta-model directory
     * @param modelDir the model directory
     * @param outDir the output directory
     * @return {@code true} if {@code modelDir} is considered to be newer than {@code outDir}, {@code false} else
     */
    protected boolean modelNewerThanOut(String metaModelDir, String modelDir, String outDir) {
        boolean newer = false;
        String execId = null == mojoExecution.getExecutionId() ? "" : "-" + mojoExecution.getExecutionId();
        File metaHashFile = FileChangeDetector.getHashFileInTarget(project, "easy-meta" + execId);
        File modelHashFile = FileChangeDetector.getHashFileInTarget(project, "easy" + execId);
        List<File> metaModelFiles = checkFilesByHash(metaHashFile, metaModelDir, null);
        List<File> modelFiles = checkFilesByHash(modelHashFile, modelDir, "IVML model");
        boolean enabled = enabled(modelFiles) || enabled(metaModelFiles);
        if (metaHashFile.exists() || modelHashFile.exists()) {
            if (null != changeCheckArtifacts && !changeCheckArtifacts.isEmpty()) {
                Predicate<File> pred = f -> isNewer(f, metaHashFile) || isNewer(f, modelHashFile);
                StringTokenizer tokenizer = new StringTokenizer(changeCheckArtifacts);
                List<String> arts = new ArrayList<>();
                while (tokenizer.hasMoreTokens()) {
                    arts.add(tokenizer.nextToken());
                }
                enabled |= new DependencyResolver(this).haveDependenciesChangedSince(arts, pred);
            }
        }
        if (enabled) { 
            long maxModel = getMaxLastModified(modelDir, f -> isModelFile(f));
            long maxOut = getMaxLastModified(outDir, f -> true);
            newer = maxOut < 0 || maxModel > maxOut;
        }
        return newer;
    }

    /**
     * Returns whether {@code file} is an IVML, VIL or VTL file.
     * 
     * @param file the file to check
     * @return {@code true} for one of these files, {@code false} else
     */
    private static boolean isModelFile(File file) {
        String name = file.getName();
        return name.endsWith(".ivml") || name.endsWith(".vil") || name.endsWith(".vtl");
    }
    
    /**
     * Returns whether {@code artifact} is newer than {@code hash}.
     * 
     * @param artifact the artifact file
     * @param hash the hash file
     * @return if {@code artifact} is newer than {@code hash}
     */
    private boolean isNewer(File artifact, File hash) {
        return hash != null && artifact.lastModified() > hash.lastModified();
    }

    /**
     * Returns whether a list of files from hash checking enables further execution.
     * 
     * @param files the files
     * @return {@code true} for enabled, {@code false} else
     */
    private boolean enabled(List<File> files) {
        return null == files || files.size() > 0; // no model, no files found, newer files identified
    }
    
    /**
     * Checks files by MD5 change hashes.
     * 
     * @param hashFile the hash file
     * @param modelDir the directory of files to analyze
     * @param info logging information on the task
     * @return the changed files or <b>null</b> for unsure/none detected
     */
    private List<File> checkFilesByHash(File hashFile, String modelDir, String info) {
        List<File> modelFiles = null;
        File model = new File(modelDir);
        try (Stream<Path> stream = Files.walk(model.toPath())) {
            FileChangeDetector fcd = new FileChangeDetector(hashFile, this, info);
            fcd.readHashFile();
            modelFiles = stream.map(p -> p.toFile()).filter(f->f.isFile()).collect(Collectors.toList());
            modelFiles = fcd.checkHashes(modelFiles);
            fcd.writeHashFile();
        } catch (IOException e) {
            // ignore
        }
        getLog().info("Changed " + info + " files: " + modelFiles);
        return modelFiles;
    }
    
    /**
     * Returns the maximum modification time of the files in {@code dir} fulfilling {@code filter}.
     * 
     * @param dir the directory to analyze
     * @param filter filter on files found in {@code dir}
     * @return the maximum modification time or {@code -1} if no files were found
     */
    protected long getMaxLastModified(String dir, FileFilter filter) {
        long result = -1;
        File d = new File(dir);
        if (d.exists()) {
            try (Stream<Path> stream = Files.walk(d.toPath())) {
                result = stream.map(p -> p.toFile().lastModified()).max(Long::compare).get();
            } catch (IOException e) {
                // ignore
            }
        }
        return result;
    }
    
    /**
     * Called by {@link #execute()} to figure out whether the instantiation shall take place. By default,
     * instantiation will be enabled if IVML files in {@code modelDir} are newer than files in {@code 
     * output directory} or if output directory is empty or missing.
     * 
     * @param metaModelDir the meta model directory
     * @param modelDir the model directory
     * @param outputDir the output directory
     * @return {@code true} for instantiation, {@code false} for no instantiation
     */
    protected boolean enableRun(String metaModelDir, String modelDir, String outputDir) {
        return modelNewerThanOut(metaModelDir, modelDir, outputDir) || force;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String resourcesDir = validateDirectory(makeAbsolute(getResourcesDirectory()));
        if (null == resourcesDir) {
            resourcesDir = validateDirectory(makeAbsolute(getFallbackResourcesDirectory()));
        }
        String outputDir = adjustOutputDir(makeAbsolute(getOutputDirectory()));
        String modelDir = makeAbsolute(getModelDirectory());
        String metaModelDir = makeAbsolute(getMetaModelDirectory());
        String[] args = {getModel(), modelDir, outputDir, getStartRule(), metaModelDir};
        if (isModelDirectoryValid()) {
            if (enableRun(metaModelDir, modelDir, outputDir)) {
                PlatformInstantiatorExecutor executor = createExecutor();
                try {
                    if (asProcess) {
                        executor.executeAsProcess(getClass().getClassLoader(), resourcesDir, getTracingLevel(), 
                            composeMvnArgs(), args);
                    } else {
                        executor.execute(ClassLoader.getPlatformClassLoader(), resourcesDir, getTracingLevel(), 
                            composeMvnArgs(), args);
                    }
                } catch (ExecutionException e) {
                    throw new MojoExecutionException(e.getMessage());
                }
            } else {
                getLog().info("Skipped as code in output directory '" + outputDir + "' is newer than IVML model.");
            }
        } else {
            getLog().info("Model directory is not valid. No IVML files found in " + getModelDirectory());
        }
    }
    
    /**
     * Creates an executor instance.
     * 
     * @return the instance
     */
    private PlatformInstantiatorExecutor createExecutor() {
        File localRepo = null;
        LocalRepository lr = getRepoSession().getLocalRepository();
        if (lr != null) { // seems to fail at least in tests, usual fallback
            localRepo = lr.getBasedir();
        }
        return new PlatformInstantiatorExecutor(localRepo, w -> getLog().warn(w), 
            i -> getLog().info(i), t -> recordExecutionTime(t));
    }
    
    /**
     * Composes Maven execution arguments for EASy-Producer/Platform instantiator.
     * 
     * @return the Maven arguments
     */
    private String composeMvnArgs() {
        String mvnArgs = "";
        if (session.isOffline()) {
            mvnArgs += "-o";
        }
        return mvnArgs;
    }

    /**
     * Called to record the execution time.
     * 
     * @param time the passed time in ms
     */
    protected void recordExecutionTime(long time) {
    }
    
    @Override
    public MavenSession getSession() {
        return session;
    }

    @Override
    public ProjectBuilder getProjectBuilder() {
        return projectBuilder;
    }

    @Override
    public RepositorySystem getRepoSystem() {
        return repoSystem;
    }

}
