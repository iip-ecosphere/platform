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
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Basic file functionality.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FileUtils {

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
     * @return the temporary folder (descriptor)
     */
    public static File createTmpFolder(String name, boolean cleanup) {
        String tmp = System.getProperty("java.io.tmpdir");
        File result = new File(tmp, name);
        deleteQuietly(result);
        result.mkdir();
        if (cleanup) {
            result.deleteOnExit();
        }
        return result;
    }
    
    /**
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories. 
     * [convenience]
     *
     * @param file file or directory to delete, may be {@code null}
     * @return {@code true} if {@code file} was deleted, otherwise {@code false}
     */
    public static boolean deleteQuietly(File file) {
        return org.apache.commons.io.FileUtils.deleteQuietly(file);        
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
    
}
