package de.iip_ecosphere.platform.support.json;

/**
 * Represents a String. Abstracted from javax.json (J2EE) and Jersey.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface JsonString extends JsonValue {

    /**
     * Returns the JSON string value.
     * 
     * @return a JSON string value
     */
    public String getString();

}
