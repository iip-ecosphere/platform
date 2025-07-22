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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.InTypeInfo;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.OutTypeInfo;
import de.iip_ecosphere.platform.support.PythonUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Generic command-line-based Python integration for asynchronous processing of multiple data types. Conventions:
 * <ul>
 *   <li>Python is determined by {@link PythonUtils#getPythonExecutable()}. The default is "ServiceEnvironment.py" 
 *       which must run for this integration with "--mode console".</li>
 *   <li>The Python program runs endless until stopped by this class.</li>
 *   <li>An asynchronous Python program receives the data via command line input streams based on the input serializer 
 *       and the symbolic type name.</li>
 * </ul>
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonAsyncProcessService extends AbstractPythonProcessService {

    private Process proc;
    private PrintWriter serviceIn;
    private Map<String, String> reconfValues;
 
    /**
     * Creates an instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public PythonAsyncProcessService(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
     * Creates an abstract service from YAML information.
     * 
     * @param yaml the service information as read from YAML. By default, the Python executable is 
     *     "ServiceEnvironment.py", which can be overridden by {@link YamlProcess#getExecutable()}. 
     *     {@link YamlProcess#getHomePath()} is set to the home path where the 
     *     executable was extracted to. Further, {@link YamlProcess#getCmdArg()} are taken over if given.
     */
    public PythonAsyncProcessService(YamlService yaml) {
        super(yaml);
    }
    
    @Override
    protected ServiceState start() throws ExecutionException {
        super.start();
        proc = createAndCustomizeProcess(null, null);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        serviceIn = new PrintWriter(writer);
        createScanInputThread(proc);
        if (null != reconfValues) {
            sendToService("*recfg", toJson(reconfValues));
            reconfValues = null;
        }
        return ServiceState.RUNNING;
    }
    
    /**
     * Creates the input scanning thread.
     * 
     * @param proc the process instance to observe
     */
    protected void createScanInputThread(Process proc) {
        createScanInputThread(proc, (t, d) -> {
            OutTypeInfo<?> info = getOutTypeInfo(t);
            if (null != info) {
                handleResult(info.getType(), d, t);
            } else {
                getLogger().error("No output type translator registered for: {}", t);
            }
            return false;
        }).start();
    }

    @Override
    protected ServiceState stop() {
        if (null != serviceIn) {
            serviceIn.flush();
            serviceIn = null;
        }
        if (null != proc) {
            proc.destroyForcibly();
            while (null != proc && proc.isAlive()) {
                TimeUtils.sleep(200);
            }
            proc = null;
        }
        if (null != getHome() && isFileDeletionEnabled()) {
            try {
                FileUtils.forceDelete(getHome());
            } catch (IOException e) {
                getLogger().error("Cannot delete Python process home {}: {}", getHome(), e.getMessage());
            }
        }
        super.stop();
        return ServiceState.STOPPED;
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        if (ServiceState.STOPPING == state) { // otherwise it's gone
            sendToService("*setstate", state.name());
        }
        super.setState(state);
        if (state != ServiceState.STOPPING) { // otherwise it's not yet there
            sendToService("*setstate", state.name());
        }
        ServiceState st = getState();
        if (st != state) { // for completeness
            sendToService("*setstate", st.name());
        }
    }
    
    @Override
    public void migrate(String resourceId) throws ExecutionException {
        sendToService("*migrate", resourceId);
    }

    @Override
    public void update(URI location) throws ExecutionException {
        sendToService("*update", location.toString());
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        sendToService("*switch", targetId);
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        if (ServiceState.RUNNING == getState()) {
            sendToService("*recfg", toJson(values));
        } else {
            if (null == reconfValues) {
                reconfValues = new HashMap<>();
            }
            reconfValues.putAll(values); // overwrite existing
        }
        super.reconfigure(values);
    }
    
    @Override
    public void activate() throws ExecutionException {
        if (getState() == ServiceState.PASSIVATED) {
            sendToService("*activate", "");
        }
        super.activate(); // TODO access to state -> Python
    }

    @Override
    public void passivate() throws ExecutionException {
        if (getState() == ServiceState.RUNNING) {
            sendToService("*passivate", "");
        }
        super.passivate(); // TODO access to state -> Python
    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(PythonAsyncProcessService.class);
    }

    /**
     * Sends {@code text} as input to the service process.
     * 
     * @param type the type of the data to be sent
     * @param data the data to be sent
     * @throws ExecutionException if sending fails for some reason
     */
    protected void sendToService(String type, Object data) throws ExecutionException {
        PrintWriter si = serviceIn; // may be gone between println and flush
        if (null != si) {
            si.println(compose(type, data.toString()));
            si.flush();
        } // ignore, this may be a deactivated service that shall not be operating
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I> void process(String inType, I data) throws ExecutionException {
        InTypeInfo<?> info = getInTypeInfo(inType);
        if (null != info) {
            TypeTranslator<I, String> inT = (TypeTranslator<I, String>) info.getInTranslator();
            if (null != inT) {
                try {
                    sendToService(inType, inT.to(data));
                } catch (IOException e) {
                    throw new ExecutionException("Cannot transfer data to service: " + e.getMessage(), e);
                }
            } else {
                throw new ExecutionException("No input type translator registered", null);
            }
        } else {
            throw new ExecutionException("No input type translator registered", null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O processSync(String inTypeName, I data, String outTypeName) throws ExecutionException {
        process(inTypeName, data);
        OutTypeInfo<O> outInfo = (OutTypeInfo<O>) getOutTypeInfo(outTypeName);
        DataIngestor<O> ingestor = outInfo.validateAndGetIngestor(outTypeName);
        return ingestor.waitForResult();
    }

}
