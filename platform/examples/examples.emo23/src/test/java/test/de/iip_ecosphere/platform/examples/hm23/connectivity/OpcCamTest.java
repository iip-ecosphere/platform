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
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.examples.hm23.CamSource;
import de.iip_ecosphere.platform.examples.hm23.Commands;
import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import iip.datatypes.BeckhoffInput;
import iip.datatypes.BeckhoffInputImpl;
import iip.datatypes.BeckhoffOutput;
import iip.datatypes.Command;
import iip.datatypes.CommandImpl;
import iip.datatypes.ImageInput;
import iip.nodes.BeckhoffOPCConnector;

// MAY BE DELETED, NOT PERMANENT !!!

/**
 * Simple fixed testing main program.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OpcCamTest {
    
    private static CamSource camSource;
    private static int imageReceived = -1;
    private static int robotId = 2;
    private static String opcNamespace = "Objects/PLC1/GVL_OPCIMG_" + robotId + "/";
    
    private static ReceptionCallback<BeckhoffOutput> callback = new ReceptionCallback<BeckhoffOutput>() {
        
        @Override
        public void received(BeckhoffOutput data) {
            int picCount = data.getIPicCounter();
            if (imageReceived != picCount) { // there is something new
                imageReceived = picCount;
                Command cmd = new CommandImpl();
                cmd.setCommand(Commands.SOURCE_TAKE_PICTURE.name());
                cmd.setStringParam(String.valueOf(picCount) + ";1"); // pic count, side
                camSource.processCommand(cmd);
            }
        }
        
        @Override
        public Class<BeckhoffOutput> getType() {
            return BeckhoffOutput.class;
        }
        
    };
    
    private static DataIngestor<ImageInput> imageIngestor = new DataIngestor<ImageInput>() {

        @Override
        public void ingest(ImageInput data) {
            System.out.println(data);
            /*try {
                BufferedImage image = ImageEncodingDecoding.base64StringToBufferdImage(data.getImage());
                ImageIcon icon = new ImageIcon(image);
                JFrame frame = new JFrame();
                frame.setLayout(new FlowLayout());
                frame.setSize(2500, 2100);
                JLabel lbl = new JLabel();
                lbl.setIcon(icon);
                frame.add(lbl);
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            } catch (IOException e) {
                System.out.println("ERROR: " + e.getMessage());
            }*/
        }
        
    };

    /**
     * Simple, fixed testing main program.
     * 
     * @param args ignored
     * @throws IOException shall not occur
     */
    public static void main(String[] args) throws IOException {
        SerializerRegistry.registerSerializer(iip.serializers.BeckhoffInputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.BeckhoffInputSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.BeckhoffOutputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.BeckhoffOutputSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.CommandImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.CommandSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.ImageInputImplSerializer.class);
        SerializerRegistry.registerSerializer(iip.serializers.ImageInputSerializer.class);

        camSource = new CamSource("CamSource", 
            ResourceLoader.getResourceAsStream("deployment.yml", 
                new FolderResourceResolver("gen/hm23/DemonstrationFederatedLearningAppHM23/src/resources")));
        camSource.attachImageInputIngestor(imageIngestor);
        try {
            camSource.setParameterRobotId(robotId);
            camSource.setParameterCamPort(-1);
            camSource.setParameterCamIP("192.168.2.80");
        } catch (ExecutionException e) {
            System.err.println("Cannot set cam parameter: " + e.getMessage());
        }
        
        de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<BeckhoffOutput, BeckhoffInput> conn = 
            new de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector<>(
                BeckhoffOPCConnector.createConnectorAdapter(null, null, () -> opcNamespace, () -> opcNamespace));
        conn.connect(BeckhoffOPCConnector.createConnectorParameter());
        conn.setReceptionCallback(callback);
        conn.notificationsChanged(false); // force sampling independent of model
        
        do {
            int lastImage = imageReceived;
            BeckhoffInput request = new BeckhoffInputImpl();
            request.setIPicScene((short) 1);
            request.setBPicTrigger(true);
            conn.write(request);
            System.out.println("Requesting image: " + request);
            while (imageReceived < 0 || lastImage != imageReceived) { // needed, sampling?
                conn.request(true);
                TimeUtils.sleep(500);
                lastImage = imageReceived;
            }
            System.out.println("Sleeping 2s");
            TimeUtils.sleep(2000);
        } while (true);
    }

}
