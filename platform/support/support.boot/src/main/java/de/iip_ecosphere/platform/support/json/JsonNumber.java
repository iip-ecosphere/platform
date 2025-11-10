package de.iip_ecosphere.platform.support.json;

/**
 * Represents a number. Abstracted from javax.json (J2EE) and Jersey.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface JsonNumber extends JsonValue {

    /**
     * Returns this JSON number as a double. Note that this conversion can lose information about the overall magnitude 
     * and precision of the number value as well as return a result with the opposite sign.
     * 
     * @return  a double representation of the JSON number.
     */
    public double doubleValue();

    /**
     * Returns this JSON number as an int. Note that this conversion can lose information about the overall magnitude 
     * and precision of the number value as well as return a result with the opposite sign.
     * 
     * @return  a int representation of the JSON number.
     */
    public int intValue();
    
    /**
     * Returns this JSON number as a long. Note that this conversion can lose information about the overall magnitude 
     * and precision of the number value as well as return a result with the opposite sign.
     * 
     * @return  a long representation of the JSON number.
     */
    public long longValue();

}
