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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.SystemUtils;

import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Implements an abstract asynchronous process-based service.
 * 
 * @param <I> the input data type
 * @param <SI> the service input data type
 * @param <SO> the service output data type
 * @param <O> the output data type
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractProcessService<I, SI, SO, O> extends AbstractService {

    private TypeTranslator<I, String> inTrans;
    private TypeTranslator<String, O> outTrans;
    private ReceptionCallback<O> callback;

    /**
     * Creates an instance of the service with the required type translators.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when data from the service is available
     * @param yaml the service description 
     */
    protected AbstractProcessService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml) {
        super(yaml);
        this.inTrans = inTrans;
        this.outTrans = outTrans;
        this.callback = callback;
    }
    
    /**
     * Requests to process the given data item.
     * 
     * @param data the data to process
     * @throws IOException if processing/transferring to the service fails
     */
    public abstract void process(I data) throws IOException;

    /**
     * Returns the reception callback.
     * 
     * @return the callback
     */
    protected ReceptionCallback<O> getReceptionCallback() {
        return callback;
    }
    
    /**
     * Returns the input translator.
     * 
     * @return the translator
     */
    protected TypeTranslator<I, String> getInputTranslator() {
        return inTrans;
    }

    /**
     * Returns the output translator.
     * 
     * @return the translatir
     */
    protected TypeTranslator<String, O> getOutputTranslator() {
        return outTrans;
    }
    
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
     * Creates and starts a command line process.
     * 
     * @param exe the executable to run
     * @param dir the home dir where to execute the process within
     * @param args the process arguments 
     * @return the created process instance
     * @throws IOException if process creation fails
     */
    public static Process createProcess(File exe, File dir, List<String> args) throws IOException {
        List<String> tmp = new ArrayList<String>();
        tmp.add(exe.getAbsolutePath());
        tmp.addAll(args);
        
        System.out.println("Cmd line: " + tmp);
        ProcessBuilder processBuilder = new ProcessBuilder(tmp);        
        processBuilder.directory(dir);
        //processBuilder.inheritIO(); // somehow does not work in Jenkins/Maven surefire testing
        return processBuilder.start();
    }
    
    /**
     * Redirects an input stream to another stream (in parallel).
     * 
     * @param in the input stream of the spawned process (e.g., input/error)
     * @param dest the destination stream within this class
     */
    public static void redirectIO(final InputStream in, final PrintStream dest) {
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
    
}
