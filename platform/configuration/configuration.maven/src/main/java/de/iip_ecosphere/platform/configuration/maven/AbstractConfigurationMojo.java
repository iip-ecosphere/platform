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
import java.util.concurrent.ExecutionException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import de.iip_ecosphere.platform.configuration.PlatformInstantiator;

/**
 * Abstract configuration Mojo with settings for all configuration Mojos.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractConfigurationMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;
    
    @Parameter(property = "configuration.model", required = true)
    private String model;

    @Parameter(property = "configuration.modelDirectory", required = true, defaultValue = "src/test/easy")
    private String modelDirectory;

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

    // different name, hook up with unpack
    @Parameter(property = "unpack.force", required = false, defaultValue = "false") 
    private boolean unpackForce;

    /**
     * Returns the actual Maven project.
     * 
     * @return the project
     */
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
            String[] files = modelDir.list((d, n) -> n.endsWith(".ivml"));
            if (null != files && files.length > 0) {
                result = true;
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
     * @param modelDir the model directory
     * @param outDir the output directory
     * @return {@code true} if {@code modelDir} is considered to be newer than {@code outDir}, {@code false} else
     */
    protected boolean modelNewerThanOut(String modelDir, String outDir) {
        long maxModel = getMaxLastModified(modelDir, f -> f.getName().endsWith(".ivml"));
        long maxOut = getMaxLastModified(outDir, f -> true);
        return maxOut < 0 || maxModel > maxOut;
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
            File[] files = d.listFiles(filter);
            if (files != null) {
                for (File f : files) {
                    result = Math.max(result, f.lastModified());
                }
            }
        }
        return result;
    }
    
    /**
     * Called by {@link #execute()} to figure out whether the instantiation shall take place. By default,
     * instantiation will be enabled if IVML files in {@code modelDir} are newer than files in {@code 
     * output directory} or if output directory is empty or missing.
     * 
     * @param modelDir the model directory
     * @param outputDir the output directory
     * @return {@code true} for instantiation, {@code false} for no instantiation
     */
    protected boolean enableRun(String modelDir, String outputDir) {
        return modelNewerThanOut(modelDir, outputDir);
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.setProperty(PlatformInstantiator.KEY_PROPERTY_TRACING, getTracingLevel());
        String mvnArgs = "";
        if (session.isOffline()) {
            mvnArgs += "-o";
        }
        System.setProperty(PlatformInstantiator.KEY_PROPERTY_MVNARGS, mvnArgs);
        String resourcesDir = validateDirectory(makeAbsolute(getResourcesDirectory()));
        if (null == resourcesDir) {
            resourcesDir = validateDirectory(makeAbsolute(getFallbackResourcesDirectory()));
        }
        if (null != resourcesDir) {
            System.setProperty("iip.resources", resourcesDir);
        }
        String outputDir = adjustOutputDir(makeAbsolute(getOutputDirectory()));
        String modelDir = makeAbsolute(getModelDirectory());
        String[] args = {getModel(), modelDir, outputDir, getStartRule()};
        try {
            if (isModelDirectoryValid()) {
                if (enableRun(modelDir, outputDir)) {
                    getLog().info("Calling platform instantiator with " + java.util.Arrays.toString(args) + ", tracing "
                        + getTracingLevel() + (null == resourcesDir ? "" : " and resources dir " + resourcesDir));
                    PlatformInstantiator.mainImpl(args);
                } else {
                    getLog().info("Skipped as code in output directory is newer than IVML model.");
                }
            }
        } catch (ExecutionException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }
    
}
