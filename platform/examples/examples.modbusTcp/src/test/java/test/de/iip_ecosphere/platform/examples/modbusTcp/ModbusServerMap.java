package test.de.iip_ecosphere.platform.examples.modbusTcp;

import java.util.ArrayList;
import java.util.HashMap;

import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusServerVariable;

public class ModbusServerMap extends HashMap<String, ModbusServerVariable> {

    private static final long serialVersionUID = -8041174891184167499L;

    /**
     * Craetes an instance of ModbusServerMap.
     * 
     * @param layout the ModbusServerLayout
     */
    public ModbusServerMap(ModbusServerLayout layout) {

        ArrayList<ModbusServerVariable> vars = layout.getVars();

        for (ModbusServerVariable var : vars) {

            this.put(var.getKey(), new ModbusServerVariable(var.getKey(), var.getOffset(), var.getType()));
        }
    }

}
