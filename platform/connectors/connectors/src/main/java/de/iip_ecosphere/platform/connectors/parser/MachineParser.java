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

package de.iip_ecosphere.platform.connectors.parser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;

/**
 * Indicates the capabilities of a machine parser. It may be used to dynamically steer the code generation. 
 * 
 * @author Holger Eichelberger, SSE
 * @see ModelAccess
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MachineParser {

    /**
     * Whether the parser supports index-based access (despite the interface).
     * 
     * @return {@code true} for index-based access, {@code} false for name-based access
     */
    public boolean supportsIndexes() default true;

    /**
     * Whether the machine connector supports hierarchical names (despite the interface).
     * 
     * @return {@code true} for hierarchical names, {@code false} else
     */
    public boolean supportsNames() default true;

    /**
     * Whether the machine connector supports hierarchical names (requires {@link #supportsNames()}.
     * 
     * @return {@code true} for hierarchical names, {@code false} else
     */
    public boolean supportsHierarchicalNames() default true;

    /**
     * Whether the machine connector supports stepping into nested structures (or emulates that).
     * 
     * @return {@code true} for nesting, {@code false} else
     */
    public boolean supportsNesting() default true;
    
}
