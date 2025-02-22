package de.iip_ecosphere.platform.connectors.rest;

import java.util.HashMap;



public class RESTItem {
    
    private RESTEndpointMap endpointMap;
    private HashMap <String, Object> values;
    private HashMap <String, Object> keysToWrite;
    
    /**
     * Constructor.
     */
    public RESTItem(RESTEndpointMap endpointMap) {
        
        this.endpointMap = endpointMap;
        values = new HashMap<String, Object>();
        keysToWrite = new HashMap<String, Object>();
        
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
     * Get keysToWrite.
     * 
     * @return keysToWrite
     */
    public HashMap <String, Object>  getKeysToWrite() {
        return keysToWrite;
    }

    /**
     * Adds Object value for String key in simpleValuesToWrite.
     * @param key to add
     * @param value to add
     */
    public void addKeyToWrite(String key, Object value) {
        this.keysToWrite.put(key, value);
    }
    
    /**
     * Get size of keysToWrite.
     * 
     * @return size of keysToWrite
     */
    public int getKeysToWriteSize() {
        return keysToWrite.size();
    }
}
