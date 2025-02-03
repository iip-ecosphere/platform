package de.iip_ecosphere.platform.connectors.rest;

public class RESTEndpoint {
    
    private String name;
    private String endpoint;
    private int responseTypeIndex;

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
     * Getter for responseTypeIndex.
     * 
     * @return responseTypeIndex
     */
    public int getResponseTypeIndex() {
        return responseTypeIndex;
    }

    /**
     * Setter for responseTypeIndex.
     * 
     * @param responseTypeIndex to set
     */
    public void setResponseTypeIndex(int responseTypeIndex) {
        this.responseTypeIndex = responseTypeIndex;
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
    
    

}
