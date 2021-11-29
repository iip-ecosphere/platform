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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Integration of <a href="https://github.com/kiprotect/kodex">KIPROTECT KODEX</a> as a service.
 * 
 * @param <I> the input type
 * @param <O> the output type
 * @author Holger Eichelberger, SSE
 */
public class KodexService<I, O> extends AbstractProcessService<I, String, String, O>  {

    public static final int WAITING_TIME = 120000; // preliminary
    private static final String VERSION = "0.0.7";
    private static final boolean DEBUG = true;
    private PrintWriter serviceIn;
    private Process proc;

    /**
     * Creates an instance of the service with the required type translators to/from JSON.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when a processed item is received from the serivce
     */
    public KodexService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback) {
        super(inTrans, outTrans, callback);
    }
    
    @Override
    public void process(I data) throws IOException {
        serviceIn.println(getInputTranslator().to(data));
    }
    
    /**
     * Preliminary: Starts the service and the background process.
     * 
     * @throws IOException if starting the process fails
     */
    public void start() throws IOException {
        String executable = getExecutableName("kodex", VERSION);
        File exe = new File("./src/main/resources/" + executable); // folder fixed? 
        File home = new File("./src/test/resources").getAbsoluteFile();

        List<String> a = new ArrayList<>();
        if (DEBUG) {
            a.add("--level");
            a.add("debug");
        }
        a.add("run");
        a.add("example-data.yml");
        proc = createProcess(exe, home, a);
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        //BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        serviceIn = new PrintWriter(writer);
        
        redirectIO(proc.getInputStream(), getReceptionCallback());
        redirectIO(proc.getErrorStream(), System.err);
    }
    
    /**
     * Preliminary: Stops the service and the background process.
     */
    public void stop() {
        if (null != serviceIn) {
            serviceIn.flush();
            serviceIn = null;
        }
        if (null != proc) {
            TimeUtils.sleep(WAITING_TIME); // preliminary, Andreas will try to fix this
            proc.destroy();
            proc = null;
        }
    }
    
    /**
     * Redirects an input stream to another stream (in parallel).
     * 
     * @param in the input stream of the spawned process (e.g., input/error)
     * @param callback the callback to inform
     */
    public void redirectIO(final InputStream in, ReceptionCallback<O> callback) {
        if (null != callback) {
            new Thread(new Runnable() {
                public void run() {
                    Scanner sc = new Scanner(in);
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();
                        try {
                            callback.received(getOutputTranslator().to(line));
                        } catch (IOException e) {
                            LoggerFactory.getLogger(getClass()).error("Receiving result: " + e.getMessage());
                        }
                    }
                    sc.close();
                }
            }).start();
        }
    }
    
    // preliminary, to be removed
    
    /**
     * Test execution of Kodex.
     * 
     * @param args command line arguments
     * @throws IOException in case that the command line streams break
     * @throws InterruptedException in case that the Kodex process is interrupted unexpectedly 
     */
    public static void main(String... args) throws IOException, InterruptedException {
        String executable = getExecutableName("kodex", VERSION);
        File exe = new File("./src/main/resources/" + executable); // folder fixed? 
        File home = new File("./src/test/resources").getAbsoluteFile();
        boolean debug = true;

        List<String> a = new ArrayList<>();
        if (debug) {
            a.add("--level");
            a.add("debug");
        }
        a.add("run");
        a.add("example-data.yml");
        Process proc = createProcess(exe, home, a);
        //PrintStream in = new PrintStream(new ByteArrayOutputStream());
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        //BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        PrintWriter w = new PrintWriter(writer);
        
        redirectIO(proc.getInputStream(), System.out);
        redirectIO(proc.getErrorStream(), System.err);
        w.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.println("{\"name\": \"test\", \"id\": \"test\"}");
        w.flush();
        TimeUtils.sleep(WAITING_TIME); // preliminary, Andreas will try to fix this
        proc.destroy();
    }
    
}
