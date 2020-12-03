package de.iip_ecosphere.platform.transport.serialization;

import java.io.IOException;

/**
 * Defines a type translator between two given types. Translation may imply a loss or augmentation of information.
 *
 * @param <T> the target type
 * @param <S> the source type
 * @author Holger Eichelberger, SSE
 */
public interface InputTypeTranslator<T, S> {
    
    /**
     * Deserializes a target value into a source value ("input <b>from</b> external").
     * 
     * @param data the data to be translated back
     * @return the serialized object
     * @throws IOException in case that serialization fails
     */
    public S from(T data) throws IOException;
    
}
