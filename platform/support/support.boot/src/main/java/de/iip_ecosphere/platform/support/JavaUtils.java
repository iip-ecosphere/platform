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
import java.lang.reflect.Method;
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
     * @param dflt the default value to return if the original value is unknown
     * @return the path, may be dflt
     */
    public static String getJavaBinaryPath(String dflt) {
        String result = getJavaBinaryPath();
        if (null == result) {
            result = dflt;
        }
        return result;
    }

    // checkstyle: stop exception type check

    /**
     * Returns the path to the running Java binary.
     * 
     * @return the path, may be <b>null</b> for unknown
     */
    public static String getJavaBinaryPath() {
        String result = null;
        try {
            Class<?> cls = Class.forName("java.lang.ProcessHandle");
            /*
                Optional<String> jp = ProcessHandle.current()
                    .info()
                    .command();
             */
            Method m = cls.getDeclaredMethod("current");
            Object tmp = m.invoke(null);
            if (null != tmp) {
                m = tmp.getClass().getDeclaredMethod("info");
                m.setAccessible(true);
                tmp = m.invoke(tmp);
                if (tmp != null) {
                    m = tmp.getClass().getDeclaredMethod("command");
                    m.setAccessible(true);
                    tmp = m.invoke(tmp);
                }
            }
            if (tmp instanceof Optional) {
                @SuppressWarnings("unchecked")
                Optional<String> jp = (Optional<String>) tmp;
                if (jp.isPresent()) {
                    result = jp.get();
                }
            }
        } catch (Throwable e) { // all typical reflection, including add-opens stuff by recent JDK
        }
        return result;
    }
    
    // checkstyle: resume exception type check
    
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
