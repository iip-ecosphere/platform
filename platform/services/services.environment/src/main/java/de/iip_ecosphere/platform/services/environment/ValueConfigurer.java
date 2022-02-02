package de.iip_ecosphere.platform.services.environment;

import java.util.concurrent.ExecutionException;

/**
 * A function which can configure a (parameter) value of a given type.
 *
 * @param <T> the type of the parameter value, e.g., a method, an attribute, etc.
 * @author Holger Eichelberger, SSE
 */
public interface ValueConfigurer<T> {
    
    /**
     * Configures the parameter with the given value.
     * 
     * @param value the value to use
     * @throws ExecutionException if configuring the value fails for some reason
     */
    public void configure(T value) throws ExecutionException;
    
}
