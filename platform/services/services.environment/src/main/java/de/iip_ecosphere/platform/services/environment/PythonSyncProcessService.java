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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.InTypeInfo;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.OutTypeInfo;
import de.iip_ecosphere.platform.support.PythonUtils;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Generic command-line-based Python integration for multiple data types. Conventions:
 * <ul>
 *   <li>Python is determined by {@link PythonUtils#getPythonExecutable()}. The default is "ServiceEnvironment.py" 
 *       which must run for this integration with "--mode console".</li>
 *   <li>A synchronous Python program receives the data including the symbolic type name as last (for 
 *       "ServiceEnvironment.py" qualified by "--data") command line argument 
 *       and returns the result on the command line including the symbolic type name.</li>
 *   <li>The Python program runs until the input data is processed.</li>
 * </ul>
 * 
 * @author Holger Eichelberger, SSE
 */
public class PythonSyncProcessService extends AbstractPythonProcessService {

    private int timeout = 1;
    private TimeUnit timeoutUnit = TimeUnit.SECONDS;
    private Map<String, String> reconfValues;

    /**
     * Creates an instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public PythonSyncProcessService(String serviceId, InputStream ymlFile) {
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
    public PythonSyncProcessService(YamlService yaml) {
        super(yaml);
    }
    
    @Override
    public void migrate(String resourceId) throws ExecutionException {
        // do within Java
    }

    @Override
    public void update(URI location) throws ExecutionException {
        // do within Java
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        // do within Java
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        if (null == reconfValues) {
            reconfValues = new HashMap<>();
        }
        reconfValues.putAll(values); // overwrite existing
        super.reconfigure(values);
    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(PythonSyncProcessService.class);
    }

    /**
     * Requests processing a data item.
     * 
     * @param <I> the input data type
     * @param <O> the output data type
     * @param inType the name of {@code inType} in the configuration model
     * @param data the data item to be processed
     * @return the output, always <b>null</b> in case of asynchronous processing as the result is passed to a 
     *     registered ingestor
     * @throws ExecutionException if the execution fails for some reason, e.g., because type translators 
     *    are not registered (@link #registerInputTypeTranslator(Class, Class, TypeTranslator, TypeTranslator)}
     */
    @SuppressWarnings("unchecked")
    private <I, O> O processImpl(String inType, I data) throws ExecutionException {
        O result = null;
        InTypeInfo<?> info = getInTypeInfo(inType);
        if (null != info) {
            TypeTranslator<I, String> inT = (TypeTranslator<I, String>) info.getInTranslator();
            if (null != inT) {
                try {
                    AtomicReference<O> tmp = new AtomicReference<O>();
                    Process proc = createAndCustomizeProcess(compose(inType, inT.to(data)), reconfValues);
                    Thread thread = createScanInputThread(proc, (t, d) -> {
                        OutTypeInfo<?> oInfo = getOutTypeInfo(t);
                        if (null != oInfo) {
                            TypeTranslator<String, O> outT = (TypeTranslator<String, O>) oInfo.getOutTranslator();
                            if (null != outT) {
                                tmp.set(outT.to(d));
                            } else {
                                throw new IOException("No output type translator registered");
                            }
                        }
                        return true;
                    });
                    thread.start();
                    if (timeout < 0) {
                        proc.waitFor();
                    } else {
                        proc.waitFor(timeout, timeoutUnit);
                    }
                    thread.join();
                    
                    result = tmp.get();
                } catch (InterruptedException | IOException e) {
                    throw new ExecutionException("Exception while data processing: " + e.getMessage(), e);
                }
            } else {
                throw new ExecutionException("No input type translator registered", null);
            }
        }
        return result;
    }

    @Override
    public <I> void process(String inType, I data) throws ExecutionException {
        processImpl(inType, data);
    }
    
    @Override
    public <I, O> O processSync(String inType, I data, String outType) throws ExecutionException {
        return processImpl(inType, data);
    }

}
