package de.iip_ecosphere.platform.tools.maven.python;
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


import java.io.File;

/**
 * Some generic python process helper functions. For now taken over to keep this plugin on Java 8.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonUtils {

    public static final File DEFAULT_PYTHON_EXECUTABLE = null;
    private static File pythonExecutable = DEFAULT_PYTHON_EXECUTABLE;

    /**
     * Defines the (global) Python executable.
     * 
     * @param exec the executable, may be <b>null</b> for dynamically determined
     */
    public static void setPythonExecutable(File exec) {
        pythonExecutable = exec;
    }
    
    /**
     * Returns the Python executable. We consider the following precedences:
     * <ol>
     *   <li>Local python3 in Linux Jenkins installation (for testing, although this is production code; with 
     *       installation path)</li>
     *   <li>Local python3 in default Linux installation ("/usr/bin", with installation path)</li>
     *   <li>The global python executable {@link #setPythonExecutable(File)} (with installation path)</li>
     *   <li>The command "python"</li>
     * </ol>
     * 
     * @return the executable, returns at least "python"; the result may be an absolute path but must not be. Calling 
     *     {@link File#getAbsolutePath()} on the result may be misleading - if required, use it as string.
     */
    public static File getPythonExecutable() {
        File result = pythonExecutable; 
        // this is not nice, but at the moment it is rather difficult to pass an option via ANT to Maven to Surefire
        File tmpPath = new File("/var/lib/jenkins/python/active/bin/python3"); // JENKINS
        if (tmpPath.exists()) {
            result = tmpPath;
        } else {
            tmpPath = new File("/usr/bin/python3"); // Standard Unix... do we need more???
            if (tmpPath.exists()) {
                result = tmpPath;
            }
        }
        if (null == result) {
            result = new File("python");
        }
        return result;
    }
}
