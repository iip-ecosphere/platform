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
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Extends the exec java mojo.
 * 
 * @author Gemini
 * @author Holger Eichelberger, SSE
 */
@Mojo( name = "update-archive", threadSafe = true)
public class UpdateArchiveMojo extends AbstractMojo {

    /**
     * A single file update on {@code target} in the jar.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class FileUpdate {
        private String target;
        private File source;
        private boolean updateContents = false;
    }
    
    /**
     * An update execution.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class Update {
        
        private String id = "";
        private File file;
        private List<FileUpdate> fileUpdates;
        private String target;
        private boolean inplace = true;
        private boolean quiet = true;
        private boolean verbose = true;
        
        /**
         * Copies all contents from {@code SourcesFile} to {@code pathInJar}, file by file.
         *  
         * @param sourceFile the source file
         * @param pathInArchive the target path-in-archive
         * @throws IOException if accessing conteints fails
         */
        private void copyAll(Path sourceFile, Path pathInArchive) throws IOException {
            FileSystem sfs = FileSystems.newFileSystem(sourceFile, (ClassLoader) null);
            FileSystem tfs = FileSystems.newFileSystem(pathInArchive, (ClassLoader) null);
            for (Path r: sfs.getRootDirectories()) {
                Files.walkFileTree(r, new SimpleFileVisitor<Path>() {
                    
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        // Map the source directory path to the target filesystem path
                        Path targetDir = translatePath(dir, tfs);
                        
                        // Create the directory in the target filesystem if it doesn't exist
                        if (targetDir != null && Files.notExists(targetDir)) {
                            Files.createDirectories(targetDir);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        // Map the source file path to the target filesystem path
                        Path targetFile = translatePath(file, tfs);
                        
                        if (targetFile != null) {
                            // Copy the file, replacing it if it already exists
                            Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        // Log or handle errors for specific unreadable files/dirs if necessary
                        if (exc != null) {
                            throw exc;
                        }
                        return FileVisitResult.CONTINUE;
                    }                    
                });
            }

            sfs.close();
            tfs.close();
        }

        /**
         * Updates an archive based on the given updates.
         * 
         * @return the updated archive
         * @throws MojoExecutionException if updating fails
         */
        private File update() throws MojoExecutionException {
            File jarFile = file;
            if (jarFile == null || !jarFile.exists()) {
                if (!quiet) {
                    throw new MojoExecutionException("JAR does not exist: " + jarFile);
                }
            }
            if (!inplace) {
                try {
                    Path tmp;
                    if (target != null) {
                        tmp = new File(target).toPath();
                    } else {
                        String name = file.getName();
                        String ext = "";
                        int pos = name.lastIndexOf('.');
                        if (pos > 0) {
                            name = name.substring(0, pos);
                            ext = name.substring(pos + 1);
                        }
                        tmp = Files.createTempFile(file.getName(), ext);
                        tmp.toFile().deleteOnExit();
                    }
                    Files.copy(file.toPath(), tmp, StandardCopyOption.REPLACE_EXISTING);
                    jarFile = tmp.toFile();
                } catch (IOException e) {
                    throw new MojoExecutionException("Updating inplace failed: " + e.getMessage());
                }
            }
            // Prepare the Zip File System environment
            URI jarUri = URI.create("jar:" + jarFile.toURI().toString());
            Map<String, String> env = new HashMap<>();
            env.put("create", "false"); 

            getLog().info("Updating " + file);
            try (FileSystem jarFileSystem = FileSystems.newFileSystem(jarUri, env)) {
                for (FileUpdate update : fileUpdates) {
                    Path sourceFile = update.source.toPath();
                    String targetPathInArchive = update.target;
                    if (!Files.exists(sourceFile)) {
                        if (quiet) {
                            continue;
                        } else {
                            throw new MojoExecutionException("Source file for replacement not found: " 
                                + sourceFile.toAbsolutePath());
                        }
                    }

                    // Ensure the target path inside the JAR starts with a leading slash
                    if (!targetPathInArchive.startsWith("/")) {
                        targetPathInArchive = "/" + targetPathInArchive;
                    }
                    Path pathInJar = jarFileSystem.getPath(targetPathInArchive);
                    if (update.updateContents) {
                        if (verbose) {
                            getLog().info(String.format(" - Replacing contents: %s -> %s", sourceFile.getFileName(), 
                                targetPathInArchive));
                        }
                        copyAll(sourceFile, pathInJar);
                    } else {
                        // Create parent directories inside the JAR if they don't exist
                        if (pathInJar.getParent() != null) {
                            Files.createDirectories(pathInJar.getParent());
                        }

                        if (verbose) {
                            getLog().info(String.format(" - Replacing/Adding: %s -> %s", sourceFile.getFileName(), 
                                targetPathInArchive));
                        }
                        // Copy file in-place, overwriting if it already exists
                        Files.copy(sourceFile, pathInJar, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to update JAR archive: " + file, e);
            }
            return jarFile;
        }
        
    }
    
    @Parameter( property = "updateArchive.skip", required = false, defaultValue = "false" )
    private boolean skip;
    
    @Parameter
    private List<Update> updates;

    /**
     * Translates a Path from one FileSystem to its equivalent string path in another FileSystem.
     */
    private static Path translatePath(Path sourcePath, FileSystem targetFs) {
        // Convert the source path to a string representation and resolve it against the target's root
        String sourcePathStr = sourcePath.toString();
        return targetFs.getPath(sourcePathStr);
    }
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping execution.");
        } else {
            for (Update e: updates) {
                File jar = e.update();
                if (e.id != null && e.id.length() > 0) {
                    System.setProperty(e.id, jar.getAbsolutePath());
                }
            }
        }
    }

}
