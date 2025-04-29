/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents something that can be invoked, e.g., an operation. Contains a mix of old and v3 functionality. These
 * instances are created by the {@link InvocablesCreator}, clients shall interact based on their type, e.g., operations
 * request the created operation via {@link #getOperation()} and ignore the rest. Depending on the implementation, 
 * clients may detail the typed signature of the actual invokable.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Invokable {
    
    /**
     * Getter invokable for v1.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface GetterInvokable extends Invokable, Supplier<Object> {

        @Override
        public default Supplier<Object> getGetter() {
            return this; // default for v3
        }

    }

    /**
     * Setter invokable for v1.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface SetterInvokable extends Invokable, Consumer<Object> {

        @Override
        public default Consumer<Object> getSetter() {
            return this; // default for v3
        }

    }

    /**
     * Operation invokable for v1.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface OperationInvokable extends Invokable, Function<Object[], Object> {

        @Override
        public default Function<Object[], Object> getOperation() {
            return this; // default for v3
        }

    }
    
    /**
     * Serializable invokable for v1.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface SerializableInvokable extends Invokable, Serializable {
        
    }
    
    /**
     * Creates a getter invokable from a given {@code getter} supplier.
     * 
     * @param getter the supplier representing the getter
     * @return the getter invokable
     */
    public static Invokable createInvokable(Supplier<Object> getter) {
        return new Invokable() {
            
            @Override
            public Supplier<Object> getGetter() {            
                return getter;
            }

        };

    }

    /**
     * Creates a setter invokable from a given {@code setter} consumer.
     * 
     * @param setter the consumer representing the setter
     * @return the setter invokable
     */
    public static Invokable createInvokable(Consumer<Object> setter) {
        return new Invokable() {
        
            @Override
            public Consumer<Object> getSetter() {
                return setter;
            }
    
        };        
    }

    /**
     * Creates an operation invokable from a given {@code function}.
     * 
     * @param function the function representing the operation
     * @return the operation invokable
     */
    public static Invokable createInvokable(Function<Object[], Object> function) {
        return new Invokable() {

            @Override
            public Function<Object[], Object> getOperation() {            
                return function;
            }            
        };        
    }
    

    /**
     * Creates a getter invokable from a given {@code getter} supplier.
     * 
     * @param getter the supplier representing the getter
     * @return the getter invokable
     */
    public static Invokable createSerializableInvokable(Supplier<Object> getter) {
        return new SerializableInvokable() {
            
            private static final long serialVersionUID = 7967781506782273712L;

            @Override
            public Supplier<Object> getGetter() {            
                return getter;
            }

        };

    }

    /**
     * Creates a setter invokable from a given {@code setter} consumer.
     * 
     * @param setter the consumer representing the setter
     * @return the setter invokable
     */
    public static Invokable createSerializableInvokable(Consumer<Object> setter) {
        return new SerializableInvokable() {
        
            private static final long serialVersionUID = 4827274105413710866L;

            @Override
            public Consumer<Object> getSetter() {
                return setter;
            }
    
        };        
    }

    /**
     * Creates an operation invokable from a given {@code function}.
     * 
     * @param function the function representing the operation
     * @return the operation invokable
     */
    public static Invokable createSerializableInvokable(Function<Object[], Object> function) {
        return new SerializableInvokable() {

            private static final long serialVersionUID = 2945328936175486929L;

            @Override
            public Function<Object[], Object> getOperation() {            
                return function;
            }            
        };        
    }

    /**
     * Returns a created getter (proxy). [BaSyx v1]
     * 
     * @return the getter
     */
    public default Supplier<Object> getGetter() {
        return null; // default for v3
    }
    
    /**
     * Returns a created setter (proxy). [BaSyx v1]
     * 
     * @return the setter
     */
    public default Consumer<Object> getSetter() {
        return null; // default for v3
    }

    /**
     * Returns a created operation (proxy). [BaSyx v1]
     * 
     * @return the operation
     */
    public default Function<Object[], Object> getOperation() {
        return null; // default for v3
    }

    /**
     * Returns the URL of the created invokable. [v3]
     * 
     * @return the URL, empty for none
     */
    public default String getUrl() {
        return ""; // default for v1
    }
    
    /**
     * Returns the URL of the invokable submodel repository. [v3]
     * 
     * @return the URL, empty for none
     */
    public default String getSubmodelRepositoryUrl() {
        return ""; // default for v1
    }

    /**
     * Represents an implementation-specific operation invocation.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface OperationInvocation {
    }
    
    /**
     * Calls the operation represented by this invokable through a given
     * implementation-specific invocation object. [v3]
     * 
     * @param invocation the invocation object
     * @throws IOException if finding/invoking the operation fails
     */
    public default void execute(OperationInvocation invocation) throws IOException {
    }

}
