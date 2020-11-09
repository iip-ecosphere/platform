package de.iip_ecosphere.platform.transport.serialization;

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
