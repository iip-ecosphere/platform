/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.modbusTcp;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusItem;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;

/**
 * Simple app to run the connector.
 *
 * @author Holger Eichelberger, SSE
 */
public class ManualConnector {

    /**
     * Creates a ManualConnector instance and returns it.
     * 
     * @return  a ManualConnector instance
     * 
     */
    public static Connector<ModbusItem, Object, ModbusMachineData, ModbusMachineCommand> createConnector() {
     
        Connector<ModbusItem, Object, ModbusMachineData, ModbusMachineCommand> connector =
                new ModbusTcpIpConnector<ModbusMachineData, ModbusMachineCommand>(
                new TranslatingProtocolAdapter<ModbusItem, Object, ModbusMachineData, ModbusMachineCommand>(
                        new ModbusMachineDataOutputTranslator<ModbusItem>(false, ModbusItem.class),
                        new ModbusMachineCommandInputTranslator<Object>(Object.class))); 
        
        return connector;
    }
    
    /**
     * Executes the connector.
     * 
     * @param args ignored
     * @throws IOException if the connector fails (preliminary)
     */
    /*
    public static void main(String... args) throws IOException {
        
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration

        AtomicReference<ModbusMachineData> md = new AtomicReference<ModbusMachineData>();

        Connector<ModbusItem, Object, ModbusMachineData, ModbusMachineCommand> connector = 
                new ModbusTcpIpConnector<ModbusMachineData, ModbusMachineCommand>(
                new TranslatingProtocolAdapter<ModbusItem, Object, ModbusMachineData, ModbusMachineCommand>(
                        new ModbusMachineDataOutputTranslator<ModbusItem>(false, ModbusItem.class),
                        new ModbusMachineCommandInputTranslator<Object>(Object.class)));

        connector.setReceptionCallback(new ReceptionCallback<ModbusMachineData>() {

            @Override
            public void received(ModbusMachineData data) {
                System.out.println("RECEIVED " + data);
                md.set(data);
            }

            @Override
            public Class<ModbusMachineData> getType() {
                return ModbusMachineData.class;
            }

        });

        connector.connect(MyModbusConnExample.createConnectorParameter());
        connector.request(true);
        // TimeUtils.sleep(20000); // model monitoring shall trigger further output

        ModbusMachineData tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((int) 0, tmp.getValue("Data"));
        Assert.assertEquals((int) 0, tmp.getValue("I1"));
        Assert.assertEquals((int) 0, tmp.getValue("S1"));
        Assert.assertEquals((int) 0, tmp.getValue("V1"));

        // Set values
        ModbusMachineCommand cmd = new ModbusMachineCommand();
        cmd.set("Data", 1);
        cmd.set("I1", 9999);
        cmd.set("S1", 123456789);
        cmd.set("V1", -512);
        connector.write(cmd);

        tmp = md.get();

        // Check the values set before
        Assert.assertEquals((int) 1, tmp.getValue("Data"));
        Assert.assertEquals((int) 9999, tmp.getValue("I1"));
        Assert.assertEquals((int) 123456789, tmp.getValue("S1"));
        Assert.assertEquals((int) -512, tmp.getValue("V1"));

        // Set values back to 0
        cmd = new ModbusMachineCommand();
        cmd.set("Data", (int) 0);
        cmd.set("I1", (int) 0);
        cmd.set("S1", (int) 0);
        cmd.set("V1", (int) 0);
        connector.write(cmd);
        
        tmp = md.get();

        // Check if the values are 0
        Assert.assertEquals((int) 0, tmp.getValue("Data"));
        Assert.assertEquals((int) 0, tmp.getValue("I1"));
        Assert.assertEquals((int) 0, tmp.getValue("S1"));
        Assert.assertEquals((int) 0, tmp.getValue("V1"));

        connector.disconnect();
        // System.exit(0);
         
         
    }
*/
}
