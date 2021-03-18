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

package de.iip_ecosphere.platform.support.jsl;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker annotation for <b>descriptor</b> classes that shall be excluded in a first search round for JSL descriptors. 
 * This is in particular helpful if test classes are in the dependencies that define a descriptor for testing that shall
 * not accidentally "overlay" production descriptors. To activate this annotation, use 
 * {@link ServiceLoaderUtils#filterExcluded(Class)}. 
 * 
 * @author Holger Eichelberger, SSE
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcludeFirst {

}
