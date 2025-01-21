package de.iip_ecosphere.platform.connectors.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class RESTServerResponse {

    /**
     * Setter for Class attributes.
     * 
     * @param key = name of the attribute to set
     * @param value to set for the attribute
     */
    public abstract void set(String key, Object value);
    
    /**
     * Getter for values. Returns null if no values existst.
     * 
     * @return values or null
     */
    //public abstract Object[] getValues();
}
