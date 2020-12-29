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

package de.iip_ecosphere.platform.support;

/**
 * Defines the usual builder interface. Instance-specific configuring methods are supposed to return an instance of 
 * the builder. {@link #build()} finally builds the instance and disposes the builder instance.
 * 
 * @param <I> The type of the instance to build.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Builder<I> {
    
    /**
     * Builds the instance under construction. The work of the builder instance shall be done by this call.
     * 
     * @return the instance.
     */
    public I build();

}
