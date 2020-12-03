package de.iip_ecosphere.platform.transport.serialization;

import java.io.IOException;

/**
 * Defines a type translator between two given types. Translation may imply a loss or augmentation of information.
 *
 * @param <S> the source type
 * @param <T> the target type
 * @author Holger Eichelberger, SSE
 */
public interface OutputTypeTranslator<S, T> {

    /**
     * Translates a source value into a target value ("output <b>to</b> external").
     * 
     * @param source the source value to be translated
     * @return the target value
     * @throws IOException in case that translation fails
     */
    public T to(S source) throws IOException;
    
}
