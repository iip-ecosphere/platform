package de.iip_ecosphere.platform.connectors.modbustcpipv1;

import java.util.HashMap;

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusVarItem.ModbusVarItemType;

/**
 * This Class maps the ModbusKeys to the corresponding ModbusVarItem.
 * 
 * @author Christian Nikolajew
 *
 */
public class ModbusMap extends HashMap<String, ModbusVarItem> {

    private static final long serialVersionUID = -8592139232187722746L;
    
    
    /**
     * Constructor.
     */
    public ModbusMap() {
        
        String[] keys = ModbusKeys.getKeys();
        
        this.put(keys[0] , new ModbusVarItem(ModbusVarItemType.Short, 0));
        this.put(keys[1] , new ModbusVarItem(ModbusVarItemType.Integer, 1));
        this.put(keys[2] , new ModbusVarItem(ModbusVarItemType.Float, 3));
        this.put(keys[3] , new ModbusVarItem(ModbusVarItemType.Long, 5));
        this.put(keys[4] , new ModbusVarItem(ModbusVarItemType.Double, 9));      
        this.put(keys[5] , new ModbusVarItem(ModbusVarItemType.Short, 10)); 
        
    }
}
