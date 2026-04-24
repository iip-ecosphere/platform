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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.google.common.io.Files;

import de.oktoflow.platform.tools.lib.loader.LoaderIndex;

/**
 * Reused copy-dependencies Mojo to enable plugin dependencies copy. build-plugin-classpath must be executed before.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "copy-plugin-dependencies", requiresDependencyResolution = ResolutionScope.TEST, 
    defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true )
public class CopyPluginDependenciesMojo extends CopyDependenciesMojo {

    @Parameter( property = "outputDirectory", defaultValue = "" )
    protected File outputDirectory;
    
    @Parameter( property = "mdep.addTestArtifact", defaultValue = "false" )
    private boolean addTestArtifact;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File targetDirectory;

    @Parameter( required = false )
    private boolean asTest;

    @Parameter( property = "mdep.clean", required = false, defaultValue = "false" )
    private boolean clean;

    @Parameter( property = "mdep.createIndex", defaultValue = "true")
    private boolean createIndex;

    @Override
    protected void doExecute() throws MojoExecutionException {
        excludeArtifactIds = Layers.getExcludeArtifactIds(getProject().getArtifactId(), excludeArtifactIds, 
            asTest, getLog());
        setPrependGroupId(true);
        if (outputDirectory == null || outputDirectory.getName().equals("")) {
            setOutputDirectory(new File(targetDirectory, "jars" + (asTest ? "-test" : "")));
        }
        overWriteReleases = false;
        overWriteSnapshots = true;
        overWriteIfNewer = true;
        if (null == includeScope || includeScope.length() == 0) { // if not defined, default it
            if (addTestArtifact || asTest) {
                includeScope = "test";
            } else {
                includeScope = "runtime";
            }
        }
        cleanOutputDirectory();
        super.doExecute();
        List<File> additional = new ArrayList<>();
        additional.add(Layers.copyGroupArtifact(targetDirectory, null, "jar", getProject(), getLog()));
        if (asTest) {
            additional.add(Layers.copyGroupArtifact(targetDirectory, "tests", "jar", getProject(), getLog()));
        }
        File cpFile = new File(targetDirectory, "jars" + (asTest ? "-test" : "") + "/classpath");
        File index = new File(targetDirectory, "jars" + (asTest ? "-test" : "") + "/classpath.idx");
        if (createIndex) {
            List<Path> jars = getCpEntries(cpFile);
            if (jars.size() > 0) {
                try {
                    long start = System.currentTimeMillis();
                    getLog().info("Indexing classes...");
                    LoaderIndex idx = new LoaderIndex();
                    idx.setFileLocationProvider(f -> {
                        String path = f.getPath();
                        if (asTest) {
                            path = path.replace(File.separator + "jars-test" + File.separator,
                                File.separator + "jars" + File.separator);
                        }
                        return path;
                    });
                    LoaderIndex.addToIndex(idx, jars, 
                        ex -> getLog().warn(ex.getClass().getSimpleName() + " " + ex.getMessage()));
                    LoaderIndex.toFile(idx, index);
                    getLog().info("Stored class index to " + index + " " + idx.getClassesCount() + " classes and " 
                        + idx.getResourcesCount() + " resources in " + idx.getLocationsCount() + " locations in " 
                        + (System.currentTimeMillis() - start) + " ms");
                } catch (IOException e) {
                    getLog().warn("Cannot write index " + index + ". Ignoring. " + e.getClass().getSimpleName() + " " 
                        + e.getMessage());
                }
            }
        } else {
            index.delete();
        }
        emitOutputDirectorySize(additional);
    }
    
    /**
     * Cleans the output directory.
     */
    private void cleanOutputDirectory() {
        if (clean) {
            cleanDir(getOutputDirectory());
            if (asTest) {
                cleanDir(new File(getOutputDirectory(), "../jars-test"));
            }
        }
    }
    
    /**
     * Cleans the given output directory.
     * 
     * @param dir the directory to clean
     */
    private void cleanDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (null != files) {
                getLog().info("Cleaning jars in " + getOutputDirectory());
                for (File f : files) {
                    if (f.getName().endsWith(".jar")) {
                        f.delete();
                    }
                }
            }
        }
    }
    
    /**
     * Emits size statistics for the output directory.
     */
    private void emitOutputDirectorySize(List<File> additional) {
        List<File> files = new ArrayList<>();
        files.addAll(additional);
        File[] fs = getOutputDirectory().listFiles();
        if (null != fs) {
            for (File f : fs) { // usually not recursive
                files.add(f);
            }
        }
        long allSizes = 0;
        long oktoSizes = 0;
        long indexSizes = 0;
        int allFileCount = 0;
        int oktoFileCount = 0;
        int indexFileCount = 0;
        for (File f : files) { 
            allSizes += f.length();
            String name = f.getName();
            allFileCount++;
            if (name.startsWith("de.iip-ecosphere.") || name.startsWith("de.oktoflow.")) {
                oktoSizes += f.length();
                oktoFileCount++;
            } else if (!name.endsWith(".jar")) {
                indexSizes += f.length();
                indexFileCount++;
            }
        }
        getLog().info("Plugin size " + allSizes + " bytes in " + allFileCount + " files, among from oktoflow " 
            + oktoSizes + " bytes in " + oktoFileCount + " JARs and " + indexSizes + " bytes in " + indexFileCount 
            + " index file(s)");
    }

    /**
     * Returns the classpath entries from the classpath file (must be built before).
     * @param cpFile
     * @return
     */
    private List<Path> getCpEntries(File cpFile) {
        List<Path> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(cpFile)) {
            List<String> contents = IOUtils.readLines(fis, Charset.defaultCharset());
            for (String line : contents) {
                if (!line.startsWith("#")) {
                    StringTokenizer tokenizer = new StringTokenizer(line, ";:");
                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();
                        if (asTest) {
                            String origToken = token;
                            token = token.replace("/jars/", "/jars-test/");
                            // for now, resulting classpath file points to jars; move the jars there
                            if (!origToken.equals(token)) {                            
                                File srcFile = new File(outputDirectory, token);
                                File tgtFile = new File(outputDirectory, origToken);
                                if (!tgtFile.exists() || tgtFile.lastModified() < srcFile.lastModified()) {
                                    try {
                                        Files.copy(srcFile, tgtFile);
                                    } catch (IOException e) {
                                        getLog().warn("Cannot copy dependency " + token + ": " + e.getMessage());
                                    }
                                }
                            }
                        }
                        result.add(new File(token).toPath());
                    }
                }
            }
        } catch (IOException e) {
            getLog().error("Cannot locate '" + cpFile + "' - not creating index: " + e.getMessage());
        }
        return result;
    }

}
