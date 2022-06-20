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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Generic command-line-based Python integration for multiple data types.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractPythonProcessService extends AbstractService implements GenericMultiTypeService {

    public static final char TYPE_SEPARATOR_CHAR = '|';
    
    private File home;
    private List<String> pythonArgs;
    private String locationKey;
    private Map<String, OutTypeInfo<?>> outTypeInfos = new HashMap<>();
    private Map<String, InTypeInfo<?>> inTypeInfos = new HashMap<>();
    
    /**
     * Represents an input or output type.
     * 
     * @param <T> the Java representation of the output type
     * 
     * @author Holger Eichelberger, SSE
     */
    protected abstract static class AbstractTypeInfo<T> {
        
        private Class<T> type;
        
        /**
         * Creates an instance.
         * 
         * @param type the class representing the data type
         */
        protected AbstractTypeInfo(Class<T> type) {
            this.type = type;
        }
       
        /**
         * Returns the Java representation of the type.
         * 
         * @return the type
         */
        protected Class<T> getType() {
            return type;
        }
        
    }

    /**
     * Represents an input type.
     * 
     * @param <T> the Java representation of the output type
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class InTypeInfo <T> extends AbstractTypeInfo<T> {
        
        private TypeTranslator<T, String> inTranslator;

        /**
         * Creates an instance.
         * 
         * @param type the class representing the data type
         */
        protected InTypeInfo(Class<T> type) {
            super(type);
        }
        
        /**
         * Returns the input translator.
         * 
         * @return the type translator, may be <b>null</b>
         */
        protected TypeTranslator<T, String> getInTranslator() {
            return inTranslator;
        }
        
    }

    /**
     * Represents an output type.
     * 
     * @param <T> the Java representation of the output type
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class OutTypeInfo <T> extends AbstractTypeInfo<T> {
        
        private TypeTranslator<String, T> outTranslator;
        private DataIngestor<T> ingestor;

        /**
         * Creates an instance.
         * 
         * @param type the class representing the data type
         */
        protected OutTypeInfo(Class<T> type) {
            super(type);
        }

        /**
         * Returns the output translator.
         * 
         * @return the type translator, may be <b>null</b>
         */
        protected TypeTranslator<String, T> getOutTranslator() {
            return outTranslator;
        }

    }

    /**
     * Creates an abstract service from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public AbstractPythonProcessService(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
     * Creates an abstract service from YAML information.
     * 
     * @param yaml the service information as read from YAML. By default, the Python executable is 
     * "ServiceEnvironment.py", which can be overridden by {@link YamlProcess#getExecutable()}. 
     * {@link YamlProcess#getHomePath()} is set to the home path where the 
     * executable was extracted to. Further, {@link YamlProcess#getCmdArg()} are taken over if given.
     */
    public AbstractPythonProcessService(YamlService yaml) {
        super(yaml);
    }
    
    /**
     * Does further setup of this instance from the given YAML information.
     * 
     * @param yaml the service information as read from YAML
     */
    @Override
    protected void initializeFrom(YamlService yaml) {
        YamlProcess pSpec = yaml.getProcess();
        pythonArgs = new ArrayList<>();
        if (pSpec != null) {
            locationKey = pSpec.getLocationKey();
            home = pSpec.getHomePath();
            pythonArgs.add(getPythonModule(pSpec.getExecutable(), yaml, home));
            List<String> cmdArg = pSpec.getSubstCmdArg();
            if (null != cmdArg) {
                pythonArgs.addAll(cmdArg);
            }
        } else {
            pythonArgs.add(getPythonModule(null, yaml, null));
        }
        if (null == home) { // shall not occur
            getLogger().warn("No home path given for service " + yaml.getId() + ". Falling back to temporary folder");
            home = FileUtils.createTmpFolder(FileUtils.sanitizeFileName(yaml.getId(), true));
        }
    }
    
    /**
     * Returns the home directory.
     * 
     * @return the home directory
     */
    protected File getHome() {
        return home;
    }
    
    /**
     * Returns the name of the Python module. The default (if not explicitly given) is "ServiceEnvironment.py".
     * 
     * @param module the module name, may be empty or <b>null</b>
     * @param yaml the YAML service deployment information
     * @param homePath optional home path to check for the module first if not given
     * @return the Python module name
     */
    private static String getPythonModule(String module, YamlService yaml, File homePath) {
        String result = module;
        if (null == result || result.length() == 0) {
            if (homePath != null) {
                result = "ServiceEnvironment.py"; // testing
                File f = new File(homePath, "iip/ServiceEnvironment.py"); // the "official" one
                if (f.exists()) {
                    result = "iip/ServiceEnvironment.py";
                }
            }
        }
        return result;
    }
    
    /**
     * Returns whether the Python executable shall be started by name or by full path.
     * 
     * @return {@code false} for starting by full path returned by {@link #getPythonExecutable()}
     */
    protected boolean startExecutableByName() {
        return true;
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        if (ServiceState.START_SERVICE == ServiceState.RUNNING) { // base implementation, STARTING fails Linux/Jenkins
            ServiceState next = null;
            super.setState(state);
            switch (state) {
            case STARTING:
                next = start();
                break;
            case STOPPING:
                next = stop();
                break;
            default:
                break;
            }
            if (null != next) {
                super.setState(next);
            }
        } else {
            super.setState(state);
        }
    }
       
    /**
     * Defines a function that handles "parsed" input, split into type and serialized data.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected interface InputHandler {

        /**
         * Handles input.
         * 
         * @param type the type
         * @param data the data
         * @return {@code true} for continue reading lines, {@code false} else
         * @throws IOException in case that data cannot be translated/processed
         */
        public boolean handle(String type, String data) throws IOException;
        
    }
    
    /**
     * Scans the input stream of the given process for return data.
     * 
     * @param proc the Python process
     * @param handler the input handler
     * @return the first line if {@code returnFirst}, else <b>null</b>
     * @see #handleResult(Class, String, String) 
     */
    protected String scanInputStream(Process proc, InputHandler handler) {
        String result = null;
        Scanner sc = new Scanner(proc.getInputStream());
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            int pos = line.indexOf(TYPE_SEPARATOR_CHAR);
            if (pos > 0 && pos < line.length()) {
                String typeName = line.substring(0, pos);
                String data = line.substring(pos + 1);
                try {
                    if (handler.handle(typeName, data)) {
                        break;
                    }
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Error processing " + line + ": " + e.getMessage());
                }
            } else {
                LoggerFactory.getLogger(getClass()).error("No type name in result " + line);
            }
        }
        sc.close();
        return result;
    }

    /**
     * Creates a thread calling {@link #scanInputStream(Process, InputHandler)}.
     * 
     * @param proc the Python process
     * @param handler the input handler
     * @return the thread
     */
    protected Thread createScanInputThread(Process proc, InputHandler handler) {
        Thread result = new Thread(new Runnable() {
            
            @Override
            public void run() {
                scanInputStream(proc, handler);
            }
            
        });
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
    protected <O> void handleResult(Class<O> cls, String data, String typeName) {
        try {
            OutTypeInfo<O> info = (OutTypeInfo<O>) outTypeInfos.get(typeName);
            if (null != info) {
                TypeTranslator<String, O> outT = info.outTranslator;
                if (outT != null) {
                    O tmp = outT.to(data);
                    if (null != info.ingestor) {
                        info.ingestor.ingest(tmp);
                    } else {
                        LoggerFactory.getLogger(getClass()).error("No ingestor registered for: " + typeName);
                    }
                } else {
                    LoggerFactory.getLogger(getClass()).error("No result type translator registered for: " + typeName);
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Receiving result: " + e.getMessage());
        }
    }
    
    /**
     * Turns a map of string values into JSON.
     * 
     * @param reconfValues the values to turn into JSON
     * @return the JSON representation
     */
    protected String toJson(Map<String, String> reconfValues) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(reconfValues);
        } catch (JsonProcessingException e) {
            getLogger().error("Translating " + reconfValues + " to JSON failed: " + e.getMessage());
            return "{}";
        }
    }

    /**
     * Creates and customizes the process.
     * 
     * @param data the data to be directly handed to the process via command line
     * @param reconfValues values to (re)configure the service environment, may be <b>null</b> for none
     * @return the process
     * @throws ExecutionException if the process cannot be created
     */
    protected Process createAndCustomizeProcess(String data, Map<String, String> reconfValues) 
        throws ExecutionException {
        try {
            List<String> args = new ArrayList<String>();
            if (null != pythonArgs) { // if not completely intialized as service description is missing
                args.addAll(pythonArgs);
            }
            if (null != reconfValues && reconfValues.size() > 0) {
                args.add("--configure");
                args.add(toJson(reconfValues));
            }
            if (null != data) {
                args.add("--data");
                args.add(org.apache.commons.text.StringEscapeUtils.escapeJava(data)); // quote quotes -> JSON
            } 
            Process proc = AbstractProcessService.createProcess(getPythonExecutable(), 
                startExecutableByName(), home, args);
            handleErrorStream(proc.getErrorStream());
            return proc;
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }
    
    /**
     * Returns the location key for access into {@link InstalledDependenciesSetup}.
     * 
     * @return the key, may be <b>null</b> for fallback
     */
    protected String getLocationKey() { // could be abstracted into parent class
        return locationKey;
    }

    /**
     * Returns the Python executable, either via {@link InstalledDependenciesSetup} and {@link #getLocationKey()}
     * or as fallback via {@link PythonUtils#getPythonExecutable()}.
     * 
     * @return the Python executable
     */
    protected File getPythonExecutable() {
        File result = null;
        String key = getLocationKey();
        if (null != key) {
            result = InstalledDependenciesSetup.getInstance().getLocation(key);
        }
        if (null == result) { // fallback
            result = PythonUtils.getPythonExecutable();
        }
        return result;
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
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(AbstractPythonProcessService.class);
    }

    /**
     * Adds an input type translator.
     *  
     * @param <I> the input data type
     * @param inCls the class representing the input type
     * @param inTypeName symbolic name of {@code inCls}, e.g. from configuration model
     * @param inTrans the input data type translator
     * @see #registerOutputTypeTranslator(Class, String, TypeTranslator)
     */
    @Override
    public <I> void registerInputTypeTranslator(Class<I> inCls, String inTypeName, 
        TypeTranslator<I, String> inTrans) {
        InTypeInfo<I> info = obtainInTypeInfo(inCls, inTypeName);
        info.inTranslator = inTrans;
    }
    
    /**
     * Returns the input type information object for the given symbolic type name.
     * 
     * @param inTypeName the symbolic type name
     * @return the information object or <b>null</b> if none was registered
     */
    public InTypeInfo<?> getInTypeInfo(String inTypeName) {
        return inTypeInfos.get(inTypeName);
    }

    /**
     * Obtains an input type information object.
     * 
     * @param <I> the input type
     * @param cls the class representing the type
     * @param typeName the associated symbolic type name
     * @return the input type information object, may be retrieved or new
     */
    @SuppressWarnings("unchecked")
    private <I> InTypeInfo<I> obtainInTypeInfo(Class<I> cls, String typeName) {
        InTypeInfo<I> info = (InTypeInfo<I>) inTypeInfos.get(typeName);
        if (null == info) {
            info = new InTypeInfo<I>(cls);
            inTypeInfos.put(typeName, info);
        }
        return info;
    }

    /**
     * Returns the output type information object for the given symbolic type name.
     * 
     * @param outTypeName the symbolic type name
     * @return the information object or <b>null</b> if none was registered
     */
    public OutTypeInfo<?> getOutTypeInfo(String outTypeName) {
        return outTypeInfos.get(outTypeName);
    }

    /**
     * Obtains an output type information object.
     * 
     * @param <O> the output type
     * @param cls the class representing the type
     * @param typeName the associated symbolic type name
     * @return the output type information object, may be retrieved or new
     */
    @SuppressWarnings("unchecked")
    private <O> OutTypeInfo<O> obtainOutTypeInfo(Class<O> cls, String typeName) {
        OutTypeInfo<O> info = (OutTypeInfo<O>) outTypeInfos.get(typeName);
        if (null == info) {
            info = new OutTypeInfo<O>(cls);
            outTypeInfos.put(typeName, info);
        }
        return info;
    }

    /**
     * Adds an output type translator.
     *  
     * @param <O> the output data type
     * @param outCls the class representing the input type
     * @param outTypeName symbolic name of {@code outCls}, e.g. from configuration model
     * @param outTrans the output data type translator
     * @see #registerInputTypeTranslator(Class, String, TypeTranslator)
     */
    @Override
    public <O> void registerOutputTypeTranslator(Class<O> outCls, String outTypeName, 
        TypeTranslator<String, O> outTrans) {
        OutTypeInfo<O> info = obtainOutTypeInfo(outCls, outTypeName);
        info.outTranslator = outTrans;
    }

    @Override
    public <O> void attachIngestor(Class<O> outCls, String outTypeName, DataIngestor<O> ingestor) {
        OutTypeInfo<O> info = (OutTypeInfo<O>) obtainOutTypeInfo(outCls, outTypeName);
        info.ingestor = ingestor;
    }
    
    /**
     * Composes a symbolic type name with the string-serialized data.
     * 
     * @param typeName the type name
     * @param data the data
     * @return the composed String
     */
    protected static String compose(String typeName, String data) {
        return typeName + TYPE_SEPARATOR_CHAR + data;
    }
    
}
