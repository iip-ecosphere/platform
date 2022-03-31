package test.de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

/**
 * Represents the input data.
 * 
 * @author Holger Eichelberger, SSE
 */
class InData {
    
    private int id;
    private double value1;
    private double value2;
    
    /**
     * Creates an instance.
     * 
     * @param id the id value
     * @param value1 the first value
     * @param value2 the second value
     */
    InData(int id, double value1, double value2) {
        this.id = id;
        this.value1 = value1;
        this.value2 = value2;
    }
    
    /**
     * Returns the id value.
     * 
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the first value.
     * 
     * @return the first value
     */
    public double getValue1() {
        return value1;
    }

    /**
     * Returns the second value.
     * 
     * @return the second value
     */
    public double getValue2() {
        return value2;
    }

}