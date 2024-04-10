package de.iip_ecosphere.platform.connectors.modbustcpipv1;

/**
 * This Class holds the keys to identify variables.
 * 
 * @author Christian Nikolajew
 *
 */
public class ModbusKeys {

    /**
     * Keys.
     */
    private static String[] keys = {"short1", "integer1", "float1", "long1", "double1", "short2"};

    /**
     * Returns the keys.
     * 
     * @return the keys
     */
    public static String[] getKeys() {
        return keys;
    }

}
