package de.iip_ecosphere.platform.transport.serialization;

import java.io.IOException;

/**
 * Defines a type translator between two given types. Translation may imply a loss or augmentation of information.
 *
 * @param <S> the source type
 * @param <T> the target type
 * @author Holger Eichelberger, SSE
 */
public interface TypeTranslator<S, T> {

    /**
     * Translates a source value into a target value.
     * 
     * @param source the source value to be translated
     * @return the target value
     * @throws IOException in case that translation fails
     */
    public T to(S source) throws IOException;
    
    /**
     * Deserializes a target value into a source value.
     * 
     * @param data the data to be translated back
     * @return the serialized object
     * @throws IOException in case that serialization fails
     */
    public S from(T data) throws IOException;
    
}
