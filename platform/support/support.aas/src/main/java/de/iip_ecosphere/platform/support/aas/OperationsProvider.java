/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides operation implementations.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface OperationsProvider {

    /**
     * May intercept results, in particular for testing/mocking.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Interceptor {
        
        /**
         * Returns a registered service operation.
         * 
         * @param category the category
         * @param name the name of the service operation
         * @param origin the origin operation functor, may be <b>null</b>
         * @return the operation functor, may be <b>null</b>
         */
        public default Function<Object[], Object> getOperation(String category, String name, 
            Function<Object[], Object> origin) {
            return origin;
        }
        
        /**
         * Returns the getter for a specified property.
         * 
         * @param name the name of the property
         * @param origin the origin getter, may be <b>null</b>
         * @return the getter, may be <b>null</b> for none
         */
        public default Supplier<Object> getGetter(String name, Supplier<Object> origin) {
            return origin;
        }

        /**
         * Returns the setter for a specified property.
         * 
         * @param name the name of the property
         * @param origin the origin setter, may be <b>null</b>
         * @return the setter, may be <b>null</b> for none
         */
        public default Consumer<Object> getSetter(String name, Consumer<Object> origin) {
            return origin;
        }
        
    }
    
    /**
     * Defines an operation.
     * 
     * @param category the category
     * @param name the name of the operation
     * @param function the implementing function
     * @return <b>this</b>
     * @throws IllegalArgumentException if the operation is already registered
     */
    public OperationsProvider defineOperation(String category, String name, Function<Object[], Object> function);
    
    /**
     * Returns a registered service operation.
     * 
     * @param category the category
     * @param name the name of the service operation
     * @return the operation functor, may be <b>null</b>
     * @see #defineOperation(String, String, Function)
     */
    public Function<Object[], Object> getOperation(String category, String name);
    
    /**
     * Returns a registered service operation.
     * 
     * @param name the name of the service operation
     * @return the operation functor, may be <b>null</b>
     * @see #defineServiceFunction(String, Function)
     */
    public Function<Object[], Object> getServiceFunction(String name);
    
    /**
     * Defines a service function (in a pre-defined category).
     * 
     * @param name the name of the service operation
     * @param function the implementing function
     * @return <b>this</b>
     * @see #defineOperation(String, String, Function)
     * @throws IllegalArgumentException if the operation is already registered
     */
    public OperationsProvider defineServiceFunction(String name, Function<Object[], Object> function);

    /**
     * Defines a property with getter/setter implementation. Theoretically, either getter/setter
     * may be <b>null</b> for read-only/write-only properties, but this must be, however, reflected in the AAS so that 
     * no wrong can access happens.
     * 
     * @param name the name of the property
     * @param get the supplier providing read access to the property value (may be <b>null</b>)
     * @param set the consumer providing write access to the property value (may be <b>null</b>)
     * @return <b>this</b>
     * @throws IllegalArgumentException if the property is already registered
     */
    public OperationsProvider defineProperty(String name, Supplier<Object> get, Consumer<Object> set);
    
    /**
     * Returns the getter for a specified property.
     * 
     * @param name the name of the property
     * @return the getter, may be <b>null</b> for none
     */
    public Supplier<Object> getGetter(String name);

    /**
     * Returns the setter for a specified property.
     * 
     * @param name the name of the property
     * @return the setter, may be <b>null</b> for none
     */
    public Consumer<Object> getSetter(String name);
    
    /**
     * Sets an interceptor for all returns, primarily intended for testing/mocking to circumvent implementations that
     * do not support direct mocking. Shall not be used for regular purposes. May be ignored by the implementation.
     * 
     * @param interceptor the intercept instance, may be <b>null</b>
     */
    public void setInterceptor(Interceptor interceptor);
    
}
