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

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import iip.datatypes.DriveBeckhoffOutput;
import iip.datatypes.DriveCommand;
import iip.datatypes.DriveCommandImpl;
import iip.datatypes.Dummy;
import iip.datatypes.LenzeDriveMeasurement;
import iip.nodes.DriveBeckhoffOPCConnector;
import iip.nodes.LenzeMQTTConnector;

//MAY BE DELETED, NOT PERMANENT !!!

/**
 * Simple, MQTT drive data test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MqttDriveTest {
    
    private static DriveBeckhoffOutput lastOpcData;
    
    private static final short DRIVE_MIN_POS = 300; // 300
    private static final short DRIVE_MAX_POS = 700; // 700

    private static ReceptionCallback<LenzeDriveMeasurement> callbackDrive 
        = new ReceptionCallback<LenzeDriveMeasurement>() {

            @Override
            public void received(LenzeDriveMeasurement data) {
                System.out.println("Drive: " + data);
                System.out.println(org.apache.commons.lang3.builder.ReflectionToStringBuilder.toString(data));
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonUtils.handleIipDataClasses(mapper); // only if nested?
                    mapper.writeValue(new File("./lenze.json"), data);
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
                System.out.println("JSON DONE");
            }
    
            @Override
            public Class<LenzeDriveMeasurement> getType() {
                return LenzeDriveMeasurement.class;
            }
            
        };

    private static ReceptionCallback<DriveBeckhoffOutput> callbackOpc = new ReceptionCallback<DriveBeckhoffOutput>() {

        @Override
        public void received(DriveBeckhoffOutput data) {
            System.out.println("OPC: " + data);
            lastOpcData = data;
        }

        @Override
        public Class<DriveBeckhoffOutput> getType() {
            return DriveBeckhoffOutput.class;
        }
        
    };
    
    /**
     * Starts the test.
     * 
     * @param args ignored
     * @throws IOException if connector creation fails
     */
    public static void main(String[] args) throws IOException {
        /*LenzeMQTTConnectorParserSerializer ps = new LenzeMQTTConnectorParserSerializer("UTF-8", null, () ->"");
        byte[] in = FileUtils.readFileToByteArray(new File("P:\\AP6\\HM23\\Example_Measurement.json"));
        LenzeDriveMeasurement ldm = ps.from(in);
        System.out.println(ldm);
        System.exit(0);*/

        SerializerRegistry.registerSerializer(iip.serializers.CommandImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.CommandSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.LenzeDriveMeasurementSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.LenzeDriveMeasurementImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.LenzeDriveMeasurementChannelSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.LenzeDriveMeasurementChannelImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.LenzeDriveMeasurementProcessSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.LenzeDriveMeasurementProcessImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.DriveCommandSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.DriveCommandImplSerializer.class);
        
        de.iip_ecosphere.platform.connectors.mqttv3.PahoMqttv3Connector<LenzeDriveMeasurement, Dummy> connDrive = 
            new de.iip_ecosphere.platform.connectors.mqttv3.PahoMqttv3Connector<>(
                LenzeMQTTConnector.createConnectorAdapter());
        connDrive.connect(LenzeMQTTConnector.createConnectorParameter());
        connDrive.setReceptionCallback(callbackDrive);
        connDrive.notificationsChanged(false); // force sampling independent of model

        de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<DriveBeckhoffOutput, DriveCommand> connOpc = 
            new de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<>(
                DriveBeckhoffOPCConnector.createConnectorAdapter());
        connOpc.connect(DriveBeckhoffOPCConnector.createConnectorParameter());
        connOpc.setReceptionCallback(callbackOpc);
        connOpc.notificationsChanged(false); // force sampling independent of model

        int driveCommandCount = 0;
        while (true) {
            DriveBeckhoffOutput opcData = lastOpcData; // prevent sudden change
            if (null == opcData) {
                System.out.println("No MQTT data received so far.");
            } else if (opcData.getError()) {
                System.out.println("Drive error: " + opcData.getError());
            } else if (opcData.getBWaitForCommand()) {
                DriveCommand cmd = createDriveCommand(driveCommandCount);
                if (null != cmd) {
                    System.out.println("Sending OPC: " + cmd);
                    connOpc.write(cmd);
                } else {
                    System.out.println("No command specified. Just collecting data.");
                }
                TimeUtils.sleep(1000);
                driveCommandCount++;
            }
            TimeUtils.sleep(500);
        }
    }
    
    /**
     * Creates a drive command depending on {@code count}.
     * 
     * @param count the number of commands issued so far
     * @return the dive command, may by <b>null</b> for none
     */
    private static DriveCommand createDriveCommand(int count) {
        DriveCommand cmd;
        switch (count) {
        case 0:
            cmd = new DriveCommandImpl();
            cmd.setRPosition(DRIVE_MAX_POS);
            cmd.setRVelocity(100);
            cmd.setIFriction_set((short) 0);
            cmd.setITension_set((short) 100);
            cmd.setBExecute(true);
            break;
        case 1:
            cmd = new DriveCommandImpl();
            cmd.setRPosition(DRIVE_MIN_POS);
            cmd.setRVelocity(100);
            cmd.setIFriction_set((short) 0);
            cmd.setITension_set((short) 100);
            cmd.setBExecute(true);
            break;
        case 2:
            cmd = new DriveCommandImpl();
            cmd.setRPosition(DRIVE_MAX_POS);
            cmd.setRVelocity(100);
            cmd.setIFriction_set((short) 0);
            cmd.setITension_set((short) 70);
            cmd.setBExecute(true);
            break;
        case 3:
            cmd = new DriveCommandImpl();
            cmd.setRPosition(DRIVE_MIN_POS);
            cmd.setRVelocity(100);
            cmd.setIFriction_set((short) 0);
            cmd.setITension_set((short) 100);
            cmd.setBExecute(true);
            break;
        default:
            cmd = null;
            break;
        }
        return cmd;
    }
    
}
