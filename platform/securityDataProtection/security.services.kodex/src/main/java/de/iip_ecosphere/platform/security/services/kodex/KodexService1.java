package de.iip_ecosphere.platform.security.services.kodex;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Integration of <a href="https://github.com/kiprotect/kodex">KIPROTECT KODEX</a> as a service. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class KodexService1 {
    
    public static final int WAITING_TIME = 120000; // preliminary
    private static final String VERSION = "0.0.7";
    
    
    /**
     * Creates and starts a Python process.
     * 
     * @param dir the home dir where to find the script/run it within
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createProcess(File dir, String... args) throws IOException {
        String executable = AbstractProcessService.getExecutableName("kodex", VERSION);
        File exe = new File("./src/main/resources/" + executable); // folder fixed? 
        List<String> tmp = new ArrayList<String>();
        tmp.add(exe.getAbsolutePath());
        for (String a : args) {
            tmp.add(a);
        }
        
        System.out.println("Cmd line: " + tmp);
        ProcessBuilder processBuilder = new ProcessBuilder(tmp);        
        processBuilder.directory(dir);
        //processBuilder.inheritIO(); // somehow does not work in Jenkins/Maven surefire testing
        return processBuilder.start();
    }
    
    // preliminary
    /**
     * Test execution of Kodex.
     * 
     * @param args command line arguments
     * @throws IOException in case that the command line streams break
     * @throws InterruptedException in case that the Kodex process is interrupted unexpectedly 
     */
    public static void main(String... args) throws IOException, InterruptedException {
        File f = new File("./src/test/resources").getAbsoluteFile();

        Process proc = createProcess(f, "--level", "debug", "run", "example-data.yml");
        //PrintStream in = new PrintStream(new ByteArrayOutputStream());
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        //BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        PrintWriter w = new PrintWriter(writer);
        
        AbstractProcessService.redirectIO(proc.getInputStream(), System.out);
        AbstractProcessService.redirectIO(proc.getErrorStream(), System.err);
        System.out.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.flush();
        TimeUtils.sleep(120000); // preliminary, Andreas will try to fix this
        proc.destroy();
    }
    
}
