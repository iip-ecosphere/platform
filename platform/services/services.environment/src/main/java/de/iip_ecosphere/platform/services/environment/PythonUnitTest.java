/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Executes syntactic tests on Python scripts.
 * 
 * @author Alexander Weber, SSE
 */
public class PythonUnitTest {
    
    /**
     * Runs a python command on CLI to evaluate the python service script on build
     * time.
     * 
     * @param args Args. No used.
     */
    public static void main(String[] args) {
        /*
         * This call just goes through some locations known to contain the python3
         * executable. i.e. "/usr/bin/python3" not perfect as the last option, the one
         * most likely for windows, will not return a path to look into the
         * side-packages! Also only working for as long windows user did not rename
         * python to something else to potentially run multiple version besides each
         * other
         */
        File pythonExecutable = PythonUtils.getPythonExecutable();

        //search the site_packages of the python for pyflakes! Currently not doable on windows!
        
        //Args[0] = path to the test file directory
        //Args[1] = name of the concrete test file
        //args[2] = relative path from src/test/python into the  impl.model project
        String output = "";
        String[] cmd = {pythonExecutable.getName(), args[1], args[2]}; 
        output += "This should be seeable:"; 
        output += runPythonTest(cmd, args[0]);
        System.out.println(output);
        
        //throw new ExecutionException(output, null);
    }
    /**
     * Running the syntax check for the python Files.
     * @param cmd  the command to run, shall run a python file
     * @param workingDirectory the directory of the python tests
     * @return The output to add to the other outputs
     */
    public static String runPythonTest(String[] cmd, String workingDirectory) {
        Process process;
        String output = "";
        try {
            process = Runtime.getRuntime().exec(cmd, null, new File(workingDirectory));
            output = readProcessOutput(process.getInputStream());
            output += readProcessOutput(process.getErrorStream());
            // only test if error is due to missing pyflakes!
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }
    
    /**
     * Shall take in input stream of processes to collect console output.
     * 
     * @param stream Process to be observed.
     * @return The read Lines from the process.
     * @throws IOException If the reading of the lines does fail.
     */
    public static String readProcessOutput(InputStream stream) throws IOException {
        StringBuffer output = new StringBuffer();
        String line = "";
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        while ((line = bufferedReader.readLine()) != null) {
            output.append(line);
            output.append("\n");
        }
        return output.toString();
    }
}
