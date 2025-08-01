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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang.SystemUtils;

import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory;
import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory.ProcessInfo;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import io.micrometer.core.instrument.Gauge;

/**
 * Implements an abstract asynchronous process-based service for a single pair of input-output types. A created 
 * service/process is stopped on JVM shutdown. 
 * 
 * @param <I> the input data type
 * @param <SI> the service input data type
 * @param <SO> the service output data type
 * @param <O> the output data type
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractProcessService<I, SI, SO, O> extends AbstractRunnablesService 
    implements MonitoringService {

    private static boolean inheritIo = false;
    private TypeTranslator<I, String> inTrans;
    private TypeTranslator<String, O> outTrans;
    private Map<Class<?>, List<ReceptionCallback<?>>> callbacks = new HashMap<>();
    private YamlService serviceSpec;
    private PrintWriter serviceIn;
    private Process proc;
    private ProcessInfo osProcess;

    /**
     * Creates an instance of the service with the required type translators.
     * 
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when data from the service is available (may be <b>null</b> if 
     * {@link #attachIngestor(Class, DataIngestor)} is called before first data processing).
     * @param serviceSpec the service description 
     */
    protected AbstractProcessService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService serviceSpec) {
        super(serviceSpec);
        this.inTrans = inTrans;
        this.outTrans = outTrans;
        this.serviceSpec = serviceSpec;
        addCallback(callback);
    }
    
    /**
     * Adds a callback.
     * 
     * @param callback the callback, ignored if <b>null</b>
     */
    private void addCallback(ReceptionCallback<?> callback) {
        if (null != callback) {
            Class<?> type = callback.getType();
            List<ReceptionCallback<?>> list = callbacks.get(type);
            if (null == list) {
                list = new ArrayList<>();
                callbacks.put(type, list);
            }
            list.add(callback);
        }
    }
    
    /**
     * Requests asynchronous processing of a data item. Calls {@link #process(Object)} and handles potential
     * exceptions.
     * 
     * @param data the data item to be processed
     */
    public void processQuiet(I data) {
        try {
            process(data);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Attaches an asynchronous result data ingestor as callback.
     * 
     * @param <P> the output type
     * @param outCls the class representing the type
     * @param ingestor the ingestor instance
     */
    public <P> void attachIngestor(Class<P> outCls, DataIngestor<P> ingestor) {
        addCallback(new ReceptionCallback<P>() {

            @Override
            public void received(P data) {
                ingestor.ingest(data);
            }

            @Override
            public Class<P> getType() {
                return outCls;
            }
        });
        if (null != proc) { // important if process is already running, otherwise done in createAndConfigureProcess
            handleInputStream(proc.getInputStream());
        }
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
     * Returns the reception callbacks for {@code out}.
     * 
     * @param <P> the output type
     * @param data the output data, may be <b>null</b> 
     */
    @SuppressWarnings("unchecked")
    protected <P> void notifyCallbacks(P data) {
        if (data != null) {
            Class<?> cls = data.getClass();
            List<ReceptionCallback<?>> cbs = callbacks.get(cls);
            if (null != cbs) {
                for (int c = 0; c < cbs.size(); c++) {
                    ((ReceptionCallback<P>) cbs.get(c)).received(data);
                }
            }
        }
    }
    
    /**
     * Returns the callbacks for a given type.
     * 
     * @param cls the type
     * @return the callbacks, may be <b>null</b> if there are none
     */
    protected Iterable<ReceptionCallback<?>> getCallbacks(Class<?> cls) {
        return callbacks.get(cls);
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
     * @return the translator
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
     * Constructs an executable name without suffix, even on Windows, not naming 32 bit.
     * 
     * @param program the program name
     * @param version the version of the program
     * @return the executable prefix
     * @see #getExecutablePrefix(String, String, boolean)
     */
    public static String getExecutablePrefix(String program, String version) {
        return getExecutablePrefix(program, version, false);
    }

    /**
     * Constructs an executable name without suffix, even on Windows.
     * 
     * @param program the program name
     * @param version the version of the program
     * @param name32 shall the method add 32 in case of a 32 bit operating system (explicit) or be quite (implicit) and 
     *     add only 64 in case of 64 bit systems
     * @return the executable prefix
     * @see #getOsArch(boolean)
     */
    public static String getExecutablePrefix(String program, String version, boolean name32) {
        return program + "-" + version + "-" + getOsArch(name32);
    }

    /**
     * Constructs an executable name.
     * 
     * @param program the program name
     * @param version the version of the program
     * @param name32 shall the method add 32 in case of a 32 bit operating system (explicit) or be quite (implicit) and 
     *     add only 64 in case of 64 bit systems
     * @return the executable name
     * @see #getExecutablePrefix(String, String, boolean)
     * @see #getExecutableSuffix()
     */
    public static String getExecutableName(String program, String version, boolean name32) {
        return getExecutablePrefix(program, version, name32) + getExecutableSuffix();        
    }

    /**
     * Constructs an executable name not naming 32 bit.
     * 
     * @param program the program name
     * @param version the version of the program
     * @return the executable name
     * @see #getExecutableName(String, String, boolean)
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
     * @see #createProcess(File, boolean, File, List, Consumer)
     */
    public static Process createProcess(File exe, boolean byName, File dir, List<String> args) throws IOException {
        return createProcess(exe, byName, dir, args, null);
    }
    
    /**
     * Creates and starts a command line process.
     * 
     * @param exe the executable to run
     * @param byName add the {@code exe} by name or (if {@code false}) by absolute name
     * @param dir the home dir where to execute the process within
     * @param args the process arguments (may be <b>null</b> for none) 
     * @param customizer allows for further customization of the process builder created in this method, may be 
     *     <b>null</b> for none
     * @return the created process instance
     * @throws IOException if process creation fails
     * @see #setInheritIo(boolean)
     */
    public static Process createProcess(File exe, boolean byName, File dir, List<String> args, 
        Consumer<ProcessBuilder> customizer) throws IOException {
        List<String> tmp = new ArrayList<String>();
        if (byName || !exe.exists()) {
            tmp.add(exe.getName());
        } else {
            tmp.add(exe.getAbsolutePath());
        }
        if (null != args) {
            tmp.addAll(args);
        }
        
        System.out.println("Cmd line: " + CollectionUtils.toStringSpaceSeparated(tmp) + " in " + dir);
        //LoggerFactory.getLogger(AbstractProcessService.class).info("Cmd line: {} in {}", 
        //     CollectionUtils.toStringSpaceSeparated(tmp), dir);
        ProcessBuilder processBuilder = new ProcessBuilder(tmp);
        processBuilder.directory(dir);
        if (null != customizer) {
            customizer.accept(processBuilder);
        }
        if (inheritIo) {
            processBuilder.inheritIO(); // somehow does not work in Jenkins/Maven surefire testing
        }
        return processBuilder.start();
    }
    
    /**
     * Sets the default value allow or preveting the inheritance of the IO setup for a process being created in 
     * {@link #createProcess(File, boolean, File, List, Consumer)}.
     * Typically switched off for conflicts with CI/surefire.
     * 
     * @param inherit allow/disable inheriting process IO setup
     * @return the value of the flag before the call
     */
    public static boolean setInheritIo(boolean inherit) {
        boolean orig = inheritIo;
        inheritIo = inherit;
        return orig;
    }
    
    /**
     * A runnable that can be stopped.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface RunnableWithStop extends Runnable {
        
        /**
         * Stops this runnable.
         */
        public void stop();
        
    }
    
    /**
     * Redirects an input stream to another stream (in parallel).
     * 
     * @param in the input stream of the spawned process (e.g., input/error)
     * @param dest the destination stream within this class
     * @return the runnable performing the redirection
     */
    public static RunnableWithStop redirectIO(final InputStream in, final PrintStream dest) {
        RunnableWithStop result = new RunnableWithStop() {
            
            private boolean cnt = true;
            
            @Override
            public void run() {
                Scanner sc = new Scanner(in);
                while (cnt && sc.hasNextLine()) {
                    String line = sc.nextLine();
                    dest.println(line);
                }
                sc.close();
            }
            
            @Override
            public void stop() {
                cnt = false;
            }
            
        };
        new Thread(result).start();
        return result;
    }

    /**
     * Waits and destroys a process with a default sleep time of 200 ms.
     * 
     * @param proc the process to be destroyed, may be <b>null</b> but nothing happens then
     * @see #waitAndDestroy(Process, int)
     */
    public static void waitAndDestroy(Process proc) {
        waitAndDestroy(proc, 200);
    }
    
    /**
     * Waits and destroys a process.
     * 
     * @param proc the process to be destroyed, may be <b>null</b> but nothing happens then
     * @param sleepTime the waiting portion until {@link Process#isAlive()} is queried again
     */
    public static void waitAndDestroy(Process proc, int sleepTime) {
        if (null != proc) {
            while (proc.isAlive()) {
                TimeUtils.sleep(sleepTime);
            }
            proc.destroyForcibly();
            while (proc.isAlive()) {
                TimeUtils.sleep(sleepTime);
            }
        }
    }

    /**
     * Preliminary: Stops the service and the background process.
     * 
     * @return the state to transition to, may be <b>null</b> for none
     */
    protected ServiceState stop() {
        if (null != serviceIn) {
            serviceIn.flush();
            serviceIn = null;
        }
        if (null != proc) {
            TimeUtils.sleep(Math.max(0, getWaitTimeBeforeDestroy()));
            if (null != proc) { // may be gone anyway
                proc.destroy();
                waitAndDestroy(proc);
                proc = null;
                osProcess = null;
            }
        }
        return super.stop();
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
        register(redirectIO(err, System.err));
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
     * @throws ExecutionException if process creation fails
     * @see #handleInputStream(InputStream)
     * @see #handleErrorStream(InputStream)
     * @see #handleOutputStream(OutputStream)
     */
    protected Process createAndConfigureProcess(File exe, boolean byName, File dir, List<String> args) 
        throws ExecutionException {
        try {
            proc = createProcess(exe, byName, dir, args, p -> configure(p));
            handleOutputStream(proc.getOutputStream());
            handleInputStream(proc.getInputStream());
            handleErrorStream(proc.getErrorStream());
            attachProcessInformation();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> stop()));
        } catch (IOException e) {
            throw new ExecutionException(e);
        }        
        return proc;
    }
    
    /**
     * Attaches process information if {@link #proc} is available. Does not attach process information twice.
     */
    private void attachProcessInformation() {
        if (null == osProcess) {
            osProcess = ProcessInfoFactory.getInstance().create(proc);
        }
    }
    
    /**
     * Returns the process id of the process implementing the service (if started).
     * 
     * @return the id, may be negative if there is no process or the id cannot be obtained
     */
    public long getPid() {
        return getProcessId(proc); // copes with null
    }
    
    /**
     * Returns the process id of a process just started.
     * 
     * @param proc the process
     * @return the id, may be negative if {@code proc} is <b>null</b> or the id cannot be obtained
     */
    public static long getProcessId(Process proc) {
        return null == proc ? -1 : proc.pid();
    }
    
    /**
     * Allows to configure a process builder for this service.
     * Called from {@link #createAndConfigureProcess(File, boolean, File, List)}
     * 
     * @param builder the process builder to configure
     */
    protected void configure(ProcessBuilder builder) {
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
     * Attaches the metrics provider.
     * 
     * @param provider the metrics provider instance
     */
    public void attachMetricsProvider(MetricsProvider provider) {
        if (null != provider) { // standalone
            Gauge.builder("service." + getId() + ".process.memory.used", 
                 () -> null == osProcess ? 0 : osProcess.getVirtualSize())
                    .description("Used memory of the attached process")
                    .baseUnit(provider.getMemoryBaseUnit().stringValue()).register(provider.getRegistry());
        }
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
