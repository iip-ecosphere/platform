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

package de.iip_ecosphere.platform.connectors.formatter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;

/**
 * Indicates the capabilities of a machine formatter. It may be used to dynamically steer the code generation. 
 * 
 * @author Holger Eichelberger, SSE
 * @see ModelAccess
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MachineFormatter {

    /**
     * Whether the machine connector supports hierarchical names.
     * 
     * @return {@code true} for hierarchical names, {@code false} else
     */
    public boolean supportsHierarchicalNames() default true;

}
