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

package test.de.iip_ecosphere.platform.examples.vdw;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.opcuav1.DataItem;
import de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.examples.vdw.MachineCommand;
import de.iip_ecosphere.platform.examples.vdw.MachineCommandInputTranslator;
import de.iip_ecosphere.platform.examples.vdw.MachineData;
import de.iip_ecosphere.platform.examples.vdw.MachineDataOutputTranslator;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

/**
 * Tests the connector parts/plugins for the VDW OPC UA server.
 * 
 * Plan: Parts of class shall be generated from the configuration model when the connector is used in an application.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OpcUaConnectorTest {
    
    /**
     * Tests the connector parts/plugins for the VDW OPC UA server.
     */
    @Test
    public void testConnector() throws IOException {
        Connector<DataItem, Object, MachineData, MachineCommand> connector = 
            new OpcUaConnector<MachineData, MachineCommand>(
                new TranslatingProtocolAdapter<DataItem, Object, MachineData, MachineCommand>(
                     new MachineDataOutputTranslator<DataItem>(DataItem.class),
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
        ConnectorParameter param = ConnectorParameterBuilder
            .newBuilder("opcua.umati.app", 4840)
            .setNotificationInterval(400) // for monitoring
            .build();
        connector.connect(param);
        connector.request(true);
        TimeUtils.sleep(20000); // model monitoring shall trigger further output
        connector.disconnect();
    }
    
}
