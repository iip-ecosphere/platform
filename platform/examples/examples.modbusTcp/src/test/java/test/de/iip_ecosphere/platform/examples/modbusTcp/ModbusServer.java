package test.de.iip_ecosphere.platform.examples.modbusTcp;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusMap;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusVarItem;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import iip.nodes.MyModbusConnExample;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

/**
 * Server for the ModbusTcp example.
 * 
 * @author Christian Nikolajew
 *
 */
public class ModbusServer {

    private ModbusTCPListener listener = null;
    private InetAddress host;
    private int port;
    private ModbusMap map = null;

    /**
     * Creates an ModbusServer instance.
     */
    public ModbusServer() {

        ConnectorParameter param = MyModbusConnExample.createConnectorParameter();
        
        Set<String> keys = param.getSpecificSettingKeys();

        for (String key : keys) {
            
            if (key.equals("SERVER_STRUCTURE")) {
             
                Object serverSettings = param.getSpecificSetting(key);
                map = JsonUtils.fromJson(serverSettings, ModbusMap.class);
                
            } 
        }
        
        if (map == null) {
            System.out.println("ModbusServer -> No SERVER_STRUCTURE found");
        } 

        SimpleProcessImage spi = null;
        spi = new SimpleProcessImage();
        
        int registerCount = getRegisterCount();

        for (int i = 0; i < registerCount; i++) {
            spi.addRegister(new SimpleRegister(0));
        }

        ModbusCoupler.getReference().setProcessImage(spi);
        ModbusCoupler.getReference().setMaster(false);
        ModbusCoupler.getReference().setUnitID(1);

        listener = new ModbusTCPListener(3);
        port = param.getPort();
        listener.setPort(port);

        try {

            host = InetAddress.getByName(param.getHost());
            listener.setAddress(host);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
 
    }

    /**
     * Starts the server.
     */
    public void start() {

        if (listener != null) {
            listener.start();
        }

    }
    
    /**
     * Shuts down the server.
     */
    public void stop() {

        if (listener != null) {
            listener.stop();
        }

    }

    /**
     * Returns the ModbusServerMap.
     * 
     * @return the ModbusServerMap
     */
    public ModbusMap getServerMap() {
        return map;
    }
    
    /**
     * Calculates the count of Registers needed to store the ModbusServerMap
     * and returs it.
     * 
     * @return the count of Registers needed to store the ModbusServerMap
     */
    public int getRegisterCount() {
        
        int maxOffset = 0;
        
        for (ModbusMap.Entry<String, ModbusVarItem> entry : map.entrySet()) {

            ModbusVarItem value = entry.getValue();
            
            if (value.getOffset() > maxOffset) {
                maxOffset = value.getOffset();
            }

        }
        
        maxOffset += 4;
        
        System.out.println("ServerRegisterCount: " + maxOffset);
        
        return maxOffset;
        
    }
}
