package test.de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

/**
 * Represents the output data.
 * 
 * @author Holger Eichelberger, SSE
 */
class OutData {

    private int id;
    private double value1;
    private double value2;
    private double confidence;
    private boolean prediction;
    
    /**
     * Creates an instance.
     * 
     * @param id the id value
     * @param value1 the first value
     * @param value2 the second value
     * @param confidence the scoring confidence
     * @param prediction whether this is a prediction
     */
    OutData(int id, double value1, double value2, double confidence, boolean prediction) {
        this.id = id;
        this.value1 = value1;
        this.value2 = value2;
        this.confidence = confidence;
        this.prediction = prediction;
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

    /**
     * Returns the confidence value.
     * 
     * @return the confidence value
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Returns the prediction value.
     * 
     * @return the prediction value
     */
    public boolean isPrediction() {
        return prediction;
    }

}