package de.iip_ecosphere.platform.connectors.rest;

import java.util.HashMap;

public class RESTEndpoint {
    
    private String name;
    private String endpoint;
    private boolean asSingleValue;
    private String type;
    private String setType;
    private int endpointIndex;
    
    private HashMap<String, Integer> itemIndexes = new HashMap<>();; 

    /**
     * Getter for endpoint.
     * 
     * @return endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }
    
    /**
     * asd.
     * @return a
     */
    public String getSimpleEndpoint() {
        
        String result = endpoint;
        
        if (endpoint.contains("?") && endpoint.contains("=") ) {
            String str1 = result.split("\\?")[0];
            String str2 = result.split("=")[1];
            
            result = str1 + "/" + str2;
        }
        return result;
    }

    /**
     * Setter for endpoint.
     * 
     * @param endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }



    /**
     * Getter for name.
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name.
     * 
     * @param name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for simpleValueToWrite.
     * 
     * @return simpleValueToWrite
     */
    public boolean getAsSingleValue() {
        return asSingleValue;
    }

    /**
     * Setter for simpleValueToWrite.
     * 
     * @param simpleValueToWrite to set
     */
    public void setAsSingleValue(boolean simpleValueToWrite) {
        this.asSingleValue = simpleValueToWrite;
    }

    /**
     * Getter for endpointIndex.
     * 
     * @return endpointIndex
     */
    public int getEndpointIndex() {
        return endpointIndex;
    }

    /**
     * Setter for endpointIndex.
     * 
     * @param endpointIndex to set
     */
    public void setEndpointIndex(int endpointIndex) {
        this.endpointIndex = endpointIndex;
    }

    /**
     * Getter for type.
     * 
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter for type.
     * 
     * @param type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter for setType.
     * 
     * @returnsetType
     */
    public String getSetType() {
        return setType;
    }

    /**
     * Setter for setType.
     * 
     * @param setType to set
     */
    public void setSetType(String setType) {
        this.setType = setType;
    }

    /**
     * Add index for key to itemIndexes.
     * 
     * @param key to add
     * @param index to add
     */
    public void addItemIndex(String key, int index) {
        itemIndexes.put(key, index);
    }
    
    /**
     * Get ItemIndex for key.
     * 
     * @param key to retreive
     * @return value for key
     */
    public int getItemIndex(String key) {
        return itemIndexes.get(key);
    }
}
