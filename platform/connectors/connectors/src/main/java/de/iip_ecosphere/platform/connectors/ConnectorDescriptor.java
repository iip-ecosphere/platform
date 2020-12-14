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

package de.iip_ecosphere.platform.connectors;

/**
 * Describes a connector without instantiating it. Required to use the Java services mechanism to
 * silently register connector classes with the {@link ConnectorRegistry}. Per connector type ({@link #getType()})
 * there shall be only a single descriptor (instance)!
 */
public interface ConnectorDescriptor {

    /**
     * Returns the name of the connector.
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * Returns the type of the connector.
     * 
     * @return the type
     */
    public Class<?> getType();
    
}