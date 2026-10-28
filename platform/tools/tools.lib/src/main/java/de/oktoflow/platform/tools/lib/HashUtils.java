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

package de.oktoflow.platform.tools.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utilities for file hashes.
 * 
 * @author Gemini
 * @author Holger Eichelberger, SSE
 */
public class HashUtils {

    /**
     * Turns an IOException to a message.
     * 
     * @param ex the exception
     * @return the message
     */
    public static String toMessage(IOException ex) {
        return ex.getClass().getSimpleName() + " " + ex.getMessage();
    }
    
    /**
     * Returns the MD5 hash of {@code file}, whereby for ZIP/JAR file the hash of the contents is returned.
     * 
     * @param file the file to hash
     * @param considerContents whether the contents of ZIP/JAR files shall be considered or the file hash shall 
     *      be returned
     * @param log a log consumer
     * @return the MD5 hash, <b>null</b> if hashing is not possible (see log)
     */
    public static String getMD5(File file, boolean considerContents, Consumer<String> log) {
        String result = null;
        try {
            result = getMD5(file, considerContents);
        } catch (NoSuchAlgorithmException e) {
            log.accept("Hashing not possible, MD5 digest not available");
        } catch (IOException e) {
            log.accept("Hashing not possible for " + file + ": " + toMessage(e));
        }
        return result;
    }

    /**
     * Returns the MD5 hash of {@code file}, whereby for ZIP/JAR file the hash of the contents is returned.
     * 
     * @param file the file to hash
     * @param considerContents whether the contents of ZIP/JAR files shall be considered or the file hash shall 
     *      be returned
     * @param log a log consumer
     * @return the MD5 hash, <b>null</b> if hashing is not possible (see log)
     * @throws NoSuchAlgorithmException if the digest 
     */
    public static String getMD5(File file, boolean considerContents) throws NoSuchAlgorithmException, IOException {
        return getHash("MD5", file, considerContents);
    }
    
    /**
     * Returns the MD5 hash of {@code file}, whereby for ZIP/JAR file the hash of the contents is returned.
     * 
     * @param file the file to hash
     * @param considerContents whether the contents of ZIP/JAR files shall be considered or the file hash shall 
     *      be returned
     * @param log a log consumer
     * @return the MD5 hash, <b>null</b> if hashing is not possible (see log)
     * @throws NoSuchAlgorithmException if the digest algorithm is not available
     * @throws IOException if the {@code file} cannot be read
     */
    public static String getHash(String algorithm, File file, boolean considerContents) 
        throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        if ((file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) && considerContents) {
            calcZipContents(digest, file);
        } else {
            digest.update(Files.readAllBytes(file.toPath()));
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    /**
     * Returns the MD5 hash of the contents of the ZIP {@code file}.
     * 
     * @param digest the digest to update
     * @param file the ZIP file to hash
     */
    private static void calcZipContents(MessageDigest digest, File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            List<? extends ZipEntry> entries = Collections.list(zipFile.entries());
            entries.sort(Comparator.comparing(ZipEntry::getName)); // guarantee deterministic ordering
            byte[] buffer = new byte[8192];
            for (ZipEntry entry : entries) {
                // Skip directories, we only care about file contents
                if (!entry.isDirectory()) {
                    digest.update(entry.getName().getBytes(StandardCharsets.UTF_8));
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            digest.update(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }
    }
    
}
