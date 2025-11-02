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

package de.iip_ecosphere.platform.support;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to indicate which logical filter is to be used for filtering out properties of type (class) 
 * annotated. Filters may have to be declared explicitly declared/enabled on the respective data mechanism. Abstracted
 * from FasterXML/Jackson.
 * 
 * @author Holger Eichelberger, SSE
 */
@Retention(RUNTIME)
@Target({ TYPE })
public @interface Filter {

    /**
     * Id of filter to use; if empty String (""), no filter is to be used.
     */
    public String value();    
    
}
