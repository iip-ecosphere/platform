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

package de.iip_ecosphere.platform.support.aas.types.common;

/**
 * Default {@link ElementCreator} implementation for {@code DefaultElement}.
 * 
 * @param <T> the value type of the element
 * @author Holger Eichelberger, SSE
 */
public class DefaultElementCreator<T> implements ElementCreator<T> {
    
    private Class<T> type;

    /**
     * Creates a {@link DefaultElement} creator.
     * 
     * @param type the element type
     */
    public DefaultElementCreator(Class<T> type) {
        this.type = type;
    }

    @Override
    public Element<T> createElement() {
        return new DefaultElement<T>();
    }

    @Override
    public Class<T> getType() {
        return type;
    }

}
