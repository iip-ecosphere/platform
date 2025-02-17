package de.iip_ecosphere.platform.examples.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestServerResponseInformation {

    @JsonProperty("context")
    private String context;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("rootItems")
    private TestServerResponseInformationRootItem[] rootItems;
    
    @JsonProperty("infoItems")
    private TestServerResponseInformationInfoItem[] infoItems;
    
    /**
     * Constructor.
     */
    public TestServerResponseInformation() {
        
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
     * Getter for rootItems.
     * 
     * @return rootItems;
     */
    public TestServerResponseInformationRootItem[] getRootItems() {
        return rootItems;
    }
    
    /**
     * Setter for rootItems.
     * 
     * @param rootItems to set
     */
    public void setRootItems(TestServerResponseInformationRootItem[] rootItems) {
        this.rootItems = rootItems;
    }
    
    /**
     * Getter for infoItems.
     * 
     * @return infoItems;
     */
    public TestServerResponseInformationInfoItem[] getInfoItems() {
        return infoItems;
    }
    
    /**
     * Setter for infoItems.
     * 
     * @param infoItems to set
     */
    public void setInfoItems(TestServerResponseInformationInfoItem[] infoItems) {
        this.infoItems = infoItems;
    }
}
