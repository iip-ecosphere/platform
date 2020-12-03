/**
 *******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;

/**
 * Indicates the capabilities of a connector, also to dynamically steer the code generation. Values shall be compliant
 * with the respective interfaces in the type hierarchy.
 * 
 * @author Holger Eichelberger, SSE
 * @see ModelAccess
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MachineConnector {

    /**
     * Whether the machine connector has a model at all.
     * 
     * @return {@code true} for model, {@code} false for no model
     */
    public boolean hasModel() default true;
    
    /**
     * Whether the machine connector supports hierarchical qualified names in the model (requires {@link #hasModel()} 
     * is {@code true)).
     * 
     * @return {@code true} for hierarchical names, {@code false} else
     */
    public boolean supportsHierarchicalQNames() default true;

    /**
     * Whether the machine connector supports calls via the model (requires {@link #hasModel()} is {@code true)).
     * 
     * @return {@code true} for calls, {@code false} for no calls
     */
    public boolean supportsModelCalls() default true;

    /**
     * Whether the machine connector supports properties via the model (requires {@link #hasModel()} is {@code true)).
     * 
     * @return {@code true} for properties, {@code false} for no properties
     */
    public boolean supportsModelProperties() default true;

    /**
     * Whether the machine connector supports user-defined structs in the model (requires {@link #hasModel()} 
     * is {@code true)).
     * 
     * @return {@code true} for user-defined structs, {@code false} for no structs
     */
    public boolean supportsModelStructs() default true;

    /**
     * Whether the machine connector supports events on changed data, i.e., polling by the connector becomes optional.
     * If no events are supported and if {@link #hasModel()} is {@code true}, {@link ModelAccess#monitor(String...)}
     * may be not supported, i.e., throw exceptions.
     * 
     * @return {@code true} for events, {@code false} for no events (polling is required)
     */
    public boolean supportsEvents() default true;
    
    /**
     * Whether the machine connector can work with Java values on the model directly and does not need conversation to 
     * connector-specific instances(requires {@link #hasModel()} is {@code true)).
     * 
     * @return {@code true} for object-based values, {@code false} if conversation is required
     */
    public boolean acceptsObject() default false;
    
}
