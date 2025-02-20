package de.iip_ecosphere.platform.connectors.rest;

public interface Convertable {

    /**
     * Converts internal REST Object to external transport.
     * 
     * @param obj to convert
     * @return converted Object
     */
    public Object fromREST(Object obj);
    
    /**
     * Converts external transport to internal REST Object.
     * 
     * @param obj to convert
     * @return converted Object
     */
    public Object toREST(Object obj);
}
