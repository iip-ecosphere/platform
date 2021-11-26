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

package de.iip_ecosphere.platform.security.services.kodex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.SystemUtils;

import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Integration of <a href="https://github.com/kiprotect/kodex">KIPROTECT KODEX</a> as a service.
 * 
 * @author Holger Eichelberger, SSE
 */
public class KodexService {
    
    private static final String VERSION = "0.0.7";
    
    // TODO move up to service environment
    /**
     * Redirects an input stream to another stream (in parallel).
     * 
     * @param in the input stream of the spawned process (e.g., input/error)
     * @param dest the destination stream within this class
     */
    private static void redirectIO(final InputStream in, final PrintStream dest) {
        new Thread(new Runnable() {
            public void run() {
                Scanner sc = new Scanner(in);
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    dest.println(line);
                }
                sc.close();
            }
        }).start();
    }
    
    // TODO move up to service environment
    /**
     * Returns the operating system and architecture in typical form, e.g, win32, win, win64 or linux32, linux, linux64.
     * 
     * @param name32 shall the method add 32 in case of a 32 bit operating system (explicit) or be quite (implicit) and 
     *     add only 64 in case of 64 bit systems
     * @return the operating system and architecture name
     */
    public static String getOsArch(boolean name32) {
        // https://stackoverflow.com/questions/47160990/how-to-determine-32-bit-os-or-64-bit-os-from-java-application
        String os = "";
        String arch = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            os = "win";
            String winArch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            arch = winArch != null && winArch.endsWith("64")
                || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
        } else { // we do not make further distinctions here for now
            os = "linux";
        }
        if (null == arch) {
            if (SystemUtils.OS_ARCH.endsWith("64")) {
                arch = "64";
            } else {
                arch = "32";
            }
        }
        if (!name32 && "32".equals(arch)) {
            arch = "";
        }
        return os + arch;
    }
    
    // TODO move up to service environment
    /**
     * Returns the executable file suffix.
     * 
     * @return the suffix, may be empty
     */
    public static String getExecutableSuffix() {
        String result = "";
        if (SystemUtils.IS_OS_WINDOWS) {
            result = ".exe";
        }
        return result;
    }
    
    // TODO move up to service environment
    /**
     * Constructs an executable name.
     * 
     * @param program the program name
     * @param version the version of the program
     * @param name32 shall the method add 32 in case of a 32 bit operating system (explicit) or be quite (implicit) and 
     *     add only 64 in case of 64 bit systems
     * @return the executable name
     * @see #getOsArch(boolean)
     * @see #getExecutableSuffix()
     */
    public static String getExecutableName(String program, String version, boolean name32) {
        return program + "-" + version + "-" + getOsArch(name32) + getExecutableSuffix();        
    }

    // TODO move up to service environment
    /**
     * Constructs an executable name not naming 32 bit.
     * 
     * @param program the program name
     * @param version the version of the program
     * @return the executable name
     * @see #getOsArch(boolean)
     * @see #getExecutableSuffix()
     */
    public static String getExecutableName(String program, String version) {
        return getExecutableName(program, version, false);
    }

    /**
     * Creates and starts a Python process.
     * 
     * @param dir the home dir where to find the script/run it within
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createProcess(File dir, String... args) throws IOException {
        String executable = getExecutableName("kodex", VERSION);
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
        
        redirectIO(proc.getInputStream(), System.out);
        redirectIO(proc.getErrorStream(), System.err);
        System.out.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.flush();
        TimeUtils.sleep(1000); // preliminary, Andreas will try to fix this
        proc.destroy();
    }
    
}
