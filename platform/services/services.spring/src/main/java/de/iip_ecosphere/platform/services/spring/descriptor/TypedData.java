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

package de.iip_ecosphere.platform.services.spring.descriptor;

/**
 * Represents typed data, e.g., a parameter.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface TypedData {

    /**
     * Returns the name of the data.
     * 
     * @return the name
     */
    public String getName();

    /**
     * Returns the description of the data.
     * 
     * @return the description
     */
    public String getDescription();

    /**
     * Returns the type of the data.
     * 
     * @return the type as qualified Java name
     */
    public String getType();

}
