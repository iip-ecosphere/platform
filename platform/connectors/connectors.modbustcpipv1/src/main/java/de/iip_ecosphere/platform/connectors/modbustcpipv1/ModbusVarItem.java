package de.iip_ecosphere.platform.connectors.modbustcpipv1;

/**
 * This Class stores type an offset of a variable.
 * 
 * @author Christian Nikolajew
 *
 */
public class ModbusVarItem {
    
    private String type;
    private int offset;

    
    /**
     * Setter for type.
     * 
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Setter for offset.
     * 
     * @param offset the offset to set
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }
    /**
     * Getter for type.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for offset.
     * 
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }
    

    /**
     * Returs the count of registers needed to store this type.
     * 
     * @return the count of registers needed to store this type
     */
    public int getTypeRegisterSize() {
        
        int result = 0;
        
        if (type.equals("short") || type.equals("dword")) {
            result = 1;
        } else if (type.equals("integer")) {
            result = 2;
        } else if (type.equals("float")) {
            result = 2;
        } else if (type.equals("long")) {
            result = 4;
        } else if (type.equals("double")) {
            result = 4;
        } /*else if (type.equals("dword")) {
            result = 1;
        }*/
        
        return result;
    }

}
