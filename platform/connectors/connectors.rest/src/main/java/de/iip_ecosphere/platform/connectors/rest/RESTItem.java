package de.iip_ecosphere.platform.connectors.rest;

import java.util.HashMap;



public class RESTItem {
    
    private RESTEndpointMap endpointMap;
    private HashMap <String, Object> values;
    private HashMap <String, Object> simpleValuesToWrite;
    
    /**
     * Constructor.
     */
    public RESTItem(RESTEndpointMap endpointMap) {
        
        this.endpointMap = endpointMap;
        values = new HashMap<String, Object>();
        simpleValuesToWrite = new HashMap<String, Object>();
        
        for (HashMap.Entry<String, RESTEndpoint> entry : endpointMap.entrySet()) {
            
            values.put(entry.getKey().toLowerCase(), null);
            
        }       
    }
    
    /**
     * Returs the Object stored for key.
     * 
     * @param key to retrive
     * @return Object stored for key
     */
    public Object getValue(String key) {
        return values.get(key);
    }
    
    /**
     * Sets value for key.
     * 
     * @param key for the value to set
     * @param value to set
     */
    public void setValue(String key, Object value) {
        values.put(key, value);
    }   
    
    /**
     * Getter for endpointMap.
     * 
     * @return endpointMap
     */
    public RESTEndpointMap getEndpointMap() {
        return endpointMap;
    }

   
    /**
     * Get Object value for String key from simpleValuesToWrite.
     * 
     * @param key to retreive
     * @return Object value stored for key
     */
    public Object getSimpleValueToWrite(String key) {
        return simpleValuesToWrite.get(key);
    }

    /**
     * Adds Object value for String key in simpleValuesToWrite.
     * @param key to add
     * @param value to add
     */
    public void addSimpleValuesToWrite(String key, Object value) {
        this.simpleValuesToWrite.put(key, value);
    }
}
