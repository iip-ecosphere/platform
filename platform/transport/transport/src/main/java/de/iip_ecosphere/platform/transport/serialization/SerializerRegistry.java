/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package de.iip_ecosphere.platform.transport.serialization;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A registry for serializers to be able to handle also nested types on-demand.
 * All relevant serializers must be registered for correct functionality.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SerializerRegistry {

    private static Map<Class<?>, Serializer<?>> serializers = Collections.synchronizedMap(new HashMap<>());

    /**
     * Returns a serializer instance.
     * 
     * @param <T>  the data type to be handled by the serializer
     * @param type the type to return the serializer for
     * @return the serializer, <b>null</b> if no such serializer is registered
     */
    @SuppressWarnings("unchecked")
    public static <T> Serializer<T> getSerializer(Class<T> type) {
        return (Serializer<T>) serializers.get(type);
    }
    
    /**
     * Returns whether a serializer is known for the given {@code type}.
     * 
     * @param type the type to query for
     * @return {@code true} if there is a registered serizalizer, {@code false} else
     */
    public static boolean hasSerializer(Class<?> type) {
        return serializers.get(type) != null;
    }
    
    /**
     * Registers a serializer through its type. An accessible no-arg constructor is required for {@code type}.
     * 
     * @param <T> the type to be serialized
     * @param type the type of the serializer to register
     * @throws IllegalArgumentException if the required no-arg constructor on {@code type} cannot be found, called 
     *   or executed, i.e., there is no instance to register
     */
    public static <T> void registerSerializer(Class<? extends Serializer<T>> type) throws IllegalArgumentException {
        try {
            registerSerializer(type.getConstructor().newInstance());
            // multi-catch for potentially outdated edge JVM
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Registers a serializer.
     * 
     * @param <T>        the type of the data
     * @param serializer the serializer instance (must not be <b>null</b>)
     */
    public static <T> void registerSerializer(Serializer<T> serializer) {
        serializers.put(serializer.getType(), serializer);
    }

    /**
     * Unregisters a serializer.
     * 
     * @param serializer the serializer instance to unregister (must not be
     *                   <b>null</b>)
     */
    public static void unregisterSerializer(Serializer<?> serializer) {
        unregisterSerializer(serializer.getType());
    }

    /**
     * Unregisters a serializer.
     * 
     * @param type the serializer type to unregister
     */
    public static void unregisterSerializer(Class<?> type) {
        serializers.remove(type);
    }

}
