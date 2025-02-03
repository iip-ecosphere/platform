package test.de.iip_ecosphere.platform.examples.rest;

/**
 * Only needed for the TestServer.
 */
public class TestServerValueMeasurement {

    private String id;
    private String name;
    private Object value;
    private String unit;
    private String description;
    
    /**
     * Constructor.
     */
    public TestServerValueMeasurement() {
        
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
     * @param nameto set
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
