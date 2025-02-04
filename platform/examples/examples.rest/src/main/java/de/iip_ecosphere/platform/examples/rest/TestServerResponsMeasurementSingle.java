package de.iip_ecosphere.platform.examples.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.iip_ecosphere.platform.connectors.rest.RESTServerResponse;
import test.de.iip_ecosphere.platform.examples.rest.TestServerValueMeasurement;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestServerResponsMeasurementSingle extends RESTServerResponse {

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

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("description")
    private String description;
    
    /**
     * Constructor.
     */
    public TestServerResponsMeasurementSingle() {
    }
    
    /**
     * Constructor. Only needed for the TestServer -> Must not be generated.
     * 
     * @param value = Instance of TestServerValueMeasurement
     */
    @JsonIgnore
    public TestServerResponsMeasurementSingle(TestServerValueMeasurement value) {
        this.id = value.getId();
        this.name = value.getName();
        this.value = value.getValue();
        this.unit = value.getUnit();
        this.description = value.getDescription();
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
     * Getter for unit.
     * 
     * @return unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Setter for unit.
     * 
     * @param unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
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
    


    @Override
    protected Class<?>[] getItemClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getValueToWrite() {
        return value;
    }

}
