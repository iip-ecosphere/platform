package de.iip_ecosphere.platform.examples.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestServerResponseTariffNumberRestType {

    @JsonProperty("context")
    private String context;

    @JsonProperty("id")
    private String id;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("name")
    private String name;

    @JsonProperty("value")
    private Object value;

    @JsonProperty("description")
    private String description;
    
    /**
     * Constructor.
     */
    public TestServerResponseTariffNumberRestType() {
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
     * Getter for id.
     * 
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for id.
     * 
     * @param id to set
     */
    public void setId(String id) {
        this.id = id;
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
     * Getter for value.
     * 
     * @return value
     */
    public Object getValue() {
        return value;
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
     * Getter for description.
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for description.
     * 
     * @param description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
