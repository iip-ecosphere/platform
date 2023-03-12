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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry.SerializerProvider;

/**
 * Very basic serializer provider.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicSerializerProvider implements SerializerProvider {

    private Map<Class<?>, Serializer<?>> serializers = Collections.synchronizedMap(new HashMap<>());

    @SuppressWarnings("unchecked")
    @Override
    public <T> Serializer<T> getSerializer(Class<T> type) {
        return (Serializer<T>) serializers.get(type);
    }

    /**
     * Registers a serializer.
     * 
     * @param <T>        the type of the data
     * @param serializer the serializer instance (must not be <b>null</b>)
     */
    public <T> void registerSerializer(Serializer<T> serializer) {
        serializers.put(serializer.getType(), serializer);
    }

}
