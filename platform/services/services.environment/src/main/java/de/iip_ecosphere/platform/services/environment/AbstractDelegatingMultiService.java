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

package de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.InTypeInfo;
import de.iip_ecosphere.platform.services.environment.GenericMultiTypeServiceImpl.OutTypeInfo;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * A multi-type service with an implementation based on a nested, delegated service as 
 * well as {@link GenericMultiTypeServiceImpl}.
 * 
 * @param <S> the type of service to delegate to
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractDelegatingMultiService<S extends Service> extends AbstractDelegatingService<S> 
    implements GenericMultiTypeService {
    
    private GenericMultiTypeServiceImpl impl = new GenericMultiTypeServiceImpl();
    
    /**
     * Creates an instance.
     * 
     * @param yaml the service description YAML
     */
    public AbstractDelegatingMultiService(YamlService yaml) {
        super(yaml);
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

    @Override
    public void registerInOutRelation(String inTypeName, String outTypeName) {
        impl.registerInOutRelation(inTypeName, outTypeName);
    }

    @Override
    public String getOutTypeName(String inTypeName) {
        return impl.getOutTypeName(inTypeName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I> void process(String inType, I data) throws ExecutionException {
        InTypeInfo<?> info = impl.getInTypeInfo(inType);
        if (null != info) {
            TypeTranslator<I, String> inT = (TypeTranslator<I, String>) info.getInTranslator();
            if (null != inT) {
                try {
                    processImpl(inType, inT.to(data));
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
    
    /**
     * Actually processes the data by the service.
     * 
     * @param inType the input type
     * @param data the serialized to be processed data
     * @throws IOException if processing fails
     */
    protected abstract void processImpl(String inType, String data) throws IOException;

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O processSync(String inTypeName, I data, String outTypeName) throws ExecutionException {
        process(inTypeName, data);
        OutTypeInfo<O> outInfo = (OutTypeInfo<O>) impl.getOutTypeInfo(outTypeName);
        DataIngestor<O> ingestor = outInfo.validateAndGetIngestor(outTypeName);
        return ingestor.waitForResult();
    }

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(AbstractDelegatingMultiService.class);
    }

    /**
     * Returns the multi-type implementation instance.
     * 
     * @return the implementation instance
     */
    protected GenericMultiTypeServiceImpl getImpl() {
        return impl;
    }
    
}
