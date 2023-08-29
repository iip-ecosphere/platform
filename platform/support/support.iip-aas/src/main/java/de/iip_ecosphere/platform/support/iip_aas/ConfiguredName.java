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

package de.iip_ecosphere.platform.support.iip_aas;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Records configured names which may be different from class or field names. May in future take over the role 
 * of certain serializer annotations.
 * 
 * @author Holger Eichelberger, SSE
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
public @interface ConfiguredName {
    
    /**
     * The configured name.
     *  
     * @return the configured name
     */
    public String value() default "";

}
