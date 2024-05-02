package test.de.iip_ecosphere.platform.examples.modbusTcp;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusServerVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Layout for ModbusServer.
 * 
 * @author Christian Nikolajew
 *
 */
public class ModbusServerLayout {

    private ArrayList<ModbusServerVariable> vars;

    private int registerCount;

    /**
     * Creates an instance.
     * 
     * @param param the ConnectorParameter
     */
    public ModbusServerLayout(ConnectorParameter param) {

        vars = new ArrayList<ModbusServerVariable>();
        registerCount = 0;

        Set<String> keys = param.getSpecificSettingKeys();

        for (String key : keys) {

            String o = (String) param.getSpecificSetting(key);

            List<String> lines = o.lines().collect(Collectors.toList());

            for (String line : lines) {

                if (line.length() > 2) {

                    String lineKey = getKey(line);
                    int offset = getOffset(line);
                    String type = getType(line);

                    ModbusServerVariable var = new ModbusServerVariable(lineKey, offset, type);
                    vars.add(var);
                }
            }
        }

        registerCount();

    }

    /**
     * Seperates the key from a given line and returns it.
     * 
     * @param line as String in JSON format
     * @return the key as String
     */
    private String getKey(String line) {

        line = line.replaceAll("\"", "");

        int first = line.indexOf(":");

        String key = line.substring(0, first);

        key = key.replaceAll("\\s", "");

        return key;

    }

    /**
     * Seperates the offset from a given line and returns it.
     * 
     * @param line as String in JSON format
     * @return the offset as int
     */
    private int getOffset(String line) {

        line = line.replaceAll("\"", "");
        line = line.replaceFirst(":", "");

        int first = line.indexOf(":");
        int second = line.indexOf(",");

        String strOffset = line.substring(first + 1, second);
        strOffset = strOffset.replaceAll("\\s", "");

        int offset = Integer.valueOf(strOffset);

        return offset;
    }

    /**
     * Seperates the type from a given line and returns it.
     * 
     * @param line as String in JSON format
     * @return the type as String
     */
    private String getType(String line) {

        line = line.replaceAll("\"", "");
        line = line.replaceFirst(":", "");
        line = line.replaceFirst(":", "");

        int first = line.indexOf(":");
        int second = line.indexOf("}");

        String type = line.substring(first + 1, second);
        type = type.replaceAll("\\s", "");

        return type;
    }

    /**
     * Calculates the amount of Registers that the Server needs to store all
     * ModbusServerVariable in vars and stores the result in registerCount.
     */
    private void registerCount() {

        int maxOffset = 0;

        for (ModbusServerVariable var : vars) {

            if (var.getOffset() > maxOffset) {
                maxOffset = var.getOffset();
            }
        }

        maxOffset += 4;

        registerCount = maxOffset;

    }

    /**
     * Returns the registerCount.
     * 
     * @return the registerCount
     */
    public int getRegisterCount() {
        return registerCount;
    }

    /**
     * Returns vars.
     * 
     * @return ArrayList<ModbusServerVariable> vars
     */
    public ArrayList<ModbusServerVariable> getVars() {
        return vars;
    }
}
