package de.iip_ecosphere.platform.connectors.rest;

import java.util.HashMap;



public class RESTItem {
    
    private HashMap <String, Object> values;
    
    /**
     * Constructor.
     */
    public RESTItem(RESTMap map) {
        values = new HashMap<String, Object>();
        
        for (RESTMap.Entry<String, RESTVarItem> entry : map.entrySet()) {
            
            values.put(entry.getKey(), 0);
            
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
}
