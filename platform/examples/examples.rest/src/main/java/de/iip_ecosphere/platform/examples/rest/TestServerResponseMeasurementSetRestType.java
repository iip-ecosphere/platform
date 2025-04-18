package de.iip_ecosphere.platform.examples.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestServerResponseMeasurementSetRestType {

    @JsonProperty("context")
    private String context;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("items")
    private TestServerResponseMeasurementSetItem[] items;
    
    /**
     * Constructor.
     */
    public TestServerResponseMeasurementSetRestType() {
        
    }
    
    /**
     * Getter for context.
     * 
     * @return context
     */
    public String getContext() {
        return context;
    }

    /**
     * Setter for context.
     * 
     * @param context to set
     */
    public void setContext(String context) {
        this.context = context;
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
     * @param timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Getter for items.
     * 
     * @return items;
     */
    public TestServerResponseMeasurementSetItem[] getItems() {
        return items;
    }
    
    /**
     * Setter for items.
     * 
     * @param items to set
     */
    public void setItems(TestServerResponseMeasurementSetItem[] items) {
        this.items = items;
    }
}
