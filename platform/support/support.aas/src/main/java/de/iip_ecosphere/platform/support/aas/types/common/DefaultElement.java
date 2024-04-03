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
 * A simple, beans-based default implementation of {@link Element}.
 * 
 * @param <T> the element type
 * @author Holger Eichelberger, SSE
 */
public class DefaultElement<T> implements Element<T> {

    private T value;
    private String semanticId;
    
    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String getSemanticId() {
        return semanticId;
    }

    @Override
    public void setSemanticId(String semanticId) {
        this.semanticId = semanticId;
    }

}
