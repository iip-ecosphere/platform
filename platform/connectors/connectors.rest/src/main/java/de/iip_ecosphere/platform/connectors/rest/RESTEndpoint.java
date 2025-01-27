package de.iip_ecosphere.platform.connectors.rest;

public class RESTEndpoint {
    
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
    
    

}
