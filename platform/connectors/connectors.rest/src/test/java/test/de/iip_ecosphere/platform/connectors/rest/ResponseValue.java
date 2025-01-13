package test.de.iip_ecosphere.platform.connectors.rest;

import de.iip_ecosphere.platform.connectors.rest.RESTServerResponseValue;

public class ResponseValue extends RESTServerResponseValue {

    private String endpointPath;
    private String timestamp;
    
    /**
     * Getter for endpointPath.
     * 
     * @return endpointPath
     */
    public String getEndpointPath() {
        return endpointPath;
    }

    /**
     * Setter for endpointPath.
     * 
     * @param endpointPath = endpointPath to set
     */
    public void setEndpointPath(String endpointPath) {
        this.endpointPath = endpointPath;
    }

    /**
     * Getter for timestamp.
     * 
     * @return timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Setter for timestamp.
     * 
     * @param timestamp = timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Object get(String key) {

        Object result = null;
        
        if (key.equals("path")) {
            result = endpointPath;
        } else if (key.equals("time")) {
            result = timestamp;
        }
        
        return result;
    }

}
