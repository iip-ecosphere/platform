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
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import de.iip_ecosphere.platform.support.TimeUtils;
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
    private boolean enableFileDeletion;
 
    /**
     * Creates an instace from a service id and a YAML artifact.
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
    
    /**
     * Enables or deletes file deletion. By default, Python files are delete upon end of the process.
     * 
     * @param enableFileDeletion enables deletion
     */
    public void enableFileDeletion(boolean enableFileDeletion) {
        this.enableFileDeletion = enableFileDeletion;
    }

    @Override
    protected ServiceState start() throws ExecutionException {
        proc = createAndCustomizeProcess(null, null);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        serviceIn = new PrintWriter(writer);
        createScanInputThread(proc, (t, d) -> {
            OutTypeInfo<?> info = getOutTypeInfo(t);
            if (null != info) {
                handleResult(info.getType(), d, t);
            } else {
                getLogger().error("No output type translator registered for: {}", t);
            }
            return false;
        }).start();
        return ServiceState.RUNNING;
    }

    @Override
    protected ServiceState stop() {
        if (null != serviceIn) {
            serviceIn.flush();
            serviceIn = null;
        }
        if (null != proc) {
            proc.destroyForcibly();
            while (proc.isAlive()) {
                TimeUtils.sleep(200);
            }
            proc = null;
        }
        if (null != getHome() && enableFileDeletion) {
            try {
                FileUtils.forceDelete(getHome());
            } catch (IOException e) {
                getLogger().error("Cannot delete Python process home {}: {}", getHome(), e.getMessage());
            }
        }
        return ServiceState.STOPPED;
    }
    
    @Override
    public void migrate(String resourceId) throws ExecutionException {
        sendToService(compose("*migrate", resourceId));
    }

    @Override
    public void update(URI location) throws ExecutionException {
        sendToService(compose("*update", location.toString()));
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        sendToService(compose("*switch", targetId));
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        sendToService(compose("*recfg", toJson(values)));
    }
    
    @Override
    public void activate() throws ExecutionException {
        if (getState() == ServiceState.PASSIVATED) {
            sendToService(compose("*activate", ""));
        }
        super.activate(); // TODO access to state -> Python
    }

    @Override
    public void passivate() throws ExecutionException {
        if (getState() == ServiceState.RUNNING) {
            sendToService(compose("*passivate", ""));
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
     * @param text the text to be sent
     * @throws ExecutionException if sending fails for some reason
     */
    private void sendToService(String text) throws ExecutionException {
        if (null != serviceIn) {
            serviceIn.println(text);
            serviceIn.flush();
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
                    sendToService(compose(inType, inT.to(data)));
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
