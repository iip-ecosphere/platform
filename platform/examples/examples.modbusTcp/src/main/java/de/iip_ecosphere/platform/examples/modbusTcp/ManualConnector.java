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

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusItem;
import de.iip_ecosphere.platform.connectors.modbustcpipv1.ModbusTcpIpConnector;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.nodes.MyModbusConnExample;

/**
 * Simple app to run the connector.
 *
 * @author Holger Eichelberger, SSE
 */
public class ManualConnector {
    
    /**
     * Executes the connector.
     * 
     * @param args ignored
     * @throws IOException if the connector fails (preliminary)
     */
    public static void main(String... args) throws IOException {
        ActiveAasBase.setNotificationMode(NotificationMode.NONE); // disable AAS connector registration
        Connector<ModbusItem, Object, MachineData, MachineCommand> connector = 
            new ModbusTcpIpConnector<MachineData, MachineCommand>(
                new TranslatingProtocolAdapter<ModbusItem, Object, MachineData, MachineCommand>(
                     new MachineDataOutputTranslator<ModbusItem>(ModbusItem.class),
                     new MachineCommandInputTranslator<Object>(Object.class)));
        connector.setReceptionCallback(new ReceptionCallback<MachineData>() {

            @Override
            public void received(MachineData data) {
                System.out.println("RECEIVED " + data);
            }

            @Override
            public Class<MachineData> getType() {
                return MachineData.class;
            }
            
        });
        
        ConnectorParameter param = MyModbusConnExample.createConnectorParameter();
        connector.connect(param);
        connector.request(true);
        TimeUtils.sleep(20000); // model monitoring shall trigger further output
        connector.disconnect();    
        System.exit(0);
    }
    
}
