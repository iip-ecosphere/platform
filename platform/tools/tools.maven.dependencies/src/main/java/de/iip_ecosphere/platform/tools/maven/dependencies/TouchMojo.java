/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import static de.iip_ecosphere.platform.tools.maven.dependencies.Resolver.normalize;
import static de.oktoflow.platform.tools.lib.HashUtils.toMessage;
import static de.oktoflow.platform.tools.lib.HashUtils.getMD5;

/**
 * A simple "touch" command, filling the touch file with service ids from {@code services.properties} if respective 
 * files change. If {@link #onServiceUpdates} is disabled, does a default touch, i.e., creating the specified file 
 * empty, potentially updating the file creation/modification time.
 * 
 * @author Gemini
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "touch", threadSafe = true, defaultPhase = LifecyclePhase.DEPLOY )
public class TouchMojo extends AbstractMojo {

    @Parameter( property = "touch.file", required = true )
    private File file;

    @Parameter( property = "touch.onServiceUpdates", required = true, defaultValue = "true" )
    private boolean onServiceUpdates;

    @Parameter( property = "updateArchive.deleteBefore", required = false, defaultValue = "false" )
    private boolean deleteBefore;

    @Parameter( property = "updateArchive.skip", required = false, defaultValue = "false" )
    private boolean skip;
    
    @Parameter( property = "touch.passJava", required = false, defaultValue = "false" )
    private boolean passJava;

    @Parameter( required = false, defaultValue = "okto.service.artifacts" )
    private String javaArtifactsProperty;
    
    @Parameter( defaultValue = "${project.build.directory}", required = true, readonly = true )
    private File buildDir;

    @Parameter( defaultValue = "${project.basedir}", required = true, readonly = true )
    private File basedir;
    
    private String basePath;
    
    private Set<String> checkedArtifacts = new HashSet<>();
    
    /**
     * POM.
     */
    private final MavenProject project;

    /**
     * Creates a mojo instance.
     * 
     * @param project the project instance
     */
    @Inject
    protected TouchMojo(MavenProject project) {
        this.project = project;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execution.}");
        } else {
            checkedArtifacts.clear();
            if (null == javaArtifactsProperty) {
                javaArtifactsProperty = "";
            }
            basePath = normalize(basedir);
            if (deleteBefore) {
                FileUtils.deleteQuietly(file);
            }
            if (onServiceUpdates) {
                Properties changed = getChangedFiles();
                try (FileWriter out = new FileWriter(file)) {
                    changed.store(out, "changed services, j=java, a=artifact");
                } catch (IOException e) {
                    throw new MojoExecutionException("Touching " + file + ": " + toMessage(e));
                }
            } else {
                try {
                    FileUtils.touch(file);
                } catch (IOException e) {
                    throw new MojoExecutionException("Touching " + file + ": " + toMessage(e));
                }
            }
            checkedArtifacts.clear();
        }
    }
    

    /**
     * Returns a list of changed service ids (based on file {@code services.properties}). May update
     * the corresponding fingerprint files.
     * 
     * @return a mapping of service id to status ("j" java, "a" Artifact e.g., for Python), may be empty
     */
    private Properties getChangedFiles() {
        File hashesFile = new File(buildDir, "touch.fpf");
        Properties hashes = new Properties();
        if (hashesFile.exists()) {
            try (FileReader fr = new FileReader(hashesFile)) {
                hashes.load(fr);
            } catch (IOException e) {
                getLog().warn("Cannot read " + hashesFile + ": " + toMessage(e));
            }
        }
        Properties serviceCfg = new Properties();
        File serviceCfgFile = new File(basedir, "services.properties");
        if (serviceCfgFile.exists()) {
            try (FileReader fr = new FileReader(serviceCfgFile)) {
                serviceCfg.load(fr);
            } catch (IOException e) {
                getLog().warn("Cannot read " + serviceCfgFile + ": " + toMessage(e));
            }
        } else {
            getLog().warn("Cannot read " + serviceCfgFile + ": file does not exist");
        }
        Properties changed = new Properties();
        for (Object k : serviceCfg.keySet()) { // take over pseudo keys; currently no change detection for Java
            String key = k.toString();
            if (key.startsWith("-")) {
                boolean isChanged = false;
                if (passJava) {
                    isChanged = true;
                } else {
                    isChanged = checkForJavaChanges(hashes);
                }
                if (isChanged) {
                    changed.put(serviceCfg.getProperty(key), "j");
                }
            }
        }
        // empty file means do the update, however if there are no services changed and passJava is on, we need to
        // avoid service updates
        if (!passJava && changed.isEmpty()) {
            changed.put("*dummy*", "?");
        }
        File[] classesFiles = new File(buildDir, "classes").listFiles();
        if (null != classesFiles) {
            for (File f : classesFiles) {
                String fName = f.getParentFile().getName() + "/" + f.getName(); // file paths with /
                Object serviceId = serviceCfg.get(fName);
                if (null != serviceId && changed(f, hashes)) {
                    changed.put(serviceId.toString(), "a");
                }
            }
        }
        
        try (FileWriter writer = new FileWriter(hashesFile)) {
            hashes.store(writer, "file hashes");
        } catch (IOException e) {
            getLog().warn("Cannot write " + hashesFile + ": " + toMessage(e));
        }
        
        return changed;
    }

    /**
     * Checks for Java changes based on the Java artifacts specified in {@link #javaArtifactsProperty}.
     * 
     * @param hashes the file-MD5 hashes
     * @return {@code true} if changed, {@code false} else
     */
    private boolean checkForJavaChanges(Properties hashes) {
        boolean isChanged = false;
        String arts =  project.getProperties().getProperty(javaArtifactsProperty, "");
        StringTokenizer javaArts = new StringTokenizer(arts, ",");
        while (javaArts.hasMoreTokens()) {
            String artifact = javaArts.nextToken();
            if (!artifact.contains(":python:") && !checkedArtifacts.contains(artifact)) {
                String artifactName = Resolver.toFileName(artifact, true);      
                if (null != artifactName && artifactName.length() > 0 && artifactName.endsWith(".jar")) {
                    File art = findArtifact(new File(buildDir, artifactName));
                    isChanged = changed(art, hashes);
                }
            }
            checkedArtifacts.add(artifact);
        }
        return isChanged;
    }

    /**
     * Tries to find a file with the given name as specified or in one of the parent folders.
     * 
     * @param file the file to look for
     * @return {@code file} (may not exist) or a file with the same name in a parent folder
     */
    private static File findArtifact(File file) {
        File result = file;
        File iter = result.getParentFile();
        while (iter != null && !file.exists()) {
            file = new File(iter, file.getName());
            iter = iter.getParentFile();
        }
        if (file.exists()) {
            result = file;
        }
        return result;
    }

    /**
     * Returns whether {@code file} was changed, compared to an eventually existing fingerprint file. May update
     * the corresponding fingerprint file.
     * 
     * @param file the file
     * @param hashes the file-MD5 hashes
     * @return {@code true} if changed, {@code false} else
     */
    private boolean changed(File file, Properties hashes) {
        boolean result = false;
        if (file.exists()) {
            String filePath = normalize(file);
            if (filePath.startsWith(basePath)) {
                filePath = filePath.substring(basePath.length());
            }
            String oldHash = hashes.getProperty(filePath);
            String newHash = getMD5(file, true, s -> getLog());
            hashes.put(filePath, newHash);
            if (oldHash == null || newHash == null || !oldHash.equals(newHash)) {
                result = true;
            }
            getLog().info("Checking '" + file + "' for changes (new/changed: " + result + ")");
        }
        return result;
    }

}
