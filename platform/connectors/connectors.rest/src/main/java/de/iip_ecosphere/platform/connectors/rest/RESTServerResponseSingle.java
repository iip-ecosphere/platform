package de.iip_ecosphere.platform.connectors.rest;

public  class RESTServerResponseSingle extends RESTServerResponse {

    private RESTServerResponseValue value;

    /**
     * Getter for value.
     * 
     * @return RESTServerResponseValue value
     */
    public RESTServerResponseValue getValue() {
        return value;
    }

    /**
     * Setter for value.
     * 
     * @param value to set
     */
    public void setValue(RESTServerResponseValue value) {
        this.value = value;
    }
}
