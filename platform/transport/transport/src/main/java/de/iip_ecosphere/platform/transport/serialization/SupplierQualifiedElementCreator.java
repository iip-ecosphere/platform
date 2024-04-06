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

import java.util.function.Supplier;

/**
 * Default {@link QualifiedElementCreator} implementation based on a supplier.
 * 
 * @param <T> the value type of the element
 * @author Holger Eichelberger, SSE
 */
public class SupplierQualifiedElementCreator<T> implements QualifiedElementCreator<T> {
    
    private Class<T> type;
    private Supplier<QualifiedElement<T>> supplier;

    /**
     * Creates a supplier-based element creator.
     * 
     * @param supplier the supplier
     * @param type the type
     */
    public SupplierQualifiedElementCreator(Supplier<QualifiedElement<T>> supplier, Class<T> type) {
        this.supplier = supplier;
        this.type = type;
    }

    @Override
    public QualifiedElement<T> createElement() {
        return supplier.get();
    }

    @Override
    public Class<T> getType() {
        return type;
    }

}
