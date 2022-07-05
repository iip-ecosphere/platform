package de.iip_ecosphere.platform.support.resources;

import java.io.InputStream;

/**
 * Resolves resources.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ResourceResolver {
    
    /**
     * Returns the name of the resource resolver.
     * 
     * @return the name, default is the class name
     */
    public default String getName() {
        return getClass().getName();
    }
    
    /**
     * Resolves a resource to an input stream using the default class loader.
     * 
     * @param resource the name of the resource
     * @return the related input stream, may be <b>null</b> for none
     */
    public default InputStream resolve(String resource) {
        return resolve(getClass().getClassLoader(), resource);
    }
    
    /**
     * Resolves a resource to an input stream.
     * 
     * @param loader the class loader to use
     * @param resource the name of the resource
     * @return the related input stream, may be <b>null</b> for none
     */
    public InputStream resolve(ClassLoader loader, String resource);
    
}