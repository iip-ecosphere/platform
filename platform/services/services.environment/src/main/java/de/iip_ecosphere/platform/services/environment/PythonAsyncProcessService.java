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
import org.slf4j.Logger;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Generic command-line-based Python integration for asynchronous processing of multiple data types. Conventions:
 * <ul>
 *   <li>Python is determined by {@link PythonUtils#getPythonExecutable()}.</li>
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
     * @param yaml the service information as read from YAML. We assume that {@link YamlProcess#getExecutable()} is 
     * set to the Python file to start and {@link YamlProcess#getHomePath()} is set to the home path where the 
     * executable was extracted to. Further, {@link YamlProcess#getCmdArg()} are taken over if given.
     */
    public PythonAsyncProcessService(YamlService yaml) {
        super(yaml);
    }

    /**
     * Preliminary: Starts the service and the background process.
     * 
     * @throws ExecutionException if starting the process fails
     * @see #getPythonExecutable()
     * @see #startExecutableByName()
     */
    protected void start() throws ExecutionException {
        proc = createAndCustomizeProcess(null);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
        serviceIn = new PrintWriter(writer);
        createScanInputThread(proc, (t, d) -> {
            OutTypeInfo<?> info = getOutTypeInfo(t);
            if (null != info) {
                handleResult(info.getType(), d, t);
            } else {
                LoggerFactory.getLogger(getClass()).error("No output type translator registered for: " + t);
            }
            return false;
        }).start();
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
        // let's see, may be generically possible for Python, VAB?
    }

    @Override
    public void update(URI location) throws ExecutionException {
        // let's see, may be generically possible for Python, VAB?
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        // let's see, may be generically possible for Python, VAB?
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        // let's see, may be generically possible for Python, VAB?
    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(PythonAsyncProcessService.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O process(String inType, I data) throws ExecutionException {
        InTypeInfo<?> info = getInTypeInfo(inType);
        if (null != info) {
            TypeTranslator<I, String> inT = (TypeTranslator<I, String>) info.getInTranslator();
            if (null != inT) {
                if (null != serviceIn) {
                    try {
                        serviceIn.println(compose(inType, inT.to(data)));
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
        } else {
            throw new ExecutionException("No input type translator registered", null);
        }
        return null;
    }

}
