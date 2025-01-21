package de.iip_ecosphere.platform.connectors.rest;

import java.util.HashMap;



public class RESTItem {
    
    private RESTEndpointMap endpointMap;
    private HashMap <String, RESTServerResponse> values;
    
    /**
     * Constructor.
     */
    public RESTItem(RESTEndpointMap endpointMap) {
        
        this.endpointMap = endpointMap;
        values = new HashMap<String, RESTServerResponse>();
        
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
    public void setValue(String key, RESTServerResponse value) {
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
}
