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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Generic command-line-based Python integration for multiple data types. Conventions:
 * <ul>
 *   <li>Python is determined by {@link  
 *   <li>A synchronous Python program receives the data as last command line</li>
 * </ul>
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonProcessService extends AbstractService implements GenericService {

    private Process proc;
    private File home;
    private List<String> pythonArgs = new ArrayList<>();
    private Map<Class<?>, TypeTranslator<?, String>> inTranslators = new HashMap<>();
    private Map<Class<?>, TypeTranslator<String, ?>> outTranslators = new HashMap<>();
    private Map<String, Class<?>> outTranslatorsClasses = new HashMap<>();
    private Map<Class<?>, DataIngestor<?>> ingestors = new HashMap<>();
    private int timeout = 1;
    private TimeUnit timeoutUnit = TimeUnit.SECONDS;
    private PrintWriter serviceIn;
    
    /**
     * Creates an abstract service from YAML information.
     * 
     * @param yaml the service information as read from YAML. We assume that {@link YamlProcess#getExecutable()} is 
     * set to the Python file to start and {@link YamlProcess#getHomePath()} is set to the home path where the 
     * executable was extracted to. Further, {@link YamlProcess#getCmdArg()} are taken over if given.
     */
    public PythonProcessService(YamlService yaml) {
        super(yaml);
        YamlProcess pSpec = yaml.getProcess();
        if (pSpec != null) {
            home = pSpec.getHomePath();
            pythonArgs.add(getPythonModule(pSpec.getExecutable(), yaml));
            if (null != pSpec.getCmdArg()) {
                pythonArgs.addAll(pSpec.getCmdArg());
            }
        } else {
            pythonArgs.add(getPythonModule(null, yaml));
        }
        if (null == home) { // shall not occur
            getLogger().warn("No home path given for service " + yaml.getId() + ". Falling back to temporary folder");
            home = FileUtils.createTmpFolder(AbstractProcessService.sanitizeFileName(yaml.getId(), true));
        }
    }
    
    /**
     * Returns the name of the Python module if given or returns a (non-existent) fallback and emits a warning.
     * 
     * @param module the module name, may be empty or <b>null</b>
     * @param yaml the YAML service deployment information
     * @return the Python module name
     */
    private static String getPythonModule(String module, YamlService yaml) {
        String result = module;
        if (null == result || result.length() == 0) {
            result = "default.py";
            getLogger().warn("No Python module given as execuable for Service '" + yaml.getId() 
                + "'. Falling back to module name that probably does not exist.");    
        }
        return result;
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
     * Returns whether the Python executable shall be started by name or by full path.
     * 
     * @return {@code false} for starting by full path returned by {@link #getPythonExecutable()}
     */
    protected boolean startExecutableByName() {
        return true;
    }
    
    /**
     * Preliminary: Starts the service and the background process.
     * 
     * @throws ExecutionException if starting the process fails
     * @see #getPythonExecutable()
     * @see #startExecutableByName()
     */
    protected void start() throws ExecutionException {
        if (ingestors.size() > 0) {
            proc = createAndCustomizeProcess(null);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
            serviceIn = new PrintWriter(writer);

            new Thread(new Runnable() {
                
                @Override
                public void run() {
                    scanInputStream(proc, false);
                }
                
            }).start();
        }
    }
    
    /**
     * Scans the input stream of the given process for return data.
     * 
     * @param proc the process
     * @param returnFirst whether a process for an ingestor shall be started or whether 
     * @return the first line if {@code returnFirst}, else <b>null</b>
     * @see #handleResult(Class, String, String) 
     */
    private String scanInputStream(Process proc, boolean returnFirst) {
        String result = null;
        Scanner sc = new Scanner(proc.getInputStream());
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (returnFirst) {
                int pos = line.indexOf('|');
                if (pos > 0 && pos < line.length()) {
                    result = line.substring(pos + 1);
                }
                break;
            } else {
                int pos = line.indexOf('|');
                if (pos > 0 && pos < line.length()) {
                    String typeName = line.substring(0, pos);
                    String data = line.substring(pos + 1);
                    Class<?> outCls = outTranslatorsClasses.get(typeName);
                    if (null != outCls) {
                        handleResult(outCls, data, typeName);
                    } else {
                        LoggerFactory.getLogger(getClass()).error("No output type translator registered for: " 
                            + typeName);
                    }
                } else {
                    LoggerFactory.getLogger(getClass()).error("No type name in result: " + line);
                }
            }
        }
        sc.close();
        return result;
    }
    
    /**
     * Handles a received processing result and ingests it back asynchronously.
     * 
     * @param <O> the data type
     * @param cls the data type class
     * @param data the serialized data
     * @param typeName the data type name
     */
    @SuppressWarnings("unchecked")
    private <O> void handleResult(Class<O> cls, String data, String typeName) {
        try {
            TypeTranslator<String, O> outT = (TypeTranslator<String, O>) outTranslators.get(cls);
            if (outT != null) {
                O tmp = outT.to(data);
                DataIngestor<O> ingestor = (DataIngestor<O>) ingestors.get(cls);
                if (null != ingestor) {
                    ingestor.ingest(tmp);
                } else {
                    LoggerFactory.getLogger(getClass()).error("No ingestor registered for: " + typeName);
                }
            } else {
                LoggerFactory.getLogger(getClass()).error("No result type translator registered for: " + typeName);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Receiving result: " + e.getMessage());
        }
    }

    /**
     * Creates and customizes the process.
     * 
     * @param data the data to be directly handed to the process via command line
     * @return the process
     * @throws ExecutionException if the process cannot be created
     */
    protected Process createAndCustomizeProcess(String data) throws ExecutionException {
        try {
            List<String> args;
            if (data != null) {
                args = new ArrayList<String>(pythonArgs);
                args.add(data);
            } else {
                args = pythonArgs;
            }
            Process proc = AbstractProcessService.createProcess(PythonUtils.getPythonExecutable(), 
                startExecutableByName(), home, args);
            handleErrorStream(proc.getErrorStream());
            return proc;
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }
    
    /**
     * Handles the error stream upon process creation.
     * 
     * @param err the process error stream
     */
    protected void handleErrorStream(InputStream err) {
        AbstractProcessService.redirectIO(err, System.err);
    }

    /**
     * Preliminary: Stops the service and the background process.
     */
    protected void stop() {
        if (null != serviceIn) {
            serviceIn.flush();
            serviceIn = null;
        }
        if (null != proc) {
            proc.destroy();
            proc = null;
        }
    }
    
    @Override
    public void migrate(String resourceId) throws ExecutionException {
        throw new ExecutionException("Not implemented", null); // let's see, may be generically possible for Python
    }

    @Override
    public void update(URI location) throws ExecutionException {
        throw new ExecutionException("Not implemented", null); // let's see, may be generically possible for Python
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        throw new ExecutionException("Not implemented", null); // let's see, may be generically possible for Python
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        throw new ExecutionException("Not implemented", null); // let's see, may be generically possible for Python
    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(PythonProcessService.class);
    }

    @Override
    public <I, O> void registerTypeTranslators(Class<I> inCls, Class<O> outCls, String outName, 
        TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans) {
        inTranslators.put(inCls, inTrans);
        outTranslators.put(outCls, outTrans);
        outTranslatorsClasses.put(outName, outCls);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I> void processAsync(Class<I> inCls, String inType, I data) throws ExecutionException {
        TypeTranslator<I, String> inT = (TypeTranslator<I, String>) inTranslators.get(inCls);
        if (null != inT) {
            if (null != serviceIn) {
                try {
                    serviceIn.println(inType + "|" + inT.to(data));
                    serviceIn.flush();
                } catch (IOException e) {
                    throw new ExecutionException("Cannot transfer data to service: " + e.getMessage(), e);
                }
            } else {
                throw new ExecutionException("Service/process not started,", null);
            }
        } else {
            throw new ExecutionException("No input type translator registered", null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O processSync(Class<I> inCls, String inType, Class<O> outCls, I data) throws ExecutionException {
        O result = null;
        TypeTranslator<I, String> inT = (TypeTranslator<I, String>) inTranslators.get(inCls);
        if (null != inT) {
            try {
                AtomicReference<String> tmp = new AtomicReference<String>();
                proc = createAndCustomizeProcess(inType + "|" + inT.to(data));
                new Thread(new Runnable() {
                    
                    @Override
                    public void run() {
                        tmp.set(scanInputStream(proc, true));
                    }
                    
                }).start();                
                if (timeout < 0) {
                    proc.waitFor();
                } else {
                    proc.waitFor(timeout, timeoutUnit);
                }
                TypeTranslator<String, O> outT = (TypeTranslator<String, O>) outTranslators.get(outCls);
                if (null != outT) {
                    result = outT.to(tmp.get());
                } else {
                    throw new ExecutionException("No output type translator registered", null);
                }
            } catch (InterruptedException | IOException e) {
                throw new ExecutionException("Exception while data processing: " + e.getMessage(), e);
            }
        } else {
            throw new ExecutionException("No input type translator registered", null);
        }
        return result;
    }

    @Override
    public <D> void attachIngestor(Class<D> cls, DataIngestor<D> ingestor) {
        ingestors.put(cls, ingestor);
    }

}
