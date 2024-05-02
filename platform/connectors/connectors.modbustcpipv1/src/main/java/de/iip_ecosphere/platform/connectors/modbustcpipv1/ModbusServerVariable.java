package de.iip_ecosphere.platform.connectors.modbustcpipv1;

public class ModbusServerVariable {

    private String key;
    private int offset;
    private String type;

    /**
     * Creates an instance with given key, offset and type.
     * 
     * @param key for the ModbusServerVariable
     * @param offset of the ModbusServerVariable
     * @param type of the ModbusServerVariable
     */
    public ModbusServerVariable(String key, int offset, String type) {

        this.key = key;
        this.offset = offset;
        this.type = type;
    }

    /**
     * Returns the key.
     * 
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the offset.
     * 
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Returns the type.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the amount of 16-Bit registers needed to store the type.
     * 
     * @return amount of 16-Bit registers needed to store the type
     */
    public int getTypeRegisterSize() {

        int result = -1;

        if (type.equals("integer")) {
            result = 2;
        } else if (type.equals("dword")) {
            result = 2;
        }

        return result;
    }
}
