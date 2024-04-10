package de.iip_ecosphere.platform.connectors.modbustcpipv1;

/**
 * This Class stores type an offset of a variable.
 * 
 * @author Christian Nikolajew
 *
 */
public class ModbusVarItem {
    
    private ModbusVarItemType type;
    private int offset;
    
    /**
     * Konstruktor.
     * 
     * @param type of Item (Short, Integer, Float, Long, Double)
     * @param offset of Item
     */
    public ModbusVarItem(ModbusVarItemType type, int offset) {
        
        this.type = type;
        this.offset = offset;
    }
    
    /**
     * Getter for type.
     * 
     * @return the type
     */
    public ModbusVarItemType getType() {
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
     * Different types for the ModbusVarItem.
     * 
     * @author Christian Nikolajew
     *
     */
    public enum ModbusVarItemType {
       Short,
       Integer,
       Float,
       Long,
       Double
    }

}
