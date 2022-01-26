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

import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Basic interface for a generic service that handles multiple any types of data.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface GenericService extends Service {
    
    /**
     * Adds an input/output type translator pair.
     *  
     * @param <I> the input data type
     * @param <O> the output data type
     * @param inCls the class representing the input type
     * @param outCls the class representing the output type
     * @param outTypeName the name of {@code outCls} in the configuration model
     * @param inTrans the input data type translator
     * @param outTrans the output data type translator
     */
    public <I, O> void registerTypeTranslators(Class<I> inCls, Class<O> outCls, String outTypeName, 
        TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans);
    
    /**
     * Attaches an asynchronous result data ingestor.
     * 
     * @param <O> the output data type
     * @param cls the class representing the type
     * @param ingestor the ingestor instance
     */
    public <O> void attachIngestor(Class<O> cls, DataIngestor<O> ingestor);

    /**
     * Requests asynchronous processing a data item. Results shall be "returned" via 
     * {@link #attachIngestor(Class, DataIngestor)}.
     * 
     * @param <I> the input data type
     * @param inType the class representing the type
     * @param inTypeName the name of {@code inType} in the configuration model
     * @param data the data item
     * @throws ExecutionException if the execution fails for some reason, e.g., because type translators 
     *    are not registered (@link #registerInputTypeTranslator(Class, Class, TypeTranslator, TypeTranslator)}
     */
    public <I> void processAsync(Class<I> inType, String inTypeName, I data) throws ExecutionException;

    /**
     * Requests synchronous processing a data item.
     * 
     * @param <I> the input data type
     * @param <O> the output data type
     * @param inCls the class representing the input type
     * @param inTypeName the name of {@code inType} in the configuration model
     * @param outCls the class representing the output type
     * @param data the data item to be processed
     * @return the output
     * @throws ExecutionException if the execution fails for some reason, e.g., because type translators 
     *    are not registered (@link #registerInputTypeTranslator(Class, Class, TypeTranslator, TypeTranslator)}
     */
    public <I, O> O processSync(Class<I> inCls, String inTypeName, Class<O> outCls, I data) throws ExecutionException;
    
}
