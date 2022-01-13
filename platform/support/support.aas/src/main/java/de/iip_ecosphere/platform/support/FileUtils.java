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
    
}
