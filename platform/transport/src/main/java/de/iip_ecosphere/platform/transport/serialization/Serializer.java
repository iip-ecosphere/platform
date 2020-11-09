package de.iip_ecosphere.platform.transport.serialization;

import java.io.IOException;

/**
 * A data serializer (so far for homogeneous streams, may require unique ids).
 * 
 * @param <T> the type to be serialized
 * @author Holger Eichelberger, SSE
 */
public interface Serializer<T> {

    /**
     * Serializes a value into a byte array.
     * 
     * @param value the value to be serialized
     * @return the serialized data
     * @throws IOException in case that serialization fails
     */
    public byte[] serialize(T value) throws IOException;

    /**
     * Deserializes a byte array into a value.
     * 
     * @param data the data to be deserialized
     * @return the serialized object
     * @throws IOException in case that serialization fails
     */
    public T deserialize(byte[] data) throws IOException;

    /**
     * The type to be handled by this serializer.
     * 
     * @return the type
     */
    public Class<T> getType();

}
