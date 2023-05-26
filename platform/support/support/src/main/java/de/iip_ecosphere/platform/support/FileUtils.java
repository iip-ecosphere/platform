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
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.SystemUtils;

/**
 * Basic file functionality.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FileUtils {
    
    /**
     * Preventing external creation.
     */
    private FileUtils() {
    }

    /**
     * Creates a temporary folder in {@code java.io.tmpdir} without cleanup.
     * 
     * @param name the name of the temporary folder within the system/user temporary directory
     * @return the temporary folder (descriptor)
     */
    public static File createTmpFolder(String name) {
        return createTmpFolder(name, false);
    }

    /**
     * Creates a temporary folder in {@code java.io.tmpdir}.
     * 
     * @param name the name of the temporary folder within the system/user temporary directory
     * @param cleanup try to do an auto cleanup at JVM shutdown
     * @return the temporary folder (descriptor), may receive a suffix if there is already a 
     *     folder that cannot be deleted
     */
    public static File createTmpFolder(String name, boolean cleanup) {
        String tmp = System.getProperty("java.io.tmpdir");
        File result = new File(tmp, name);
        if (result.exists() && !deleteQuietly(result)) {
            result = new File(tmp, name + "_" + System.currentTimeMillis());
        }
        result.mkdir();
        if (cleanup) {
            result.deleteOnExit();
        }
        return result;
    }
    
    /**
     * Deletes a file or directory, not throwing an exception. If file is a directory, delete it and all 
     * sub-directories. [convenience]
     *
     * @param file file or directory to delete, may be {@code null}
     * @return {@code true} if {@code file} was deleted, otherwise {@code false}
     */
    public static boolean deleteQuietly(File file) {
        return org.apache.commons.io.FileUtils.deleteQuietly(file);        
    }
    
    /**
     * Deletes a file or directory on JVM exit, not throwing an exception. If file is a directory, delete it and all 
     * sub-directories. [convenience]
     *
     * @param file file or directory to delete, may be {@code null}
     */
    public static void deleteOnExit(File file) {
        try {
            if (null != file) {
                org.apache.commons.io.FileUtils.forceDeleteOnExit(file);
            }
        } catch (IOException e) {
        }
    }

    /**
     * Returns the path to the system temporary directory. [convenience]
     *
     * @return the path to the system temporary directory.
     */
    public static String getTempDirectoryPath() {
        return org.apache.commons.io.FileUtils.getTempDirectoryPath();
    }

    /**
     * Returns a {@link File} representing the system temporary directory. [convenience]
     *
     * @return the system temporary directory.
     */
    public static File getTempDirectory() {
        return org.apache.commons.io.FileUtils.getTempDirectory();
    }

    /**
     * Closes a closable quietly.
     * 
     * @param closable the closable, may be <b>null</b>
     */
    public static void closeQuietly(Closeable closable) {
        if (null != closable) {
            try {
                closable.close();
            } catch (IOException e ) {
                // do nothing, quietly
            }
        }
    }
    
    /**
     * Lists contained files.
     * 
     * @param file the file/folder to list
     * @param accept accept the file for further (nested) listing, not called for folders
     * @param handle handle an accepted file
     */
    public static void listFiles(File file, Predicate<File> accept, Consumer<File> handle) {
        if (accept.test(file)) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (null != files) {
                    for (File f : files) {
                        listFiles(f, accept, handle);
                    }
                }
            } else {
                handle.accept(file);
            }
        }
    }
    
    /**
     * Composes a path and returns the canonical/absolute path (single-time use).
     * 
     * @param path the path
     * @param name the name to be added (may be a path, may be empty or <b>null</b>)
     * @return the canonical/absolute path
     */
    public static String getResolvedPath(File path, String name) {
        File f;
        if (null != name && name.length() > 0) {
            f = new File(path, name);
        } else {
            f = path;
        }
        try {
            return f.getCanonicalPath();
        } catch (IOException e) {
            return f.getAbsolutePath();
        }
    }

    /**
     * Resolves a path and returns the canonical/absolute path.
     * 
     * @param path the path
     * @return the canonical/absolute path
     */
    public static File getResolvedFile(File path) {
        try {
            return path.getCanonicalFile();
        } catch (IOException e) {
            return path.getAbsoluteFile();
        }
    }
    
    /**
     * Turn {@code file} into a base64 encoded string.
     * 
     * @param file the file
     * @return the base64 encoded string
     * @throws IOException if {@code file} cannot be read
     */
    public static String fileToBase64(File file) throws IOException {
        byte[] fileContent = org.apache.commons.io.FileUtils.readFileToByteArray(file);
        return Base64.getEncoder().encodeToString(fileContent);        
    }

    /**
     * Turn base64 encoded {@code string} into a {@code file}.
     * 
     * @param string the base64 encoded string
     * @param file the file
     * @throws IOException if {@code file} cannot be written
     */
    public static void base64ToFile(String string, File file) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(string);
        org.apache.commons.io.FileUtils.writeByteArrayToFile(file, decodedBytes);   
    }
    
    /**
     * Turns an arbitrary string into something that can be used as a file name. 
     * 
     * @param str the string to use
     * @return the file name
     */
    public static String sanitizeFileName(String str) {
        return str.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
    
    /**
     * Turns an arbitrary string into something that can be used as a file name. 
     * 
     * @param str the string to use
     * @param addTimestamp whether the current timestamp shall be added to {@code str}
     * @return the file name
     * @see #sanitizeFileName(String)
     */
    public static String sanitizeFileName(String str, boolean addTimestamp) {
        String tmp = addTimestamp ? str + "-" + System.currentTimeMillis() : str;
        return sanitizeFileName(tmp);
    }

    /**
     * Tries to find the file {@code name} in {@code folder} and its subfolders.
     * 
     * @param folder the folder to search
     * @param name the file name to find
     * @return the found file or <b>null</b> for none
     */
    public static File findFile(File folder, String name) {
        Collection<File> tmp = org.apache.commons.io.FileUtils.listFiles(folder, 
            FileFilterUtils.nameFileFilter(name), 
            TrueFileFilter.INSTANCE);
        Iterator<File> iter = tmp.iterator();
        File result;
        if (iter.hasNext()) {
            result = iter.next();
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Returns the system root folder.
     * 
     * @return the system root
     */
    public static File getSystemRoot() {
        // https://stackoverflow.com/questions/4362786/getting-the-default-root-directory-in-java
        File result;
        if (SystemUtils.IS_OS_WINDOWS) {
            String sysDrive = System.getenv("SystemDrive");
            if (null == sysDrive) {
                sysDrive = "C:";
            }
            result = new File(sysDrive);
        } else {
            result = new File("/");
        }
        return result;
    }

}
