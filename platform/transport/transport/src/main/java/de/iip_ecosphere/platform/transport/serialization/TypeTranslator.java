package de.iip_ecosphere.platform.transport.serialization;

/**
 * Defines a type translator between two given types. Translation may imply a loss or augmentation of information.
 *
 * @param <S> the source type
 * @param <T> the target type
 * @author Holger Eichelberger, SSE
 */
public interface TypeTranslator<S, T> extends InputTypeTranslator<T, S>, OutputTypeTranslator<S, T> {
}
