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

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorFactory;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.events.ConnectorInputHandler;
import de.iip_ecosphere.platform.examples.hm23.Commands;
import de.iip_ecosphere.platform.examples.hm23.MdzhInputHandler;
import de.iip_ecosphere.platform.examples.hm23.carAas.CarsAasServer;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import iip.datatypes.Command;
import iip.datatypes.MdzhInput;
import iip.datatypes.MdzhOutput;
import iip.nodes.MDZHAASConnector;

/**
 * Tests the car AAS server/connector. Overrides connector information for local testing! Requires 
 * flowTest=false.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CarAasTest {

    /**
     * Tests the car AAS server/connector.
     * 
     * @param args ignored
     * @throws IOException shall not occur
     */
    public static void main(String[] args) throws IOException {
        final String host = "localhost";
        final int port = 9989;
        CarsAasServer server = new CarsAasServer(new String[] {"--host", host, "--port", String.valueOf(port)});
        server.start();
        
        ConnectorParameter param = ConnectorParameterBuilder.newBuilder(
            MDZHAASConnector.createConnectorParameter(), host, port, null)
            .setEndpointPath("http://" + host + ":" + port + "/registry").build();
        Connector<Object, Object, MdzhOutput, MdzhInput> conn = ConnectorFactory.createConnector(
            "de.iip_ecosphere.platform.connectors.aas.AasConnector", () -> param, 
            MDZHAASConnector.createConnectorAdapter(()->"", ()->"ProductData/"));
        conn.setReceptionCallback(new ReceptionCallback<MdzhOutput>() {
            
            @Override
            public void received(MdzhOutput data) {
                System.out.println("RECEIVED " + data);
            }
            
            @Override
            public Class<MdzhOutput> getType() {
                return MdzhOutput.class;
            }
        });
        conn.connect(param);
        
        TimeUtils.sleep(1000);

        ConnectorInputHandler<Command> commandHandler = new MdzhInputHandler();
        Command cmd = Commands.createCarQueryCommand(Commands.DRIVE_QUERY_CAR_AAS, "5012");
        String newCls = commandHandler.getNewConnectorClass(cmd);
        if (null != newCls && newCls.length() > 0) {
            System.out.println("Expected runtime change of connector  to " + newCls);
        }
        commandHandler.received(cmd, conn);
        
        TimeUtils.sleep(1000);

        conn.disconnect();
        server.stop(true);
    }

}
