package de.iip_ecosphere.platform.examples.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TestServerResponseMeasurementSetItem {

    private String href;
    private String id;
    private String name;
    private Object value;
    private String unit;
    private String description;
    
    /**
     * Constructor.
     */
    public TestServerResponseMeasurementSetItem() {
        
    }
    
    /**
     * Constructor. Only needed for the TestServer -> Must not be generated.
     * 
     * @param value = Instance of TestServerValueMeasurement
     */
    @JsonIgnore
    public TestServerResponseMeasurementSetItem(TestServerValueMeasurement value) {
        this.id = value.getId();
        this.name = value.getName();
        this.value = value.getValue();
        this.unit = value.getUnit();
        this.description = value.getDescription();
    }
    
    /**
     * Getter for href.
     * 
     * @return href
     */
    public String getHref() {
        return href;
    }
    
    /**
     * Setter for href.
     * 
     * @param href to set
     */
    public void setHref(String href) {
        this.href = href;
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


}
