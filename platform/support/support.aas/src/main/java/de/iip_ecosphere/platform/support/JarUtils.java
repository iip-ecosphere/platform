/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Basic Jar utilities.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JarUtils {
    
    /**
     * Finds a file within the ZIP/JAR file given by {@code in}.
     * 
     * @param in the input stream containing ZIP/JAR data
     * @param name the name of the file within {@code in} to be returned
     * @return the input stream to {@code name} (must be closed explicitly) or <b>null</b> for none
     * @throws IOException if something I/O related fails
     */
    public static InputStream findFile(InputStream in, String name) throws IOException {
        InputStream found = null;
        try {
            ZipInputStream zis = new ZipInputStream(in);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (zipEntry.getName().equals(name)) {
                    found = zis;
                    break;
                }
                zipEntry = zis.getNextEntry();
            }
            if (null == found) {
                zis.closeEntry();
                zis.close();
            }
        } catch (IOException e) {
            throw e;
        }
        return found;
    }

    /**
     * Extracts a ZIP/JAR file.
     * 
     * @param in the input stream containing the ZIP/JAR file
     * @param target the target path
     * @throws IOException if something I/O related fails
     */
    public static void extractZip(InputStream in, Path target) throws IOException {
        extractZip(in, target, null);
    }

    /**
     * Returns a predicate checking whether {@link ZipEntry} is in {@code folder}. Helper for 
     * {@link #extractZip(InputStream, Path, Predicate)}.
     *  
     * @param folder the folder to check for
     * @return the predicate
     */
    public static Predicate<ZipEntry> inFolder(String folder) {
        return z -> {
            return z.getName().equals(folder) || z.getName().startsWith(folder + "/");
        };
    }

    // adapted from https://mkyong.com/java/how-to-decompress-files-from-a-zip-file/
    
    /**
     * Extracts a ZIP/JAR file.
     * 
     * @param in the input stream containing the ZIP/JAR file
     * @param target the target path
     * @param pred a predicate selecting ZIP entries for creation/extraction (may be <b>null</b> for all contents) 
     * @throws IOException if something I/O related fails
     */
    public static void extractZip(InputStream in, Path target, Predicate<ZipEntry> pred) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                boolean isDirectory = zipEntry.isDirectory();
                if (null == pred || pred.test(zipEntry)) {
                    Path newPath = zipSlipProtect(zipEntry, target);
                    if (isDirectory) {
                        Files.createDirectories(newPath);
                    } else {
                        if (newPath.getParent() != null) {
                            if (Files.notExists(newPath.getParent())) {
                                Files.createDirectories(newPath.getParent());
                            }
                        }
                        Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            throw e;
        }
    }
    
    /**
     * Protects from ZIP slip attack.
     * 
     * @param zipEntry the ZIP entry
     * @param targetDir the target directory
     * @return the normalized/fixed path
     * @throws IOException if something I/O related fails
     */
    private static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir)
        throws IOException {
        Path targetDirResolved = targetDir.resolve(zipEntry.getName());
        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }
        return normalizePath;
    }
    
}
