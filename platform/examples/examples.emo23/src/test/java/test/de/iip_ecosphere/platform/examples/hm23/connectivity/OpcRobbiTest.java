/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.examples.hm23.connectivity;

import java.io.IOException;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import iip.datatypes.PlcInput;
import iip.datatypes.PlcInputImpl;
import iip.datatypes.PlcOutput;
import iip.datatypes.PlcOutputImpl;
import iip.nodes.PLCnextOPCConnector;

//MAY BE DELETED, NOT PERMANENT !!!

/**
 * Simple, fixed two robots test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OpcRobbiTest {

    private static final String ROBBY1_OPC_PATH = "Objects/PLCnext/Arp.Plc.Eclr/RobInstance01/";
    private static final String ROBBY2_OPC_PATH = "Objects/PLCnext/Arp.Plc.Eclr/RobInstance02/";
    private static PlcOutput robbi1 = new PlcOutputImpl();
    private static PlcOutput robbi2 = new PlcOutputImpl();
    
    private static ReceptionCallback<PlcOutput> callbackR1 = new ReceptionCallback<PlcOutput>() {

        @Override
        public void received(PlcOutput data) {
            System.out.println("Robbi1: " + data);
            robbi1 = data;
        }

        @Override
        public Class<PlcOutput> getType() {
            return PlcOutput.class;
        }
        
    };

    private static ReceptionCallback<PlcOutput> callbackR2 = new ReceptionCallback<PlcOutput>() {

        @Override
        public void received(PlcOutput data) {
            System.out.println("Robbi2: " + data);
            robbi2 = data;
        }

        @Override
        public Class<PlcOutput> getType() {
            return PlcOutput.class;
        }
        
    };
    
    /**
     * Starts the test.
     * 
     * @param args ignored
     * @throws IOException if connector creation fails
     */
    public static void main(String[] args) throws IOException {
        SerializerRegistry.registerSerializer(iip.serializers.CommandImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.CommandSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.PlcOutputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.PlcOutputSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.PlcInputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.PlcInputSerializer.class);
        
        System.out.println("Robbis may require a QUIT via HMI. Program does not sent PC_QUIT so far.");

        de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<PlcOutput, PlcInput> connR1 = 
            new de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<>(
                PLCnextOPCConnector.createConnectorAdapter(() -> ROBBY1_OPC_PATH, () -> ROBBY1_OPC_PATH));
        connR1.connect(PLCnextOPCConnector.createConnectorParameter());
        connR1.setReceptionCallback(callbackR1);
        connR1.notificationsChanged(false); // force sampling independent of model

        de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<PlcOutput, PlcInput> connR2 = 
            new de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<>(
                PLCnextOPCConnector.createConnectorAdapter(() -> ROBBY2_OPC_PATH, () -> ROBBY2_OPC_PATH));
        connR2.connect(PLCnextOPCConnector.createConnectorParameter());
        connR2.setReceptionCallback(callbackR2);
        connR2.notificationsChanged(false); // force sampling independent of model

        int robbi1State = 0;
        int robbi2State = 0;
        
        while (true) {
            if (!robbi1.getUR_BusyOperating()) {
                if (robbi1State == 0 && robbi1.getPC_ReadyForRequest()) { // usual start
                    PlcInput pi = new PlcInputImpl();
                    //pi.setPC_RequestedOperation(1);
                    pi.setPC_StartOperation(true);
                    System.out.println("To Robbi1 (0): " + pi);
                    connR1.write(pi);
                    robbi1State++;
                } else if (robbi1State == 1 || robbi1State == 2) { // advance cam position
                    PlcInput pi = new PlcInputImpl();
                    pi.setPC_Command01((short) 100);
                    System.out.println("To Robbi1 (1/2): " + pi);
                    connR1.write(pi);
                    robbi1State++;
                } else {
                    PlcInput pi = new PlcInputImpl();
                    pi.setPC_Command01((short) 100);
                    System.out.println("To Robbi1 (else): " + pi);
                    connR1.write(pi);
                    robbi1State = 0; // reset
                }
            }

            if (!robbi2.getUR_BusyOperating()) {
                if (robbi2State == 0 && robbi2.getPC_ReadyForRequest()) { // usual start
                    PlcInput pi = new PlcInputImpl();
                    //pi.setPC_RequestedOperation(1);
                    pi.setPC_StartOperation(true);
                    System.out.println("To Robbi2 (0): " + pi);
                    connR2.write(pi);
                    robbi2State++;
                } else if (robbi2State == 1 || robbi2State == 2) { // advance cam position
                    PlcInput pi = new PlcInputImpl();
                    pi.setPC_Command01((short) 100);
                    System.out.println("To Robbi2 (1/2): " + pi);
                    connR2.write(pi);
                    robbi2State++;
                } else {                    
                    PlcInput pi = new PlcInputImpl();
                    pi.setPC_Command01((short) 100);
                    System.out.println("To Robbi2 (else): " + pi);
                    connR2.write(pi);
                    robbi2State = 0; // reset
                }
            }
            connR1.request(true);
            connR2.request(true);
            TimeUtils.sleep(1000);
        }
    }
    
}
