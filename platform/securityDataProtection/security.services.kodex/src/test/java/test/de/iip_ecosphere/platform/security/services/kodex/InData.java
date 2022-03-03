package test.de.iip_ecosphere.platform.security.services.kodex;

/**
 * Represents the input data.
 * 
 * @author Holger Eichelberger, SSE
 */
class InData {
    
    private String name;
    private String id;
    
    /**
     * Creates an instance.
     * 
     * @param name the name value
     * @param id the id value
     */
    InData(String name, String id) {
        this.name = name;
        this.id = id;
    }
    
    /**
     * Returns the name value.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id value.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }
    
}