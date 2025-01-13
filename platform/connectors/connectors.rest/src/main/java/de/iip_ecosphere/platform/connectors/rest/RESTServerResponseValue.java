package de.iip_ecosphere.platform.connectors.rest;

public abstract class RESTServerResponseValue {

    private String name;
    private Object value;
    
    /**
     * Getter for value.
     * 
     * @return value
     */
    public Object getValue() {       
        return   value;
    }

    /**
     * Setter for value.
     * 
     * @param value to set
     */
    public void setValue(Object value) {
        this.value = value;
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
     * Get other Setting from a given key.
     * 
     * @param key to retrieve
     * @return Object stored for key
     */
    public abstract Object get(String key);
}
