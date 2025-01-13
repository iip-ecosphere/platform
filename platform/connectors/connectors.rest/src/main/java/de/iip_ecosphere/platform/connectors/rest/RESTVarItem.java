package de.iip_ecosphere.platform.connectors.rest;

public class RESTVarItem {

    private Type type;
    private String endpoint;
    
    /**
     * Constructor.
     */
    public RESTVarItem() {

    }

    /**
     * Constructor.
     * 
     * @param type for RESTVarItem
     * @param endpoint for RESTVarItem
     */
    public RESTVarItem(Type type, String endpoint) {
        
        this.setType(type);
        this.setEndpoint(endpoint);
    }

    /**
     * Getter for type.
     * 
     * @return type
     */
    public Type getType() {
        return type;
    }

    /**
     * Setter for type.
     * 
     * @param type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

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
}
