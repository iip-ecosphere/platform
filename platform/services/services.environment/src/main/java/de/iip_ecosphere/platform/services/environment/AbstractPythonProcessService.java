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
import java.util.Set;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;

import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.InTypeInfo;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.OutTypeInfo;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.PythonUtils;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;

/**
 * Generic command-line-based Python integration for multiple data types.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractPythonProcessService extends AbstractRunnablesService implements GenericMultiTypeService {

    public static final char TYPE_SEPARATOR_CHAR = '|';
    
    private File home;
    private List<String> pythonArgs;
    private String locationKey;
    private String transportChannel;
    private GenericMultiTypeServiceImpl impl = new GenericMultiTypeServiceImpl();
    private Map<String, ParameterConfigurer<?>> paramConfigurers = new HashMap<>();
    private Map<String, ReceptionCallback<?>> callbacks = new HashMap<>();
    private String averageResponseTime = "";
    private boolean enableFileDeletion;

    /**
     * Creates a service from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public AbstractPythonProcessService(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
     * Creates a service from YAML information.
     * 
     * @param yaml the service information as read from YAML. By default, the Python executable is 
     * "ServiceEnvironment.py", which can be overridden by {@link YamlProcess#getExecutable()}. 
     * {@link YamlProcess#getHomePath()} is set to the home path where the 
     * executable was extracted to. Further, {@link YamlProcess#getCmdArg()} are taken over if given.
     */
    public AbstractPythonProcessService(YamlService yaml) {
        super(yaml);
    }
    
    @Override
    protected void initializeFrom(YamlService yaml) {
        YamlProcess pSpec = yaml.getProcess();
        pythonArgs = new ArrayList<>();
        if (pSpec != null) {
            locationKey = pSpec.getLocationKey();
            home = pSpec.getHomePath();
            pythonArgs.addAll(pSpec.getExecArg());
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
        transportChannel = yaml.getTransportChannel();
        customizePythonArgs(pythonArgs);
    }
    
    /**
     * Allows to customize the Python command line arguments taken from the yaml spec.
     * 
     * @param pythonArgs the arguments to customize
     */
    protected void customizePythonArgs(List<String> pythonArgs) {
    }
    
    /**
     * Enables or deletes file deletion. By default, Python files are delete upon end of the process.
     * 
     * @param enableFileDeletion enables deletion
     */
    public void enableFileDeletion(boolean enableFileDeletion) {
        this.enableFileDeletion = enableFileDeletion;
    }
    
    /**
     * Returns whether file deletion is enabled. By default, Python files are delete upon end of the process.
     * 
     * @return {@code true} if enabled, {@code false} else
     */
    public boolean isFileDeletionEnabled() {
        return enableFileDeletion;
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
                pos = data.indexOf(TYPE_SEPARATOR_CHAR);
                averageResponseTime = data.substring(0, pos);
                data = data.substring(pos + 1);
                try {
                    if (handler.handle(typeName, data)) {
                        break;
                    }
                } catch (IOException e) {
                    getLogger().error("Error processing {}: {}", line, e.getMessage());
                }
            } else {
                getLogger().error("No type name in result {}", line);
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
     * Ingestor implementation for synchronous processing. Stores received data and returns it.
     * 
     * @param <D> type of data to be ingested
     * @author Holger Eichelberger, SSE
     */
    protected static class SyncDataIngestor<D> implements DataIngestor<D> {

        private BlockingQueue<D> received = new LinkedBlockingQueue<>();
        
        @Override
        public void ingest(D data) {
            received.offer(data);
        }
        
        @Override
        public D waitForResult() throws ExecutionException {
            try {
                return received.take();
            } catch (InterruptedException e) {
                throw new ExecutionException(e);
            }
        }
        
    }
    
    /**
     * Handles a received processing result and ingests it back asynchronously.
     * 
     * @param <O> the data type
     * @param cls the data type class
     * @param data the serialized data
     * @param typeName the data type name as specified in the configuration model
     */
    @SuppressWarnings("unchecked")
    protected <O> void handleResult(Class<O> cls, String data, String typeName) {
        try {
            OutTypeInfo<O> info = (OutTypeInfo<O>) getOutTypeInfo(typeName);
            if (null != info) {
                TypeTranslator<String, O> outT = info.getOutTranslator();
                if (outT != null) {
                    O tmp = outT.to(data);
                    DataIngestor<O> ingestor = info.validateAndGetIngestor(typeName);
                    ingestor.ingest(tmp);
                } else {
                    getLogger().error("No result type translator registered for: {}", typeName);
                }
            }
        } catch (IOException e) {
            getLogger().error("Receiving result: {}", e.getMessage());
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
            ServerAddress netMgtKeyAdr = getNetMgtKeyAddress(); 
            if (null != netMgtKeyAdr) {
                args.add("--netMgtKeyAddress");
                args.add(netMgtKeyAdr.getHost() + ":" + netMgtKeyAdr.getPort()); // preliminary
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
        register(AbstractProcessService.redirectIO(err, System.err));
    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(AbstractPythonProcessService.class);
    }

    @Override
    public <I> void registerInputTypeTranslator(Class<I> inCls, String inTypeName, 
        TypeTranslator<I, String> inTrans) {
        impl.registerInputTypeTranslator(inCls, inTypeName, inTrans);
    }
    
    /**
     * Returns the input type information object for the given symbolic type name.
     * 
     * @param inTypeName the symbolic type name
     * @return the information object or <b>null</b> if none was registered
     */
    public InTypeInfo<?> getInTypeInfo(String inTypeName) {
        return impl.getInTypeInfo(inTypeName);
    }

    /**
     * Returns the output type information object for the given symbolic type name.
     * 
     * @param outTypeName the symbolic type name
     * @return the information object or <b>null</b> if none was registered
     */
    public OutTypeInfo<?> getOutTypeInfo(String outTypeName) {
        return impl.getOutTypeInfo(outTypeName);
    }

    @Override
    public <O> void registerOutputTypeTranslator(Class<O> outCls, String outTypeName, 
        TypeTranslator<String, O> outTrans) {
        impl.registerOutputTypeTranslator(outCls, outTypeName, outTrans);
    }

    @Override
    public <O> void attachIngestor(Class<O> outCls, String outTypeName, DataIngestor<O> ingestor) {
        impl.attachIngestor(outCls, outTypeName, ingestor);
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
    
    @Override
    public ParameterConfigurer<?> getParameterConfigurer(String paramName) {
        return paramConfigurers.get(paramName);
    }
    
    @Override
    public Set<String> getParameterNames() {
        return paramConfigurers.keySet();
    }

    /**
     * Adds parameter configurers via a consumer.
     * 
     * @param paramConsumer the consumer, user 
     *     {@link AbstractService#addConfigurer(Map, String, Class, TypeTranslator, ValueConfigurer)} and related 
     *     methods in there
     */
    public void addParameterConfigurer(Consumer<Map<String, ParameterConfigurer<?>>> paramConsumer) {
        paramConsumer.accept(paramConfigurers); // paramConfigurers may be protected here
    }
    
    @Override
    protected ServiceState start() throws ExecutionException {
        if (null != transportChannel && transportChannel.length() > 0) {
            try {
                getLogger().info(
                    "Establishing clientserver channel for {}, {}: {} ", getId(), getKind(), transportChannel);
                if (ServiceKind.SERVER == getKind()) {
                    establishServerListener("*SERVER", transportChannel);
                } else {
                    establishClientListener("*SERVER", transportChannel);
                }
            } catch (IOException e) {
                getLogger().error(
                    "While establishing client-server channel for {}, {}: {} ", getId(), getKind(), e.getMessage());
            }
        }
        return super.start();
    }
    
    @Override
    protected ServiceState stop() {
        if (!callbacks.isEmpty()) {
            TransportConnector conn = Transport.getConnector();
            if (null != conn) {
                for (Map.Entry<String, ReceptionCallback<?>> c : callbacks.entrySet()) {
                    try {
                        conn.detachReceptionCallback(c.getKey(), c.getValue());
                    } catch (IOException e) {
                        // ignore for now
                    }
                }
            }
            callbacks.clear();
        }
        return super.stop();
    }

    /**
     * Self-registering abstract byte array reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    private abstract static class ByteArrayReceptionCallback implements ReceptionCallback<byte[]> {

        /**
         * Creates a callback instance and registers it in {@code callbacks}.
         * 
         * @param channel the channel name this callback is reacting on
         * @param callbacks the callbacks (for cleanup in {@link AbstractPythonProcessService#stop()}.
         */
        private ByteArrayReceptionCallback(String channel, Map<String, ReceptionCallback<?>> callbacks) {
            callbacks.put(channel, this);
        }
        
        @Override
        public Class<byte[]> getType() {
            return byte[].class;
        }

    }

    /**
     * Establishes a server listener for internal server-client communication via Transport.
     * 
     * @param typeName the name of the type for Java-Python communication
     * @param serverChannel the name of the server channel to send initial request to
     * @throws IOException if sending/receiving messages fails
     */
    private void establishServerListener(String typeName, String serverChannel) throws IOException {
        TransportConnector conn = Transport.createConnector();
        if (null == conn) {
            return; // may happen in testing
        }
        // setup Python->Java channel
        registerInputTypeTranslator(byte[].class, typeName, TypeTranslators.BYTEARRAY_TO_BASE64);
        registerOutputTypeTranslator(byte[].class, typeName, TypeTranslators.BASE64_TO_BYTEARRAY);
        // listen on incoming client requests for private server-client channels
        conn.setReceptionCallback(serverChannel, new ByteArrayReceptionCallback(serverChannel, callbacks) {

            @Override
            public void received(byte[] data) {
                String cChannel = new String(data);
                // respond with server -> client channel
                String cSChannel =  cChannel + "_" + System.currentTimeMillis();
                // if data arrives from Python, pass it on to client
                attachIngestor(byte[].class, typeName, d -> {
                    try {
                        conn.asyncSend(cChannel, data);
                    } catch (IOException e) {
                        getLogger().error("While receiving from Python and passing on to {}", cChannel, e.getMessage());
                    }
                });
                try {
                    // listen on the private client->server channel, pass on data to Python
                    conn.setReceptionCallback(cSChannel, new ByteArrayReceptionCallback(cSChannel, callbacks) {
    
                        @Override
                        public void received(byte[] data) {
                            try {
                                process(typeName, data);
                            } catch (ExecutionException e) {
                                getLogger().error("While receiving on {} and passing on to Python: {}", 
                                    cSChannel, e.getMessage());
                            }
                        }
                        
                    });
                    // sent client connection ack with private client-server channel
                    conn.asyncSend(cChannel, cSChannel); 
                } catch (IOException e) {
                    getLogger().error("While setting up server-client-connection {}-{}", 
                        cChannel, cSChannel, e.getMessage());
                }
            }
            
        });
    }
    
    /**
     * Establishes a client listener for internal server-client communication via Transport.
     * 
     * @param typeName the name of the type for Java-Python communication
     * @param serverChannel the name of the server channel to send initial request to
     * @throws IOException if sending/receiving messages fails
     */
    private void establishClientListener(String typeName, String serverChannel) throws IOException {
        TransportConnector conn = Transport.createConnector();
        if (null == conn) {
            return; // may happen in testing
        }
        // setup Python->Java channel
        registerInputTypeTranslator(byte[].class, typeName, TypeTranslators.BYTEARRAY_TO_BASE64);
        registerOutputTypeTranslator(byte[].class, typeName, TypeTranslators.BASE64_TO_BYTEARRAY);
        // init communication, setup listener on private server-client channel, 
        String clientChannel = getId() + "_client_" + System.currentTimeMillis();
        conn.setReceptionCallback(clientChannel, new ByteArrayReceptionCallback(clientChannel, callbacks) {

            private boolean firstReception = true;
            
            @Override
            public void received(byte[] data) {
                if (firstReception) {
                    // first reception, attach Python->Java ingestor passing on
                    firstReception = false;
                    String serverChannel = new String(data);
                    attachIngestor(byte[].class, typeName, d -> {
                        try {
                            conn.asyncSend(serverChannel, d);
                        } catch (IOException e) {
                            getLogger().error("While receiving from Python passing on to {}", 
                                serverChannel, e.getMessage());
                        }
                    });
                } else {
                    try {
                        process(typeName, data);
                    } catch (ExecutionException e) {
                        getLogger().error("While receiving on {} and passing on to Python: {}", 
                            clientChannel, e.getMessage());
                    }
                }
            }
            
        });
        // request private client-server channel
        conn.asyncSend(serverChannel, clientChannel); 
    }

    /**
     * Returns the average response time for the execution in Python (without transport).
     * 
     * @return the average response time
     */
    public long getAvgResponseTime() {
        try {
            return Long.parseLong(averageResponseTime);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
 
}
