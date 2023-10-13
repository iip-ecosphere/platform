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

package de.iip_ecosphere.platform.support.iip_aas.json;

import java.util.function.Function;

import de.iip_ecosphere.platform.support.TaskRegistry;

/**
 * Uniform way to represent results of AAS operations that may fail.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonResultWrapper extends de.iip_ecosphere.platform.support.json.JsonResultWrapper {

    private static final long serialVersionUID = -4306755602763593416L;

    /**
     * Creates a wrapper object.
     * 
     * @param func a function that may throw an exception.
     */
    public JsonResultWrapper(ExceptionFunction func) {
        super(func);
    }

    /**
     * Creates a wrapper object.
     * 
     * @param func a function that may throw an exception.
     * @param listener optional operation completed listener
     */
    public JsonResultWrapper(ExceptionFunction func, OperationCompletedListener listener) {
        super(func, listener);
    }

    /**
     * Creates a wrapper object.
     * 
     * @param func a function that may throw an exception.
     * @param taskIdSupplier optional task id to track via {@link TaskRegistry}
     */
    public JsonResultWrapper(ExceptionFunction func, Function<Object[], String> taskIdSupplier) {
        super(func, taskIdSupplier);
    }

    /**
     * Creates a wrapper object.
     * 
     * @param func a function that may throw an exception.
     * @param listener optional operation completed listener
     * @param taskIdSupplier optional task id to track via {@link TaskRegistry}
     */
    public JsonResultWrapper(ExceptionFunction func, OperationCompletedListener listener, 
        Function<Object[], String> taskIdSupplier) {
        super(func, listener, taskIdSupplier);
    }

}
