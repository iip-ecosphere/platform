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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Specialized mojo for building plugin classpath files.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "build-plugin-classpath", requiresDependencyResolution = ResolutionScope.TEST, 
    defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true )
public class BuildPluginClasspathMojo extends BuildClasspathMojo {

    public static final String KEY_PREFIX = "# prefix: ";
    public static final String KEY_UNPACK_MODE = "# unpackMode: ";
    public static final String KEY_SETUP_DESCRIPTOR = "# setupDescriptor: ";
    public static final String KEY_PLUGIN_IDS = "# pluginIds: ";
    
    @Parameter( property = "mdep.addTestArtifact", defaultValue = "false" )
    private boolean addTestArtifact;

    @Parameter( property = "mdep.unpackMode", defaultValue = "jars" )
    private String unpackMode;

    @Parameter( property = "mdep.setupDescriptor", defaultValue = "FolderClasspath" )
    private String setupDescriptor;

    @Parameter( property = "mdep.pluginIds", defaultValue = "" )
    private List<String> pluginIds;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File targetDirectory;

    /**
     * Returns the relative target directory.
     * 
     * @return the relative target directory
     */
    private String getRelTargetDirectory() {
        String result = targetDirectory.getName(); // often but not always
        File home = new File(System.getProperty("user.dir"));
        String homePath = home.getAbsolutePath();
        String targetPath = targetDirectory.getAbsolutePath();
        try {
            homePath = home.getCanonicalPath();
            targetPath = targetDirectory.getCanonicalPath();
        } catch (IOException e) {
        }
        if (targetPath.startsWith(homePath)) {
            result = targetPath.substring(homePath.length() + 1);
        }
        return result;
    }
    
    /**
     * Composes the path for one of the own artifacts.
     * 
     * @param classifier optional classifier, may be empty or <b>null</b>
     * @param extension optional file extension, defaults to "jar" if empty or <b>null</b>
     * @return the path to the composed artifact
     */
    private String composeMyArtifact(String classifier, String extension) {
        classifier = null == classifier ? "" : classifier;
        if (classifier.length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        extension = null == extension || extension.length() == 0 ? "jar" : extension;
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        return getRelTargetDirectory() + "/" + getProject().getArtifactId() + "-" + getProject().getVersion() 
            + classifier + extension;
    }
    
    @Override
    protected void doExecute() throws MojoExecutionException {
        final String prefix = "target/jars";
        excludeArtifactIds = Layers.getExcludeArtifactIds(getProject().getArtifactId(), excludeArtifactIds, getLog());
        setOutputFile(new File(targetDirectory, "jars/classpath"));
        setPrependGroupId(true);
        overWriteIfNewer = true;
        setLocalRepoProperty(prefix);
        setPrefix(prefix);
        setFileSeparator("/");
        setPathSeparator(":");
        if (null == includeScope || includeScope.length() == 0) { // if not defined, default it
            if (addTestArtifact) {
                includeScope = "test";
            } else {
                includeScope = "runtime";
            }
        }
        List<String> prepends = new ArrayList<>();
        prepends.add(composeMyArtifact("", "jar"));
        if (addTestArtifact) {
            prepends.add(composeMyArtifact("tests", "jar")); // default Maven classifier
        }
        setPrepends(prepends);
        List<String> befores = new ArrayList<>();
        befores.add(KEY_PREFIX + prefix);
        befores.add(KEY_UNPACK_MODE + unpackMode);
        befores.add(KEY_SETUP_DESCRIPTOR + setupDescriptor);
        if (pluginIds != null && pluginIds.size() > 0) {
            befores.add(KEY_PLUGIN_IDS + String.join(", ", pluginIds));
        }
        setBefores(befores);
        super.doExecute();
    }
        
}
