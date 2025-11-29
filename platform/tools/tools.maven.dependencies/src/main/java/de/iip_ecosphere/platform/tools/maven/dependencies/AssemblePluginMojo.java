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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.model.fileset.FileSet;

import de.iip_ecosphere.platform.tools.maven.python.FilesetUtils;

/**
 * Assembles the plugin. Planned to reuse maven-assembly-single, but fails with some injection error.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "assemble-plugin", inheritByDefault = false, requiresDependencyResolution = ResolutionScope.TEST,
    defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true )
public class AssemblePluginMojo extends AbstractMojo {

    @Parameter( property = "mdep.addTestArtifact", defaultValue = "false" )
    private boolean addTestArtifact;
  
    @Parameter( property = "mdep.unpackMode", defaultValue = Layers.DEFAULT_UNPACK_MODE )
    private String unpackMode;
    
    @Parameter( required = false )
    private boolean asTest;

    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    @Parameter(required = false)
    private FileSet furtherFiles;
    
    @Component
    private MavenProjectHelper projectHelper;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File targetDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String namePrefix = project.getArtifactId() + "-" + project.getVersion();
        File outputFile = new File(targetDirectory, namePrefix + "-plugin" + (asTest ? "-test" : "") + ".zip");
        File jarsDir = new File(targetDirectory, "jars" + (asTest ? "-test" : ""));
        getLog().info("Building " + outputFile);
        FileUtils.deleteQuietly(outputFile);
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile))) {
            if (!addClasspathFiles(out, jarsDir)) { 
                // initial style
                addClasspathFiles(out, new File(targetDirectory, "classes"));
            }
            if (isJarUnpacking()) {
                addFile(out, prependGroup(new File(targetDirectory, namePrefix + ".jar")), "target/", false);
                if (addTestArtifact || asTest) {
                    addFile(out, prependGroup(new File(targetDirectory, namePrefix + "-tests.jar")), "target/", false);
                }
            }
            Set<File> excluded = new HashSet<>();
            FilesetUtils.determineFiles(furtherFiles, false, f -> excluded.add(f));
            if (isJarUnpacking()) {
                getLog().info("Adding dependencies jars from " + jarsDir);
                File[] jars = jarsDir.listFiles();
                if (null != jars) {
                    for (File f : jars) {
                        if (f.getName().endsWith(".jar") && !excluded.contains(f)) {
                            addFile(out, f, "target/jars/", false);
                        }
                    }
                }
            }
        } catch (IOException e) {
            getLog().error("While packaging '" + outputFile + "': " + e.getMessage());
        }
        projectHelper.attachArtifact(project, "zip", "plugin" + (asTest ? "-test" : ""), outputFile);
    }

    /**
     * Prepends the group id before an usual Maven artifact as we need it that way for resolution on unpacking.
     * 
     * @param file the file to prepend
     * @return the prepended file
     */
    private File prependGroup(File file) {
        File result = new File(file.getParent(), project.getGroupId() + "." + file.getName());
        try {
            Files.copy(file.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            getLog().error("While prepending groupId: " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Returns whether unpacking mode is "jars".
     * 
     * @return {@code true} for jars, else {@code false} in particular for "resolve"
     */
    private boolean isJarUnpacking() {
        return "jars".equals(unpackMode);
    }

    /**
     * Adds classpath files in {@code dir} to {@code out}.
     * 
     * @param out the output ZIP stream
     * @param dir the source directory
     * @return whether classpath files were found and added
     */
    private boolean addClasspathFiles(ZipOutputStream out, File dir) {
        boolean done = false;
        File cpFile = new File(dir, "classpath");
        if (cpFile.exists()) {
            addFile(out, cpFile, "", false);
            done = true;
            File[] jars = dir.listFiles();
            if (null != jars) {
                for (File f : jars) {
                    if (f.getName().startsWith("classpath-")) {
                        addFile(out, f, "", false);
                    }
                }                
            }
        }
        return done;
    }
    
    /**
     * Adds a file to {@code out}.
     * 
     * @param out the output ZIP stream
     * @param file the file to add
     * @param prefix path prefix for packaging file
     */
    private void addFile(ZipOutputStream out, File file, String prefix, boolean deleteAfter) {
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                String name = prefix + file.getName();
                ZipEntry entry = new ZipEntry(name);
                entry.setTime(file.lastModified());
                out.putNextEntry(entry);
                IOUtils.copy(fis, out);
                getLog().debug(" - Added " + file + " as " + name);
            } catch (IOException e) {
                getLog().error("Cannot open " + file + ": " + e.getMessage());
            }
        }
        if (deleteAfter) {
            FileUtils.deleteQuietly(file);
        }
    }
    
}
