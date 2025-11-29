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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

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
    public static final String KEY_SEQUENCE_NR = "# sequenceNr: ";

    private final String prefix = "target/jars";
   
    @Parameter( property = "mdep.addTestArtifact", defaultValue = "false" )
    private boolean addTestArtifact;

    @Parameter( property = "mdep.unpackMode", defaultValue = Layers.DEFAULT_UNPACK_MODE )
    private String unpackMode;

    @Parameter( property = "mdep.setupDescriptor", defaultValue = "FolderClasspath" )
    private String setupDescriptor;

    @Parameter( property = "mdep.pluginIds", defaultValue = "" )
    private List<String> pluginIds;

    @Parameter( property = "mdep.resolvedFile", defaultValue = "*")
    private String resolvedFile;

    @Parameter( required = false )
    private boolean asTest;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File targetDirectory;

    @Parameter( property = "mdep.validateJsl", defaultValue = "INFO" )
    private JslMode validateJsl;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteRepositories;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repoSession;

    @Component
    private RepositorySystem repoSystem;
    
    public enum JslMode {
        OFF,
        INFO,
        WARN,
        ERROR,
        FAIL
    }

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
        // standard Maven naming does not add groupId, our packager is obliged to do so
        return getRelTargetDirectory() + "/" + getProject().getGroupId() + "." + getProject().getArtifactId() 
            + "-" + getProject().getVersion() + classifier + extension;
    }

    @Override
    protected void doExecute() throws MojoExecutionException {
        excludeArtifactIds = Layers.getExcludeArtifactIds(getProject().getArtifactId(), excludeArtifactIds, 
            asTest, getLog());
        setOutputFile(new File(targetDirectory, "jars" + (asTest ? "-test" : "") + "/classpath"));
        setPrependGroupId(true);
        overWriteIfNewer = true;
        setLocalRepoProperty(prefix);
        setPrefix(prefix);
        setFileSeparator("/");
        setPathSeparator(":");
        if (null == includeScope || includeScope.length() == 0) { // if not defined, default it
            if (addTestArtifact || asTest) {
                includeScope = "test";
            } else {
                includeScope = "compile";
            }
        }
        List<String> prepends = new ArrayList<>();
        prepends.add(composeMyArtifact("", "jar"));
        if (addTestArtifact || asTest) {
            prepends.add(composeMyArtifact("tests", "jar")); // default Maven classifier
        }
        setPrepends(prepends);
        composeBefores(null);
        super.doExecute();

        if (validateJsl != JslMode.OFF) {
            switch (validateJsl) {
            case FAIL:
                if (!validateJsl(s -> getLog().error(s))) {
                    throw new MojoExecutionException("Cannot validate existence of JSL services. See messages above.");
                }
                break;
            case ERROR:
                validateJsl(s -> getLog().error(s));
                break;
            case WARN:
                validateJsl(s -> getLog().warn(s));
                break;
            case INFO:
                validateJsl(s -> getLog().info(s));
                break;
            default:
                break;
            }
        }
    }
    
    /**
     * Validates whether JSL descriptor files have their counterparts in classes. Must be executed after class and 
     * test class compilation.
     * 
     * @param logConsumer consumer for logging messages
     * @return {@code true} if valid, {@code false} if failed
     */
    private boolean validateJsl(Consumer<String> logConsumer) {
        File classes = new File(targetDirectory, "classes");
        File testClasses = new File(targetDirectory, "test-classes");
        boolean result = true;
        result &= validateJsl(new File("src/main/resources"), logConsumer, classes);
        result &= validateJsl(new File("src/test/resources"), logConsumer, classes, testClasses);
        return result;
    }
    
    /**
     * Validates whether JSL descriptor files in {@link descParent} have their counterparts in classes compiled to 
     * {@link classDir}.
     * 
     * @param logConsumer consumer for logging messages
     * @param descParent, usually a resource folder where {@code META-INF/services} is located within
     * @param classDir the parent folders to search classes within
     * @return {@code true} if valid, {@code false} if failed
     */
    private boolean validateJsl(File descParent, Consumer<String> logConsumer, File... classDir) {
        boolean result = true;
        List<File> classDirList = new ArrayList<>();
        Collections.addAll(classDirList, classDir);
        File jslDir = new File(descParent, "META-INF/services");
        if (jslDir.isDirectory()) {
            File[] descs = jslDir.listFiles(f -> f.isFile());
            if (null != descs) {
                for (File desc : descs) {
                    try {
                        List<String> lines = Files.readAllLines(desc.toPath());
                        for (String line: lines) {
                            result &= validateJsl(desc, logConsumer, line, classDirList);
                        }
                    } catch (IOException e) {
                        logConsumer.accept("Cannot read JSL descriptor " + desc + ": " + e.getMessage());
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Validates whether a JSL descriptor file in {@link descParent} has counterparts in classes compiled to 
     * {@link classDirList}.
     * 
     * @param desc the descriptor file
     * @param logConsumer consumer for logging messages
     * @param descClass the class name within {@code desc} to validate
     * @param classDirList the parent folders to search classes within
     * @return {@code true} if valid, {@code false} if failed
     */
    private boolean validateJsl(File desc, Consumer<String> logConsumer, String descClass, List<File> classDirList) {
        boolean result = true;
        for (File cDir : classDirList) {
            File clsFile = new File(cDir, descClass.replace(".", "/") + ".class");
            if (!clsFile.isFile()) {
                logConsumer.accept("Class '" + descClass + "' (" + clsFile + ") in JSL descriptor " + desc 
                    + " not found in " + classDirList);
                result = false;
            }
        }
        return result;
    }

    @Override
    public void adjustTo(Function<String, String> func) {
        composeBefores(func);
    }

    /**
     * Composes and sets the befores.
     * 
     * @param func a path adjustment function, may be <b>null</b>
     */
    private void composeBefores(Function<String, String> func) {
        List<String> befores = new ArrayList<>();
        befores.add(KEY_PREFIX + (null != func ? func.apply(prefix) : prefix));
        befores.add(KEY_UNPACK_MODE + unpackMode);
        befores.add(KEY_SETUP_DESCRIPTOR + setupDescriptor);
        if (pluginIds != null && pluginIds.size() > 0) {
            befores.add(KEY_PLUGIN_IDS + String.join(", ", pluginIds));
        }
        setBefores(befores);
    }
        
}
