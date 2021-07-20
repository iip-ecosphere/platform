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

package de.iip_ecosphere.platform.support;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Optional annotation for lifecycle descriptors to prevent descriptor interaction. Handle with care.
 * 
 * @author Holger Eichelberger, SSE
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface LifecycleExclude {
    
    /**
     * Returns class names of known lifecycle handles that shall not be started although present as descriptors.
     * 
     * @return excluded descriptors
     */
    public Class<?>[] value() default {};
    
    /**
     * Returns class names of known lifecycle handles that shall not be started although present as descriptors.
     * 
     * @return excluded descriptors
     */
    public String[] names() default {};

}
