/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.tools.maven.invoker;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * Represents a system property.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SystemProperty {
    
    @Parameter(required = true)
    private String key;

    @Parameter(required = true)
    private String value;

    /**
     * Returns the property key.
     * 
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the property value.
     * 
     * @return the value (may be empty for none)
     */
    public String getValue() {
        return value;
    }

}
