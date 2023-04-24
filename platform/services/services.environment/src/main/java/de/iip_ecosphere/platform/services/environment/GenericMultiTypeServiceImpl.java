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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.AbstractPythonProcessService.SyncDataIngestor;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Basic implementation classes for {@link GenericMultiTypeService}. Intended use is by delegation.
 * 
 * @author Holger Eichelberger, SSE
 */
public class GenericMultiTypeServiceImpl {

    private Map<String, OutTypeInfo<?>> outTypeInfos = new HashMap<>();
    private Map<String, InTypeInfo<?>> inTypeInfos = new HashMap<>();

    /**
     * Represents an input or output type.
     * 
     * @param <T> the Java representation of the output type
     * 
     * @author Holger Eichelberger, SSE
     */
    public abstract static class AbstractTypeInfo<T> {
        
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
    public static class InTypeInfo <T> extends AbstractTypeInfo<T> {
        
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
    public static class OutTypeInfo <T> extends AbstractTypeInfo<T> {
        
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
        
        /**
         * Returns the associated ingestor.
         * 
         * @return the ingestor (may be <b>null</b>)
         */
        protected DataIngestor<T> getIngestor() {
            return ingestor;
        }
        
        /**
         * Validates the associated ingestor and returns it. If no ingestor is associated, an ingestor
         * for synchronous processing ({@link SyncDataIngestor}) will be created and associated.
         * 
         * @param typeName the data type name as specified in the configuration model
         * @return the ingestor
         */
        protected DataIngestor<T> validateAndGetIngestor(String typeName) {
            if (null == ingestor) {
                getLogger().info(
                    "No ingestor registered for: {}. Registering an internal synchronous ingestor.", typeName);
                ingestor = new SyncDataIngestor<T>();
            }
            return ingestor;
        }
        
        /**
         * Validates the associated ingestor and ingests the given data. If no ingestor is associated, an ingestor
         * for synchronous processing ({@link SyncDataIngestor}) will be created and associated.
         * 
         * @param typeName the data type name as specified in the configuration model
         * @param data to be ingested
         */
        protected void validateAndIngest(String typeName, String data) throws IOException {
            ingestor.ingest(outTranslator.to(data));
        }

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
    public <O> OutTypeInfo<O> obtainOutTypeInfo(Class<O> cls, String typeName) {
        OutTypeInfo<O> info = (OutTypeInfo<O>) outTypeInfos.get(typeName);
        if (null == info) {
            info = new OutTypeInfo<O>(cls);
            outTypeInfos.put(typeName, info);
        }
        return info;
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
    public <I> InTypeInfo<I> obtainInTypeInfo(Class<I> cls, String typeName) {
        InTypeInfo<I> info = (InTypeInfo<I>) inTypeInfos.get(typeName);
        if (null == info) {
            info = new InTypeInfo<I>(cls);
            inTypeInfos.put(typeName, info);
        }
        return info;
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
     * Returns the output type information object for the given symbolic type name.
     * 
     * @param outTypeName the symbolic type name
     * @return the information object or <b>null</b> if none was registered
     */
    public OutTypeInfo<?> getOutTypeInfo(String outTypeName) {
        return outTypeInfos.get(outTypeName);
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
    public <I> void registerInputTypeTranslator(Class<I> inCls, String inTypeName, TypeTranslator<I, String> inTrans) {
        InTypeInfo<I> info = obtainInTypeInfo(inCls, inTypeName);
        info.inTranslator = inTrans;
    }

    /**
     * Attaches an asynchronous result data ingestor.
     * 
     * @param <O> the output data type
     * @param outCls the class representing the type
     * @param outTypeName symbolic name of {@code outCls}, e.g. from configuration model
     * @param ingestor the ingestor instance
     */
    public <O> void attachIngestor(Class<O> outCls, String outTypeName, DataIngestor<O> ingestor) {
        OutTypeInfo<O> info = (OutTypeInfo<O>) obtainOutTypeInfo(outCls, outTypeName);
        info.ingestor = ingestor;
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
    public <O> void registerOutputTypeTranslator(Class<O> outCls, String outTypeName,
        TypeTranslator<String, O> outTrans) {
        OutTypeInfo<O> info = obtainOutTypeInfo(outCls, outTypeName);
        info.outTranslator = outTrans;
    }
    
    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    protected static Logger getLogger() {
        return LoggerFactory.getLogger(GenericMultiTypeServiceImpl.class);
    }
    
}
