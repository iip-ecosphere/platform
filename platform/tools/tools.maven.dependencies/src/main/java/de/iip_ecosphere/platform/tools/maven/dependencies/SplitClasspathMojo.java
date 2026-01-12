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
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * A mojo that splits classpaths into main and app classpaths based on contained file name parts. Handlers spring
 * app package as well as oktoflow classpath jars.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "split-classpath", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true )
public class SplitClasspathMojo extends AbstractMojo {

    @Parameter( property = "mdep.archiveFile", defaultValue = "", required = true )
    private List<File> archiveFiles;

    @Parameter( property = "mdep.skip", defaultValue = "false", required = false )
    private boolean skip;

    @Parameter( required = false )
    private List<String> mainPatterns;

    @Parameter( required = false )
    private List<String> keepClasses;

    /**
     * Returns whether the given name/line contains on of the main file patterns.
     * 
     * @param name the name to check
     * @return {@code true} for match, {@code false} else
     */
    private boolean isMainFile(String name, boolean includeSpringLoader) {
        boolean found = false;
        for (String m : mainPatterns) {
            if (name.contains(m)) {
                found = true;
                break;
            }
        }
        if (includeSpringLoader && !found) {
            found = name.contains("spring-boot-loader-");
        }
        return found;
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            if (mainPatterns == null) {
                mainPatterns = new ArrayList<>();
            }
            if (mainPatterns.isEmpty()) {
                Collections.addAll(mainPatterns, "maven-python", "transport-", "support-", "support.boot-", 
                    "support.aas-", "support.iip-aas-", "connectors-", "services.environment-", 
                    "services.spring.loader-");
                // preliminary, to become plugins // TODO clean up!!!
                Collections.addAll(mainPatterns, "commons-io", "commons-lang3", "jackson-", "joda-", "jsoniter");
            }
            for (File f: archiveFiles) {
                if (null != f) { // erroneous XML
                    processFile(f);
                }
            }
            cleanup();
        } else {
            getLog().info("Skipping.");
        }
    }

    /**
     * Returns whether (parts of) {@code file} are compressed/deflated. Keep stored/deflated, in particular for 
     * Spring boot app jars, which are either exploded or stored.
     * 
     * @param file the file to check
     * @return {@code true} for compression, {@code false} else
     */
    private boolean isCompressed(File file) {
        boolean compressed = false;
        try {
            JarFile jf = new JarFile(file);
            Enumeration<JarEntry> enm = jf.entries();
            while (enm.hasMoreElements()) {
                JarEntry ent = enm.nextElement();
                if (ent.getName().endsWith(".jar")) { // classes are compressed
                    compressed = ent.getMethod() == ZipEntry.DEFLATED;
                    break;
                }
            }
            jf.close();
        } catch (IOException e) {
            getLog().warn("While checking Jar file: " + e.getMessage() + " Assuming no compression.");
        }
        return compressed;
    }

    /**
     * Processes a given file.
     * 
     * @param file the file to be processed
     * @throws MojoExecutionException if processing fails
     */
    private void processFile(File file) throws MojoExecutionException {
        boolean compressed = isCompressed(file);
        getLog().info("Using compression: " + compressed);
        try (FileSystem fs = FileSystems.newFileSystem(file.toPath(), 
            Map.of("compressionMethod", compressed ? "DEFLATED" : "STORED"))) {
            Path mainPath;
            Path appPath = null;
            StringBuilder main = new StringBuilder();
            StringBuilder app = new StringBuilder();

            mainPath = fs.getPath("/classpath");
            if (Files.exists(mainPath)) {
                getLog().info("Processing " + file + " as classpath JAR archive");
                appPath = fs.getPath("/classpath-app");
                List<String> lines = IOUtils.readLines(Files.newInputStream(mainPath), Charset.defaultCharset());
                for (String line: lines) {
                    if (line.startsWith("#")) {
                        main.append(line);
                        main.append("\n");
                        app.append(line);
                        app.append("\n");
                    } else {
                        String sep = ":";
                        StringTokenizer tokenizer = new StringTokenizer(line, sep);
                        boolean firstMain = true;
                        boolean firstApp = true;
                        while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken();
                            StringBuilder target;
                            boolean sepBefore;
                            if (isMainFile(token, true)) {
                                target = main;
                                sepBefore = !firstMain;
                                firstMain = false;
                            } else {
                                target = app;
                                sepBefore = !firstApp;
                                firstApp = false;
                            }
                            if (sepBefore) {
                                target.append(sep);
                            }
                            target.append(token);
                        }
                        main.append("\n");
                        app.append("\n");
                    }
                }
            } else {
                mainPath = fs.getPath("/BOOT-INF/classpath.idx");
                if (Files.exists(mainPath)) {
                    getLog().info("Processing " + file + " as spring app JAR archive");
                    appPath = fs.getPath("/BOOT-INF/classpath-app.idx");
                    Path libAppPath = fs.getPath("/BOOT-INF/lib-app");
                    if (!Files.isDirectory(libAppPath)) {
                        Files.createDirectory(libAppPath);
                    }
                    List<String> lines = IOUtils.readLines(Files.newInputStream(mainPath), Charset.defaultCharset());
                    for (String line: lines) {
                        StringBuilder target;
                        if (isMainFile(line, false)) { // spring boot loader implicitly there
                            target = main;
                        } else {
                            target = app;
                            line = processSpringLine(fs, line);
                        }
                        target.append(line);
                        target.append("\n");
                    }
                    postProcessSpringJar(fs);
                }
            }
            if (null != appPath) {
                Files.writeString(mainPath, main.toString());
                Files.writeString(appPath, app.toString());
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }
    
    /**
     * Cleans up left-over files.
     */
    private void cleanup() {
        // left-overs of zip filesystem provider at least on Windows
        File[] main = new File("").listFiles(f -> f.getName().startsWith("zipfstmp") && f.getName().endsWith(".tmp"));
        if (null != main) {
            for (File f: main) {
                FileUtils.deleteQuietly(f);
            }
        }
    }
    
    /**
     * Processes a spring index line. May move files in the archive.
     * 
     * @param fs the filesystem to operate on
     * @param line the line to process
     * @return the potentially modified line
     * @throws IOException if moving files fails
     */
    private String processSpringLine(FileSystem fs, String line) throws IOException {
        if (line.startsWith("- \"") && line.endsWith("\"")) {
            String src = line.substring(3, line.length() - 1);
            String tgt = src.replace("BOOT-INF/lib/", "BOOT-INF/lib-app/");
            Path srcPath = fs.getPath(src);
            Path tgtPath = fs.getPath(tgt);
            Files.move(srcPath, tgtPath, StandardCopyOption.REPLACE_EXISTING);
            line = "- \"" + tgt + "\"";
        }
        return line;
    }

    /**
     * Post-processes a spring jar, by moving those files that are not mentioned in an index file.
     * 
     * @param fs the filesystem to operate on
     * @throws IOException if moving files fails
     */
    private void postProcessSpringJar(FileSystem fs) throws IOException {
        Path srcPath = fs.getPath("BOOT-INF/classes/");
        Path tgtPath = fs.getPath("BOOT-INF/classes-app/");
        Predicate<Path> filter = null;
        if (null != keepClasses && keepClasses.size() > 0) {
            filter = p -> {
                String path = p.toString();
                return !keepClasses.stream().anyMatch(k -> path.contains(k));
            };
        }
        moveAll(srcPath, tgtPath, true, filter);
    }
    
    /**
     * Moves all files and folders from a source directory to a destination directory.
     *
     * @param sourcePath The path to the source directory
     * @param destPath   The path to the destination directory
     * @param keepSourcePath keep {@code sourcePath} itself if {@code true}, else remove also {@code sourcePath}
     * @param filter determines the files to move, may be <b>null</b> for all
     * @throws IOException If an I/O error occurs during the move operation
     */
    public void moveAll(Path sourcePath, Path destPath, boolean keepSourcePath, Predicate<Path> filter) 
        throws IOException {
        final boolean initialNoFilter = filter == null;
        if (initialNoFilter) {
            filter = p -> true;
        }
        if (!Files.exists(destPath)) {
            Files.createDirectories(destPath);
        }
        Files.walk(sourcePath)
            .filter(filter)
            .forEach(source -> {
                try {
                    Path destination = destPath.resolve(sourcePath.relativize(source));
                    Path destParent = destination;
                    if (Files.isRegularFile(source)) {
                        destParent = destination.getParent();
                    }
                    if (Files.notExists(destParent)) {
                        Files.createDirectories(destParent);
                    }
                    if (Files.isRegularFile(source)) {
                        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    getLog().error("In Jar: Failed to move " + source + ": " 
                        + e.getClass().getSimpleName() + " " + e.getMessage());
                }
            });
            
        // 4. Optionally, delete the now empty source directory after the move
        try {
            Files.walk(sourcePath)
                .filter(filter)
                .sorted(Comparator.reverseOrder()) // files (leftover?) before their parent directories
                .forEach(path -> {
                    try {
                        if (!keepSourcePath || (keepSourcePath && !path.equals(sourcePath))) {
                            Files.delete(path);
                        }
                    } catch (IOException e) {
                        if (initialNoFilter) { // otherways there might be intentional leftovers
                            getLog().error("In Jar: Failed to delete " + ": " 
                                + e.getClass().getSimpleName() + " " + e.getMessage());
                        }
                    }
                });
        } catch (IOException e) {
            getLog().error("In Jar: Failed to delete source directory: " + e.getMessage());
        }
    }
    
}
