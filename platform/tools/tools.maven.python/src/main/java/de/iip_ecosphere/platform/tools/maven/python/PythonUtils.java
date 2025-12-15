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
import java.util.StringTokenizer;

import org.apache.commons.lang3.SystemUtils;

/**
 * Some generic Python process helper functions. For now taken over to keep this plugin on Java 8. Considers from 
 * environment {@code IIP_PYTHON} as Python binary to use as well as {@code IIP_PYTHONARGS} as additional Python 
 * interpreter arguments. Tries to find Python (python3, python) in system path.
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
        return getPythonExecutable(null);
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
     * @param pythonBinary explicitly given path to python binary
     * @return the executable, returns at least "python"; the result may be an absolute path but must not be. Calling 
     *     {@link File#getAbsolutePath()} on the result may be misleading - if required, use it as string.
     */
    public static File getPythonExecutable(String pythonBinary) {
        File result = pythonExecutable; 
        // this is not nice, but at the moment it is rather difficult to pass an option via ANT to Maven to Surefire
        String path = System.getenv("IIP_PYTHON");
        if (null == path) {
            path = pythonBinary;
        }
        if (null == path) {
            path = "/var/lib/jenkins/python/active/bin/python3"; // JENKINS, still legacy
        }
        File tmpPath = new File(path);
        if (tmpPath.exists()) {
            result = tmpPath;
        } else {
            path = System.getenv(SystemUtils.IS_OS_WINDOWS ? "PATH" : "path");
            if (null != path) {
                StringTokenizer pathEntries = new StringTokenizer(path, File.pathSeparator);
                while (pathEntries.hasMoreTokens()) {
                    String pe = pathEntries.nextToken();
                    tmpPath = new File(pe, "python3");
                    if (tmpPath.exists()) {
                        result = tmpPath;
                        break;
                    }
                    tmpPath = new File(pe, "python");
                    if (tmpPath.exists()) {
                        result = tmpPath;
                        break;
                    }
                }
            }
            if (null == result) {
                tmpPath = new File("/usr/bin/python3"); // Standard Unix... do we need more???
                if (tmpPath.exists()) {
                    result = tmpPath;
                }
            }
        }
        
        if (null == result) {
            result = new File("python");
        }
        return result;
    }

    /**
     * Returns the python interpreter arguments either from the string passed in or from {@code IIP_PYTHONARGS}.
     * 
     * @param pythonArgs the python interpreter arguments, may be <b>null</b> or empty
     * @return the python interpreter arguments, may be <b>null</b> or empty
     */
    public static String getPythonArgs(String pythonArgs) {
        String result = pythonArgs;
        if (null == result || result.isEmpty()) {
            result = System.getenv("IIP_PYTHONARGS");
        }
        return result;
    }
    
    /**
     * Inserts arguments into a (command line) array.
     * 
     * @param cmd the original (command line) array
     * @param pos the position where to insert, call is ignored if the position is outside the array
     * @param args the arguments to insert, call is ignored if <b>null</b> or empty, args are split by spaces
     * @return the (extended) arguments
     */
    public static final String[] insertArgs(String[] cmd, int pos, String args) {
        String[] result = cmd;
        if (args == null) {
            args = "";
        }
        args = args.trim();
        if (args.length() > 0) {
            String[] tmpArgs = args.split(" ");
            if (pos >= 0 || pos <= cmd.length) {
                result = new String[cmd.length + tmpArgs.length];
                // Copy elements before position
                System.arraycopy(cmd, 0, result, 0, pos);
                // Copy array to insert
                System.arraycopy(tmpArgs, 0, result, pos, tmpArgs.length);
                // Copy remaining elements
                System.arraycopy(cmd, pos, result, pos + tmpArgs.length, cmd.length - pos);
            }
        }
        return result;
    }
    
}
