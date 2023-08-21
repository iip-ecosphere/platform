/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
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
 * A specialized serializer that creates a generic Json fallback if there is no serializer.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicSerializerProviderWithJsonDefault extends BasicSerializerProvider {
 
    @Override
    public <T> Serializer<T> getSerializer(Class<T> type) {
        Serializer<T> result = super.getSerializer(type);
        if (null == result) {
            result = createDefault(type);
            registerSerializer(result);
        }
        return result;
    }
    
    /**
     * Creates a serializer default.
     * 
     * @param <T> the type to create the fallback for
     * @param type the type to create the fallback for
     * @return the default serializer 
     */
    protected <T> Serializer<T> createDefault(Class<T> type) {
        return new GenericJsonSerializer<>(type);
    }
    
}
