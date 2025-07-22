/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.ZipUtils;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Resolves artifacts to classpaths or class loaders.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArtifactResolver {

    public static final String CLASSPATH_FILE = "classpath";
    private List<URL> jars = null;
    private File classpathArtifact;
    private SpringCloudArtifactDescriptor artifact;

    /**
     * Creates an artifact resolver for {@code artifact}.
     * 
     * @param artifact the artifact to determine the class loader for
     * @param homeDir process home dir to use for unpacking, may be <b>null</b> for none/unknown
     */
    public ArtifactResolver(SpringCloudArtifactDescriptor artifact, File homeDir) {
        this.artifact = artifact;
        String artId = artifact.getId();
        File jar = artifact.getJar();
        String jarName = jar.getName();
        if (jarName.endsWith(".jar")) { 
            classpathArtifact = jar;
        } else if (jarName.endsWith(".zip")) {
            try {
                classpathArtifact = homeDir;
                if (null == classpathArtifact) {
                    classpathArtifact = FileUtils.createTmpFolder(FileUtils.sanitizeFileName(artId), true);
                }
                classpathArtifact.deleteOnExit();
                ZipUtils.extractZip(new FileInputStream(jar), classpathArtifact.toPath());
                // may look for classpath file and take that into account!
                List<File> jarFiles = new ArrayList<>();
                FileUtils.listFiles(classpathArtifact, f -> f.isDirectory() || f.getName().endsWith(".jar"), 
                    f -> jarFiles.add(f));
                File tmp = new File(classpathArtifact, CLASSPATH_FILE);
                if (tmp.exists()) {
                    getLogger().info("Considering classpath file {}", tmp);
                    classpathArtifact = tmp;
                    String classpath = org.apache.commons.io.FileUtils.readFileToString(classpathArtifact, "UTF-8");
                    StringBuilder cpTmp = new StringBuilder();
                    findAppJars(f -> cpTmp.append(f.getName() + ":"));
                    
                    // sorting classpath file into file sequence
                    List<File> jarFilesSorted = new ArrayList<>();
                    Map<String, File> jarMap = new HashMap<>();
                    for (File j : jarFiles) {
                        jarMap.put(normalizeName(homeDir, j), j);
                    }
                    // add matching in sequence
                    StringTokenizer tokens = new StringTokenizer(classpath, ":");
                    while (tokens.hasMoreTokens()) {
                        String token = tokens.nextToken();
                        File f = jarMap.remove(token);
                        if (null != f) {
                            jarFilesSorted.add(f);
                        }
                    }
                    // add remaining
                    for (File j : jarFiles) {
                        File f = jarMap.remove(normalizeName(homeDir, j));
                        if (null != f) {
                            jarFilesSorted.add(f);
                        }
                    }
                    jarFiles.clear();
                    // the "app" is usually not in jars
                    jarFiles.addAll(jarFilesSorted);
                }
                jars = new ArrayList<URL>();
                for (File f : jarFiles) {
                    addUrlSafe(jars, f);
                }
                getLogger().info("Jars in classpath for {}: {}", artId, jars);
            } catch (IOException e) {
                getLogger().warn("Cannot unpack ZIP {}. Classloading may fail", jar, e.getMessage());
            }
        }
    }

    /**
     * Finds app JARs in {@link #classpathArtifact} folder and passes them on to {@code jarConsumer}.
     * 
     * @param jarConsumer the jar file consumer
     */
    private void findAppJars(Consumer<File> jarConsumer) {
        File[] files = classpathArtifact.getParentFile().listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.getName().endsWith(".jar")) {
                    jarConsumer.accept(f);
                }
            }
        }
    }
    
    /**
     * Normalizes the given file name towards classpath file notation.
     * 
     * @param home the home directory (may be <b>null</b> for none)
     * @param file the file
     * @return the normalized file name
     */
    private static String normalizeName(File home, File file) {
        String result = file.toString();
        if (home != null) {
            try {
                result = home.toPath().relativize(file.getAbsoluteFile().toPath()).toString();
            } catch (IllegalArgumentException e) {
                // take absolute
            }
        }
        return result.replace("\\", "/");
    }

    // checkstyle: stop exception type check
    
    /**
     * Makes {@code file} relative to {@code home} if possible.
     * 
     * @param home the home directory (may be <b>null</b> for none)
     * @param file the file to make relative
     * @return the relative file name
     */
    private static String relativize(File home, File file) {
        String result;
        if (home != null) {
            try {
                result = home.toPath().relativize(file.getAbsoluteFile().toPath()).toString();
            } catch (IllegalArgumentException e) {
                result = file.getAbsolutePath();
            }
        } else {
            result = file.toString();
        }
        return result;
    }

    /**
     * Adds the resolved classpath arguments to {@code args} taking {@code homeDir} as base directory.
     * 
     * @param args the JVM arguments to be modified as a side effect
     * @param homeDir process home dir to use for unpacking, may be <b>null</b> for none/unknown
     */
    public void addClasspathArguments(List<String> args, File homeDir) {
        if (isSpringJar()) {
            args.add("-jar");
            args.add(relativize(homeDir, classpathArtifact));
        } else if (null != jars) {
            String tmp = "";
            for (URL f : jars) {
                if (tmp.length() > 0) {
                    tmp += File.pathSeparator;
                }
                try {
                    tmp += relativize(homeDir, new File(f.toURI()));
                } catch (URISyntaxException e) {
                    tmp += f.toString(); // shall not occur
                }
            }
            args.add("-cp");
            args.add(tmp);
            args.add("iip.Starter");
        }

    }

    /**
     * Adds the URL of {@code file} to {@code urls}. Emits a warning if URL problems occur.
     * 
     * @param urls the URL list to be modified as a side effect
     * @param file the file to take the URL from
     */
    static void addUrlSafe(List<URL> urls, File file) {
        if (file.isFile()) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                getLogger().warn("Cannot turn file {} into URL. Classpath may be incomplete: {}", file, e.getMessage());
            }
        }
    }
    
    /**
     * Determines the class loader of the {@code artifact}.
     * 
     * @return the class loader
     */
    public ClassLoader determineArtifactClassLoader() {
        ClassLoader loader = SpringCloudServiceManager.class.getClassLoader();
        String artId = artifact.getId();
        File jar = artifact.getJar();
        if (isSpringJar()) { 
            getLogger().info("Creating Spring classloader for {}/{}", artId, jar);
            try {
                loader = DescriptorUtils.createClassLoader(jar);
            } catch (Exception e) {
                getLogger().warn("Cannot create Spring classloader for {}: {}", jar, e.getMessage());
                // use loader as fallback
            }
        } else {
            getLogger().info("Creating URL classloader for {}", jars);
            loader = new URLClassLoader(jars.toArray(new URL[jars.size()]));
        }
        return loader;
    }
    
    /**
     * Returns whether the resolved classpath artifact is a FAT Spring JAR.
     * 
     * @return {@code true} for Spring JAR
     */
    public boolean isSpringJar() {
        return classpathArtifact.isFile() && classpathArtifact.getName().endsWith(".jar");
    }

    /**
     * Returns whether the resolved classpath artifact is a classpath file.
     * 
     * @return {@code true} for Spring JAR
     */
    public boolean isClasspathFile() {
        return classpathArtifact.isFile() && classpathArtifact.getName().equals(CLASSPATH_FILE);
    }

    /**
     * Returns whether the resolved classpath artifact is a classpath file.
     * 
     * @return {@code true} for Spring JAR
     */
    public boolean isClasspath() {
        return classpathArtifact.isDirectory();
    }

    // checkstyle: resume exception type check

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ArtifactResolver.class);
    }

    
}
