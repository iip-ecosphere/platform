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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Basic JAR/ZIP utilities. Streams given as parameters in this class are generic {@link InputStream}s to be used
 * with class/resource loading. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class ZipUtils {
    
    /**
     * Preventing external creation.
     */
    protected ZipUtils() {
    }
    
    /**
     * Finds a file within the ZIP/JAR file given by {@code in}. Closes {@code in} if not found.
     * 
     * @param in the input stream containing ZIP/JAR data
     * @param name the name of the file within {@code in} to be returned
     * @return the input stream to {@code name} (must be closed explicitly) or <b>null</b> for none
     * @throws IOException if something I/O related fails
     */
    public static InputStream findFile(InputStream in, String name) throws IOException {
        return findFile(in, z -> z.getName().equals(name));
    }

    /**
     * Finds a file within the ZIP/JAR file given by {@code in}. Closes {@code in} if not found.
     * 
     * @param in the input stream containing ZIP/JAR data
     * @param pred the predicate to identify the file
     * @return the input stream to {@code name} (must be closed explicitly) or <b>null</b> for none
     * @throws IOException if something I/O related fails
     * @see #findFile(File, Predicate)
     */
    public static InputStream findFile(InputStream in, Predicate<ZipEntry> pred) throws IOException {
        InputStream found = null;
        try {
            ZipInputStream zis = new ZipInputStream(in);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (pred.test(zipEntry)) {
                    found = zis;
                    break;
                }
                zipEntry = zis.getNextEntry();
            }
            if (null == found) {
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw e;
        }
        return found;
    }
    
    /**
     * Finds a file within the ZIP/JAR file given by {@code file}. Same as {@link #findFile(InputStream, Predicate)},
     * but may work on ZIP files that have been processed as stream or Zip file system, i.e., in case of the bug that 
     * only deflated entries can have ext descriptors.
     * 
     * @param file the ZIP/JAR file
     * @param pred the predicate to identify the file
     * @return the input stream to {@code name} (must be closed explicitly) or <b>null</b> for none
     * @throws IOException if something I/O related fails
     */
    public static InputStream findFile(File file, Predicate<ZipEntry> pred) throws IOException {
        // https://stackoverflow.com/questions/47208272/android-zipinputstream-only-deflated-entries-can-have-
        // ext-descriptor
        InputStream found = null;
        ZipFile zf = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries =  zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (pred.test(zipEntry)) {
                found = new ClosingInputStream(zf.getInputStream(zipEntry), zf);
                break;
            }
        }
        // do not close zf here, happens in ClosingInputStream
        return found;
    }
    
    /**
     * Finds a file within the ZIP/JAR file given by {@code file}. Same as {@link #findFile(InputStream, Predicate)},
     * but may work on ZIP files that have been processed as stream or Zip file system, i.e., in case of the bug that 
     * only deflated entries can have ext descriptors.
     * 
     * @param file the ZIP/JAR file
     * @param name the name of the file within {@code in} to be returned
     * @return the input stream to {@code name} (must be closed explicitly) or <b>null</b> for none
     * @throws IOException if something I/O related fails
     */
    public static InputStream findFile(File file, String name) throws IOException {
        return findFile(file, z -> z.getName().equals(name));
    }    

    /**
     * A delegating input stream that closes a given closable after closing this stream. Typically, the closeable
     * is some parent stream.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ClosingInputStream extends DelegatingInputStream {

        private Closeable closeable;
        
        /**
         * Creates an instance.
         * 0
         * @param delegate the instance to delegate the operations to
         * @param closeable the closable to close after this stream
         */
        public ClosingInputStream(InputStream delegate, Closeable closeable) {
            super(delegate);
            this.closeable = closeable;
        }
        
        @Override
        public void close() throws IOException {
            super.close();
            closeable.close();
        }
        
    }
    
    /**
     * Finds a file within the ZIP/JAR file given by {@code in}. Closes {@code in}.
     * 
     * @param in the input stream containing ZIP/JAR data
     * @param pred optional predicate to determine the entries to return, may be <b>null</b>
     * @param consumer called for an ZIP entry to be listed, called also for folders if accepted by {@code pred}
     * @throws IOException if something I/O related fails
     */
    public static void listFiles(InputStream in, Predicate<ZipEntry> pred, Consumer<ZipEntry> consumer) 
        throws IOException {
        try {
            ZipInputStream zis = new ZipInputStream(in);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (null == pred || pred.test(zipEntry)) {
                    consumer.accept(zipEntry);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            throw e;
        }
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
        extractZip(in, target, pred, false, null);
    }

    /**
     * Extracts a ZIP/JAR file.
     * 
     * @param in the input stream containing the ZIP/JAR file
     * @param target the target path
     * @param pred a predicate selecting ZIP entries for creation/extraction (may be <b>null</b> for all contents) 
     * @param flatten whether directories shall be flattened or unpacked
     * @param pathConsumer consumes paths of unpacked files (may be <b>null</b> for none)
     * @throws IOException if something I/O related fails
     */
    public static void extractZip(InputStream in, Path target, Predicate<ZipEntry> pred, boolean flatten, 
        Consumer<Path> pathConsumer) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(in)) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                boolean isDirectory = zipEntry.isDirectory();
                if (null == pred || pred.test(zipEntry)) {
                    Path newPath = zipSlipProtect(zipEntry, target);
                    if (isDirectory) {
                        if (!flatten) {
                            Files.createDirectories(newPath);
                        }
                    } else {
                        if (newPath.getParent() != null && !flatten) {
                            if (Files.notExists(newPath.getParent())) {
                                Files.createDirectories(newPath.getParent());
                            }
                        }
                        if (flatten) {
                            newPath = Paths.get(target.toString(), newPath.getFileName().toString());
                        }
                        Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                        if (null != pathConsumer) {
                            pathConsumer.accept(newPath);
                        }
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
