/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.serialization;

/**
 * Default {@link QualifiedElementCreator} implementation for {@code DefaultQualifiedElement}.
 * 
 * @param <T> the value type of the element
 * @author Holger Eichelberger, SSE
 */
public class DefaultQualifiedElementCreator<T> implements QualifiedElementCreator<T> {
    
    private Class<T> type;

    /**
     * Creates a {@link DefaultQualifiedElement} creator.
     * 
     * @param type the element type
     */
    public DefaultQualifiedElementCreator(Class<T> type) {
        this.type = type;
    }

    @Override
    public QualifiedElement<T> createElement() {
        return new DefaultQualifiedElement<T>();
    }

    @Override
    public Class<T> getType() {
        return type;
    }

}