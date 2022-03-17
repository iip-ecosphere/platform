package de.iip_ecosphere.platform.monitoring.prometheus;

/** TestObject for testing purposes.
 * 
 * @author const
 *
 */
public class TestObject {
    private String description;
    private int value;

    /** Constructor.
     * 
     * @param description
     * @param value
     */
    public TestObject(String description, int value) {
        this.description = description;
        this.value = value;
    }
    
    /** Getter for the Description.
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }
    
    /** Getter for the Value.
     * 
     * @return value
     */
    public int getValue() {
        return value;
    }
}
