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

package de.iip_ecosphere.platform.support.json;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Uniform JSON way to represent results of operations that may fail.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonResultWrapper implements Function<Object[], Object>, Serializable {

    private static final long serialVersionUID = 6531890963314078947L;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private ExceptionFunction func;
    private OperationCompletedListener listener;
    private Function<Object[], String> taskIdSupplier;

    /**
     * Creates a wrapper object.
     * 
     * @param func a function that may throw an exception.
     */
    public JsonResultWrapper(ExceptionFunction func) {
        this(func, null, null);
    }

    /**
     * Creates a wrapper object.
     * 
     * @param func a function that may throw an exception.
     * @param listener optional operation completed listener
     */
    public JsonResultWrapper(ExceptionFunction func, OperationCompletedListener listener) {
        this(func, listener, null);
    }

    /**
     * Creates a wrapper object.
     * 
     * @param func a function that may throw an exception.
     * @param taskIdSupplier optional task id to track via {@link TaskRegistry}
     */
    public JsonResultWrapper(ExceptionFunction func, Function<Object[], String> taskIdSupplier) {
        this(func, null, taskIdSupplier);
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
        this.func = func;
        this.listener = listener;
        this.taskIdSupplier = taskIdSupplier;
    }

    /**
     * Represents the result w/o exception.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Result implements Serializable {
        
        private static final long serialVersionUID = -4586150559933545643L;

        @JsonInclude(Include.NON_NULL)
        private String result;

        @JsonInclude(Include.NON_NULL)
        private String exception;

        /**
         * Creates a result object (for JSON).
         */
        private Result() {
        }
        
        /**
         * Creates a result object for a result value.
         * 
         * @param result the result value
         */
        private Result(String result) {
            this.result = result;
        }

        /**
         * Creates a result object for an exceptional situation.
         * 
         * @param ex the exception
         */
        private Result(Throwable ex) {
            exception = ex.getMessage();
        }

        /**
         * Returns the result.
         * 
         * @return the result
         */
        public String getResult() {
            return result;
        }

        /**
         * Returns the exception text.
         * 
         * @return the exception text
         */
        public String getException() {
            return exception;
        }
        
        /**
         * Returns whether this object represents an exception.
         * 
         * @return {@code true} for exception, {@code false} for normal execution
         */
        public boolean isException() {
            return null != exception;
        }
        
        /**
         * Defines the result.
         * 
         * @param result the result
         */
        public void setResult(String result) {
            this.result = result;
        }

        /**
         * Defines the exception text.
         * 
         * @param exception the exception text
         */
        public void setException(String exception) {
            this.exception = exception;
        }

    }
    
    // checkstyle: stop exception type check
    
    /**
     * A function that may throw an exception.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ExceptionFunction extends Serializable {
        
        /**
         * Applies the function.
         * 
         * @param params function parameter
         * @return function result
         * @throws Exception potentially thrown exception
         */
        public Object apply(Object[] params) throws Exception;
        
    }

    @Override
    public Object apply(Object[] param) {
        Result result;
        String taskId = null;
        if (null != taskIdSupplier) {
            taskId = taskIdSupplier.apply(param);
        }
        try {
            if (null != taskId) {
                TaskRegistry.registerTask(taskId);
            }
            Object funcRes = func.apply(param);
            result = new Result(null == funcRes ? null : funcRes.toString());
            if (null != listener) {
                listener.operationCompleted();
            }
            if (null != taskId) {
                TaskRegistry.stopTask(taskId);
            }
        } catch (Throwable e) { // including AasExecutionException, NPE
            LoggerFactory.getLogger(getClass()).error("Operation execution failed: {}", e.getMessage());
            LoggerFactory.getLogger(getClass()).trace("Oktoflow debug catch: ", e);
            result = new Result(e);
            if (null != listener) {
                listener.operationFailed();
            }
            if (null != taskId) {
                TaskRegistry.stopTask(taskId);
            }
        }
        return toJson(result);
    }

    // checkstyle resume exception type check
    
    /**
     * Turns a {@link Result} into JSON.
     * 
     * @param res the result instance (may be <b>null</b>)
     * @return the JSON string or an empty string in case of problems/no address
     */
    public static String toJson(Result res) {
        String result = "";
        if (null != res) {
            try {
                result = MAPPER.writeValueAsString(res);
            } catch (JsonProcessingException e) {
                // handled by default value
            }
        } 
        return result;
    }
    
    
    /**
     * Turns something in JSON into a {@code Result}.
     * 
     * @param json the JSON value, usually a String
     * @return the result instance
     */
    public static Result resultFromJson(Object json) {
        Result result = null;
        if (null != json) {
            try {
                result = MAPPER.readValue(json.toString(), Result.class);
            } catch (JsonProcessingException e) {
                result = new Result(e);
            }
        }
        return result; 
    }

    /**
     * Turns something in JSON into a result via an instance of {@code Result}.
     * 
     * @param json the JSON value, usually a String
     * @return the result value, may be <b>null</b>, e.g., if {@code json} is <b>null</b>
     * @throws ExecutionException if the parsed {@link Result} represents an exception
     */
    public static String fromJson(Object json) throws ExecutionException {
        String result; 
        Result res = resultFromJson(json);
        if (null != res) {
            if (res.isException()) {
                throw new ExecutionException(res.getException(), null);
            }
            result = res.getResult();
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Executes {@code function} with {@code params} and catches all occurring exceptions turning them into an 
     * {@link ExecutionException}.
     * 
     * @param function the function to be executed
     * @param params the function parameters
     * @return the return value
     * @throws ExecutionException in case that function cannot be executed
     */
    public static String fromJson(Function<Object[], Object> function, Object... params) throws ExecutionException {
        try {
            return fromJson(function.apply(params));
        } catch (Throwable t) {
            throw new ExecutionException(t.getMessage(), t);
        }
    }
    
    /**
     * Allows to track operations.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface OperationCompletedListener {
        
        /**
         * Called when an operation is completed.
         */
        public void operationCompleted();

        /**
         * Called when an operation failed due to an exception.
         */
        public void operationFailed();

    }

}
