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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.dependency.fromConfiguration.ArtifactItem;
import org.apache.maven.shared.model.fileset.FileSet;
import org.codehaus.plexus.components.io.filemappers.FileMapper;

/**
 * Extended unpack Mojo for plugins.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "unpack-plugins", defaultPhase = LifecyclePhase.TEST_COMPILE, requiresProject = false, threadSafe = true)
public class UnpackPluginMojo extends CleaningUnpackMojo {

    private static final String NAME_CLASSPATH_FILE = "classpath";
    
    @Parameter(property = "unpack.plugins", required = false)
    private List<ArtifactItem> plugins;
    
    @Parameter(property = "unpack.version", required = false, defaultValue = "")
    private String version;

    @Parameter(property = "unpack.relocate", required = false, defaultValue = "false")
    private boolean relocate;
    
    @Parameter(property = "unpack.relocateTarget", required = false, defaultValue = "jars")
    private File relocateTarget;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File targetDirectory;

    private List<String> classpathFiles = new ArrayList<>();
    
    /**
     * Used for relocating classpath files, i.e., to rename them with their plugin short (artifactId) name.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class RelocatingFileMapper implements FileMapper {

        private String targetName;
        
        /**
         * Creates a relocating file mapper.
         * 
         * @param targetName the target name to map to
         */
        private RelocatingFileMapper(String targetName) {
            this.targetName = targetName;
        }
        
        @Override
        public String getMappedFileName(String pName) {
            String name = pName;
            if (name.startsWith(NAME_CLASSPATH_FILE)) {
                if (name.length() == NAME_CLASSPATH_FILE.length()) {
                    name = targetName;
                } else {
                    name = targetName + name.substring(NAME_CLASSPATH_FILE.length());
                }
                classpathFiles.add(name);
            }
            return name;
        }
        
    }
    
    /**
     * Returns the part after the last ".".
     * 
     * @param name the name to strip
     * @return the last part or {@code name}
     */
    private String lastPart(String name) {
        int pos = name.lastIndexOf(".");
        return pos > 0 ? name.substring(pos + 1) : name;
    }
    
    @Override
    public void doExecute() throws MojoExecutionException, MojoFailureException {
        setForceCleanup(true);

        FileSet cleanup = new FileSet();
        cleanup.setDirectory((relocate ? relocateTarget : new File(targetDirectory, "oktoPlugins")).toString());
        setCleanup(cleanup);

        if (!relocate) {
            // figure out whether we are in development mode, i.e., in git workspace
            File skipIfExists = new File("../../support/support"); // building for arbitrary component
            if (!skipIfExists.exists()) {
                skipIfExists = new File("../support"); // building for a support component
            }
            if (skipIfExists.exists()) {
                setSkipIfExists(skipIfExists);
            }
        } else {
            relocateTarget.mkdirs();
            setForce(true);
        }

        if (plugins != null && plugins.size() > 0) {
            List<ArtifactItem> artifactItems = new ArrayList<>();
            for (ArtifactItem pl : plugins) {
                ArtifactItem item = new ArtifactItem();
                String name = lastPart(pl.getArtifactId());
                if (StringUtils.isBlank(pl.getGroupId())) {
                    item.setGroupId("de.iip-ecosphere.platform");
                }
                item.setArtifactId(pl.getArtifactId());
                item.setVersion(StringUtils.isBlank(version) ? pl.getVersion() : version);
                item.setType("zip");
                item.setClassifier("plugin");
                item.setOverWrite(String.valueOf(true));
                item.setOutputDirectory(relocate ? relocateTarget : new File(targetDirectory, "oktoPlugins/" + name));
                getLog().info("Configuring plugin '" + name + "' -> " + item.getOutputDirectory());
                item.setDestFileName(name + ".zip");
                if (relocate) {
                    item.setFileMappers(new FileMapper[] {new RelocatingFileMapper(name)});
                }
                artifactItems.add(item);
            }
            setArtifactItems(artifactItems);
        }
        
        super.doExecute();
        
        if (relocate) {
            relocate();
        }
    }
    
    /**
     * Merges files from {@code src} into {@code tgt} by only copying those that do not already exist.
     * 
     * @param src the source folder
     * @param tgt the target folder
     * @throws IOException if copying fails
     */
    private void mergeFiles(File src, File tgt) throws IOException {
        File[] srcFiles = src.listFiles();
        if (null != srcFiles) {
            for (File f : srcFiles) {
                File t = new File(tgt, f.getName());
                if (!t.exists() && f.isFile()) {
                    FileUtils.moveFile(f, t);
                }
            }
        }
    }

    /**
     * Relocates the unpacked files.
     * 
     * @throws MojoExecutionException if the execution fails
     * @throws MojoFailureException if a remarkable processing failure occurred
     */
    private void relocate() throws MojoExecutionException, MojoFailureException {
        if (relocate) {
            File tgtDir = relocateTarget;
            File pluginDir = new File(relocateTarget, "../plugins");
            getLog().info("Flattening jars/target/jars and jars/target to jars");        
            try { // flatten
                mergeFiles(new File(tgtDir, "target/jars"), tgtDir);
                mergeFiles(new File(tgtDir, "target"), tgtDir);
            } catch (IOException e) {
                throw new MojoFailureException("Cannot move unpacked files: " + e.getMessage());
            }
            // cleanup
            FileUtils.deleteQuietly(new File(tgtDir, "target"));
            // move from jars/target and jars/target/jars to jars
            for (String cpFile : classpathFiles) {
                File src = new File(relocateTarget, cpFile);
                File tgt = new File(pluginDir, cpFile);
                tgt.mkdirs();
                getLog().info("Rewriting classpath file " + src + " to " + tgt);
                try {
                    FileUtils.deleteQuietly(tgt);
                    FileUtils.moveFile(src, tgt);
                    FileInputStream fis = new FileInputStream(tgt);
                    String contents = IOUtils.toString(fis, Charset.defaultCharset());
                    fis.close();
                    String sep = ":";
                    boolean win = false;
                    StringTokenizer tokenizer = new StringTokenizer(contents, sep);
                    if (tokenizer.countTokens() < 1) {
                        sep = ";";
                        tokenizer = new StringTokenizer(contents, sep);
                        win = true;
                    }
                    PrintStream out = new PrintStream(new FileOutputStream(tgt));
                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();
                        if (win) {
                            token = token.replace("target\\jars\\", "jars\\");
                            token = token.replace("target\\", "jars\\");
                        } else {
                            token = token.replace("target/jars/", "jars/");
                            token = token.replace("target/", "jars/");
                        }
                        out.print(token);
                        if (tokenizer.hasMoreTokens()) {
                            out.print(sep);
                        }
                    }
                    out.close();
                    FileUtils.deleteQuietly(src);
                } catch (IOException e) {
                    throw new MojoFailureException("Cannot postprocess " + src + ": " + e.getMessage());
                }
            }
        }
    }
   

}
