/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

import java.io.File;
import java.util.Optional;

/**
 * Java utils with based on more recent JDK libraries.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JavaUtils {
    
    /**
     * Returns the path to the running Java binary.
     * 
     * @return the path, may be <b>null</b> for unknown
     */
    public static String getJavaBinaryPath() {
        String result = null;
        Optional<String> jp = ProcessHandle.current()
            .info()
            .command();
        if (jp.isPresent()) {
            result = jp.get();
        }
        return result;
    }
    
    /**
     * Returns the path to the running JVM bin folder.
     * 
     * @return the path, may be <b>null</b> for unknown
     */
    public static String getJavaPath() {
        String javaPath = getJavaBinaryPath();
        if (null != javaPath) {
            int pos = javaPath.lastIndexOf(File.separator);
            if (pos > 0) {
                javaPath = javaPath.substring(0, pos);
            }
        } else {
            javaPath = System.getProperty("sun.boot.library.path");
            if (null != javaPath) {
                int pos = javaPath.lastIndexOf(File.separator + "lib");
                if (pos > 0) { // linux
                    javaPath = javaPath.substring(0, pos) + File.separator + "bin";
                }
            }
        }
        return javaPath;
    }

}
