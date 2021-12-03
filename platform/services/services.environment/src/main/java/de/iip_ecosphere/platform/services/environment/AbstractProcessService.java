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

package de.iip_ecosphere.platform.services.environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.TimeUtils;
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
    private YamlService serviceSpec;
    private PrintWriter serviceIn;
    private Process proc;

    /**
     * Creates an instance of the service with the required type translators.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when data from the service is available
     * @param serviceSpec the service description 
     */
    protected AbstractProcessService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService serviceSpec) {
        super(serviceSpec);
        this.inTrans = inTrans;
        this.outTrans = outTrans;
        this.callback = callback;
        this.serviceSpec = serviceSpec;
    }
    
    /**
     * Requests to process the given data item.
     * 
     * @param data the data to process
     * @throws IOException if processing/transferring to the service fails
     */
    public abstract void process(I data) throws IOException;

    /**
     * Returns the service specification.
     * 
     * @return the service specification
     */
    protected YamlService getServiceSpec() {
        return serviceSpec;
    }
    
    /**
     * Returns the process specification within {@link #getServiceSpec()}.
     * 
     * @return the process specification, may be <b>null</b>
     */
    protected YamlProcess getProcessSpec() {
        return null == serviceSpec ? null : serviceSpec.getProcess();
    }
    
    /**
     * Adds the command line arguments from {@code #getProcessSpec()} to {@code args}.
     * 
     * @param args the arguments to be modified as a side effect
     */
    protected void addProcessSpecCmdArg(List<String> args) {
        YamlProcess pSpec = getProcessSpec();
        if (null != pSpec && null != pSpec.getCmdArg()) {
            args.addAll(pSpec.getCmdArg());
        }            
    }
    
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
     * @param byName add the {@code exe} by name or (if {@code false}) by absolute name
     * @param dir the home dir where to execute the process within
     * @param args the process arguments (may be <b>null</b> for none) 
     * @return the created process instance
     * @throws IOException if process creation fails
     */
    public static Process createProcess(File exe, boolean byName, File dir, List<String> args) throws IOException {
        List<String> tmp = new ArrayList<String>();
        if (byName) {
            tmp.add(exe.getName());
        } else {
            tmp.add(exe.getAbsolutePath());
        }
        if (null != args) {
            tmp.addAll(args);
        }
        
        LoggerFactory.getLogger(AbstractProcessService.class).info("Cmd line: " + tmp + " in " + dir);
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
    
    @Override
    public void activate() throws ExecutionException {
        super.setState(ServiceState.ACTIVATING);
        stop();
        super.setState(ServiceState.ACTIVATING);
    }

    @Override
    public void passivate() throws ExecutionException {
        super.setState(ServiceState.PASSIVATING);
        start();
        super.setState(ServiceState.PASSIVATED);
    }

    @Override
    public void setState(ServiceState state) throws ExecutionException {
        switch (state) {
        case STARTING:
            start();
            break;
        case STOPPING:
            stop();
            break;
        default:
            break;
        }
        super.setState(state);
    }

    /**
     * Preliminary: Starts the service and the background process.
     * 
     * @throws ExecutionException if starting the process fails
     */
    protected abstract void start() throws ExecutionException;

    /**
     * Preliminary: Stops the service and the background process.
     */
    protected void stop() {
        if (null != serviceIn) {
            serviceIn.flush();
            serviceIn = null;
        }
        if (null != proc) {
            TimeUtils.sleep(Math.max(0, getWaitTimeBeforeDestroy()));
            proc.destroy();
            proc = null;
        }
    }

    /**
     * Returns an optional time to wait before destroying the process.
     * 
     * @return the wait time in ms, may be 0 for none, default 300 ms
     */
    protected int getWaitTimeBeforeDestroy() {
        return 300;
    }
    
    /**
     * Handles the output stream upon process creation.
     * 
     * @param out the process output stream
     */
    protected void handleOutputStream(OutputStream out) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        serviceIn = new PrintWriter(writer);
    }

    /**
     * Handles the input stream upon process creation.
     * 
     * @param in the process input stream
     */
    protected abstract void handleInputStream(InputStream in);
    //BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

    /**
     * Handles the error stream upon process creation.
     * 
     * @param err the process error stream
     */
    protected void handleErrorStream(InputStream err) {
        redirectIO(err, System.err);
    }

    /**
     * Creates, configures and starts a command line process.
     * 
     * @param exe the executable to run
     * @param byName add the {@code exe} by name or (if {@code false}) by absolute name
     * @param dir the home dir where to execute the process within
     * @param args the process arguments (may be <b>null</b> for none)
     * @return the created process instance
     * 
     * @throws IOException if process creation fails
     * @see #handleInputStream(InputStream)
     * @see #handleErrorStream(InputStream)
     * @see #handleOutputStream(OutputStream)
     */
    protected Process createAndConfigureProcess(File exe, boolean byName, File dir, List<String> args) 
        throws ExecutionException {
        try {
            proc = createProcess(exe, byName, dir, args);
            handleOutputStream(proc.getOutputStream());
            handleInputStream(proc.getInputStream());
            handleErrorStream(proc.getErrorStream());
        } catch (IOException e) {
            throw new ExecutionException(e);
        }        
        return proc;
    }
    
    /**
     * Returns the print writer wrapping the process service input stream created by 
     * {@link #handleInputStream(InputStream)}.
     * 
     * @return the stream
     */
    protected PrintWriter getServiceIn() {
        return serviceIn;
    }
    
    /**
     * Selects between {@code value} and {@code dflt}, if {@code value} is <b>null</b> use {@code dflt}, else 
     * the value of {@code value}.  
     * 
     * @param <T> the value type
     * @param value the primary value
     * @param dflt the default value to use if {@code value} is <b>null</b>
     * @return either {@code value} or {@code dflt}
     */
    protected static <T> T selectNotNull(T value, T dflt) {
        T result = value;
        if (null == result) {
            result = dflt;
        } 
        return result;
    }

    /**
     * Selects between a value of {@code object} determined by {@code valueFunc} and {@code dflt},  
     * if {@code object} is <b>null</b> or the result of {@code valueFunc} is <b>null</b> use {@code dflt}, else 
     * the value of {@code valueFunc}.
     * 
     * @param <O> the object type
     * @param <T> the value type
     * @param object the object to take the value from
     * @param valueFunc the function to apply on {@code object}
     * @param dflt the default value to use if {@code object} or the result of {@code valueFunc} is <b>null</b>
     * @return either the result of {@code valueFunc} or {@code dflt}
     */
    protected static <O, T> T selectNotNull(O object, Function<O, T> valueFunc, T dflt) {
        return selectNotNull(object != null ? valueFunc.apply(object) : null, dflt);
    }

}
