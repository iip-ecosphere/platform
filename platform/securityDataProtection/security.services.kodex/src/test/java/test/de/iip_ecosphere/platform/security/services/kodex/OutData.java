package test.de.iip_ecosphere.platform.security.services.kodex;

/**
 * Represents the output data.
 * 
 * @author Holger Eichelberger, SSE
 */
class OutData {

    private String kip;
    private String name;
    private String id;
    
    /**
     * Creates an instance.
     *
     * @param kip the kip value introduced by KODEX
     * @param name the name value
     * @param id the id value
     */
    OutData(String kip, String name, String id) {
        this.kip = kip;
        this.name = name;
        this.id = id;
    }

    /**
     * Returns the kip value introduced by KODEX.
     * 
     * @return the kip value
     */
    public String getKip() {
        return kip;
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