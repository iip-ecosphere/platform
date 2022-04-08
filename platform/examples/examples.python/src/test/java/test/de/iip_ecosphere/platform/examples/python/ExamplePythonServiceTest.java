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

package test.de.iip_ecosphere.platform.examples.python;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.PythonUtils;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Basic testing for the Python service implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ExamplePythonServiceTest {

    /**
     * Tests whether the service code can be compiled and basically executed.
     * 
     * @throws IOException shall not occur
     * @throws InterruptedException shall not occur
     */
    @Test
    public void testService() throws IOException, InterruptedException {
        int port = NetUtils.getEphemeralPort();
        Process python = createPythonProcess(new File("src/test/python"), "__init__.py", "--port", 
            String.valueOf(port));
        // add protocol
        TimeUtils.sleep(1000); // works without on Windows, but not on Jenkins/Linux
        python.destroy();
    }
    
    /**
     * Creates and starts a Python process.
     * 
     * @param dir the home dir where to find the script/run it within
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createPythonProcess(File dir, String... args) throws IOException {
        String pythonPath = PythonUtils.getPythonExecutable().toString();
        System.out.println("Using Python: " + pythonPath);
        List<String> tmp = new ArrayList<String>();
        tmp.add(pythonPath);
        for (String a : args) {
            tmp.add(a);
        }
        
        System.out.println("Cmd line: " + tmp);
        ProcessBuilder processBuilder = new ProcessBuilder(tmp);        
        processBuilder.directory(dir);
        //processBuilder.inheritIO(); // somehow does not work in Jenkins/Maven surefire testing
        Process python = processBuilder.start();
        redirectIO(python.getInputStream(), System.out);
        redirectIO(python.getErrorStream(), System.err);
        return python;
    }
    
    /**
     * Redirects an input stream to another stream (in parallel).
     * 
     * @param src the source stream
     * @param dest the destination stream
     */
    private static void redirectIO(final InputStream src, final PrintStream dest) {
        new Thread(new Runnable() {
            public void run() {
                Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    dest.println(sc.nextLine());
                }
                sc.close();
            }
        }).start();
    }
    
}
