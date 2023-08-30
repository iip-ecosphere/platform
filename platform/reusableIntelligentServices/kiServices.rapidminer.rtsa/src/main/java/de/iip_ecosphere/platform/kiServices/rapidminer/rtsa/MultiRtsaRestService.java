/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeService;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.InTypeInfo;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.OutTypeInfo;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;

/**
 * Multi-type RTSA service.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MultiRtsaRestService implements GenericMultiTypeService {

    /**
     * Extended RTSA service for multi-type queries.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class ExRtsaRestService extends RtsaRestService<String, String> {

        /**
         * Creates an instance.
         * 
         * @param yaml the string information as YAML
         */
        public ExRtsaRestService(YamlService yaml) {
            super(TypeTranslators.STRING, TypeTranslators.STRING, null, yaml);
        }
        
        @Override
        protected String adjustRestQuery(String input, String inTypeName) {
            return "{\"" + inTypeName + "\":[" + input + "]}";
        }
        
        @Override
        protected void handleReception(String data) {
            if (data.startsWith("{\"") && data.endsWith("]}")) {
                data = data.substring(2, data.length() - 2);
                int pos = data.indexOf("\"");
                if (pos > 0) {
                    String typeName = data.substring(0, pos);
                    data = data.substring(pos + 1);
                    if (data.startsWith(":[")) {
                        data = data.substring(2);
                        OutTypeInfo<?> info = impl.getOutTypeInfo(typeName);
                        if (null != info) {
                            handleResult(info.getType(), data, typeName);
                        } else {
                            getLogger().error("No output type translator registered for: {}", typeName);
                        }
                    }
                }
            }
        }
        
    }
    
    private GenericMultiTypeServiceImpl impl = new GenericMultiTypeServiceImpl();
    private ExRtsaRestService service;
    
    /**
     * Creates an instance.
     * 
     * @param yaml the service description YAML
     */
    public MultiRtsaRestService(YamlService yaml) {
        service = createService(yaml);
    }

    /**
     * Creates the nested service instance to delegate to.
     * 
     * @param yaml the service description YAML
     * @return the service instance
     */
    protected ExRtsaRestService createService(YamlService yaml) {
        return new ExRtsaRestService(yaml);
    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(MultiRtsaRestService.class);
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
            OutTypeInfo<O> info = (OutTypeInfo<O>) impl.getOutTypeInfo(typeName);
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

    @Override
    public <I> void registerInputTypeTranslator(Class<I> inCls, String inTypeName, TypeTranslator<I, String> inTrans) {
        impl.registerInputTypeTranslator(inCls, inTypeName, inTrans);
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

    @SuppressWarnings("unchecked")
    @Override
    public <I> void process(String inType, I data) throws ExecutionException {
        InTypeInfo<?> info = impl.getInTypeInfo(inType);
        if (null != info) {
            TypeTranslator<I, String> inT = (TypeTranslator<I, String>) info.getInTranslator();
            if (null != inT) {
                try {
                    service.process(inT.to(data), inType);
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
        OutTypeInfo<O> outInfo = (OutTypeInfo<O>) impl.getOutTypeInfo(outTypeName);
        DataIngestor<O> ingestor = outInfo.validateAndGetIngestor(outTypeName);
        return ingestor.waitForResult();
    }

    @Override
    public String getName() {
        return service.getName();
    }

    @Override
    public Version getVersion() {
        return service.getVersion();
    }

    @Override
    public String getDescription() {
        return service.getDescription();
    }

    @Override
    public boolean isDeployable() {
        return service.isDeployable();
    }

    @Override
    public boolean isTopLevel() {
        return service.isTopLevel();
    }

    @Override
    public ServiceKind getKind() {
        return service.getKind();
    }

    @Override
    public void migrate(String resourceId) throws ExecutionException {
        service.migrate(resourceId);
    }

    @Override
    public void update(URI location) throws ExecutionException {
        service.update(location);
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
        service.switchTo(targetId);
    }

    @Override
    public void activate() throws ExecutionException {
        service.activate();
    }

    @Override
    public void passivate() throws ExecutionException {
        service.passivate();
    }

    @Override
    public void reconfigure(Map<String, String> values) throws ExecutionException {
        service.reconfigure(values);
    }

    @Override
    public String getId() {
        return service.getId();
    }

    @Override
    public ServiceState getState() {
        return service.getState();
    }

    @Override
    public void setState(ServiceState state) throws ExecutionException {
        service.setState(state);
    }

}
