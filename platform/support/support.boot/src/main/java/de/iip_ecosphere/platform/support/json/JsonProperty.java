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

package de.iip_ecosphere.platform.support.json;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import de.iip_ecosphere.platform.support.ConfiguredName;

/**
 * Simplified annotation to specify properties. To be replaced by {@link ConfiguredName}.
 * 
 * @author Fasterxml/Jackson
 */
@Retention(RUNTIME)
@Target({ANNOTATION_TYPE, FIELD, METHOD, PARAMETER})
public @interface JsonProperty {

    /**
     * Defines name of the logical property, i.e. JSON object field
     * name to use for the property. If value is empty String (which is the
     * default), will try to use name of the field that is annotated.
     */
    public String value() default "";
    
}
