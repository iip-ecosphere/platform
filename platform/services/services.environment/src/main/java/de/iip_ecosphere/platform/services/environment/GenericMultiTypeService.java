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

import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Basic interface for a generic service that handles multiple any types of data. We use symbolic type names to 
 * distinguish the data types used. Symbolic names can be names from the configuration model, the class name or also 
 * some unique optimized short names shared between Java and Python (through generation). Registration methods also
 * require the class of the type in order to have the type available for internal type casts.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface GenericMultiTypeService extends Service {
    
    /**
     * Adds an input type translator.
     *  
     * @param <I> the input data type
     * @param inCls the class representing the input type
     * @param inTypeName symbolic name of {@code inCls}, e.g. from configuration model
     * @param inTrans the input data type translator
     * @see #registerOutputTypeTranslator(Class, String, TypeTranslator)
     */
    public <I> void registerInputTypeTranslator(Class<I> inCls, String inTypeName, 
        TypeTranslator<I, String> inTrans);

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
        TypeTranslator<String, O> outTrans);

    /**
     * Attaches an asynchronous result data ingestor.
     * 
     * @param <O> the output data type
     * @param outCls the class representing the type
     * @param outTypeName symbolic name of {@code outCls}, e.g. from configuration model
     * @param ingestor the ingestor instance
     */
    public <O> void attachIngestor(Class<O> outCls, String outTypeName, DataIngestor<O> ingestor);

    /**
     * Requests asynchronous processing a data item.
     * 
     * @param <I> the input data type
     * @param <O> the output data type
     * @param inTypeName the name of {@code inType} in the configuration model
     * @param data the data item to be processed
     * @return the output, always <b>null</b> in case of asynchronous processing as the result is passed to a 
     *     registered ingestor
     * @throws ExecutionException if the execution fails for some reason, e.g., because type translators 
     *    are not registered (@link #registerInputTypeTranslator(Class, Class, TypeTranslator, TypeTranslator)}
     */
    public <I, O> O process(String inTypeName, I data) throws ExecutionException;

    /**
     * Requests synchronous processing a data item.
     * 
     * @param <I> the input data type
     * @param <O> the output data type
     * @param inTypeName the name of {@code inType} in the configuration model
     * @param data the data item to be processed
     * @param outTypeName the name of {@code outType} in the configuration model
     * @return the output, always <b>null</b> in case of asynchronous processing as the result is passed to a 
     *     registered ingestor
     * @throws ExecutionException if the execution fails for some reason, e.g., because type translators 
     *    are not registered (@link #registerInputTypeTranslator(Class, Class, TypeTranslator, TypeTranslator)}
     */
    public <I, O> O processSync(String inTypeName, I data, String outTypeName) throws ExecutionException;

    /**
     * Requests asynchronous processing a data item. Shall call {@link #process(String, Object)} but handle potential
     * exceptions.
     * 
     * @param <I> the input data type
     * @param <O> the output data type
     * @param inTypeName the name of {@code inType} in the configuration model
     * @param data the data item to be processed
     * @return the output, always <b>null</b> in case of asynchronous processing as the result is passed to a 
     *     registered ingestor
     */
    public default <I, O> O processQuiet(String inTypeName, I data) {
        try {
            return process(inTypeName, data);
        } catch (ExecutionException e) {
            LoggerFactory.getLogger(getClass()).error("Processing failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Requests asynchronous processing a data item. Shall call {@link #process(String, Object)} but handle potential
     * exceptions.
     * 
     * @param <I> the input data type
     * @param <O> the output data type
     * @param inTypeName the name of {@code inType} in the configuration model
     * @param data the data item to be processed
     * @param outTypeName the name of {@code outType} in the configuration model
     * @return the output, always <b>null</b> in case of asynchronous processing as the result is passed to a 
     *     registered ingestor
     */
    public default <I, O> O processSyncQuiet(String inTypeName, I data, String outTypeName) {
        try {
            return processSync(inTypeName, data, outTypeName);
        } catch (ExecutionException e) {
            LoggerFactory.getLogger(getClass()).error("Processing failed: " + e.getMessage());
            return null;
        }
    }

}
