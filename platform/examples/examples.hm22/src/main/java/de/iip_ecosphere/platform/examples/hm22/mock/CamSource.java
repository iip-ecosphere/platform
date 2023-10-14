/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.hm22.mock;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import de.iip_ecosphere.platform.examples.hm22.Commands;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import iip.datatypes.ImageInput;
import iip.datatypes.ImageInputImpl;

/**
 * Robot, image and QR data source.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CamSource extends de.iip_ecosphere.platform.examples.hm22.CamSource {

    /**
     * Fallback constructor.
     */
    public CamSource() {
        super();
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public CamSource(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    @Override
    protected String requestBase64Image(Commands command) {
        System.out.println(">>>>>>>>>>>>> Requesting Image for Cam Source " + command);
        AtomicReference<String> result = new AtomicReference<>("");
        AasUtils.resolveImage("resources/PhoenixContact.jpg",
            ResourceLoader.getAllRegisteredResolver(RESOURCE_DEVICES_RESOLVER), 
            true, (n, r, m) -> {
                result.set(r);
            });
        return result.get();
    }

    @Override
    public ImageInput robotQRScan(String b64Image) {
        ImageInput input = new ImageInputImpl();
        input.setImage(b64Image);
        input.setProductId("1234");
        input.setQrCodeDetected(true);
        return input;
    }

/*    @Override
    public void processCommand(Command cmd) {
        System.out.println("CAM: " + cmd);
        Commands c = Commands.valueOfSafe(cmd);
        ImageInput input;
        switch (c) {
        case SOURCE_DO_QR_SCAN:
            if (hasImageInputIngestor()) {
                input = new ImageInputImpl();
                AasUtils.resolveImage("resources/PhoenixContact.jpg",
                    ResourceLoader.getAllRegisteredResolver(RESOURCE_DEVICES_RESOLVER), 
                    true, (n, r, m) -> {
                        input.setImage(r);
                    });
                input.setProductId("1234");
                input.setQrCodeDetected(true);
                System.out.println("CAM SEND QR: " + input);
                ingestImageInput(input);
            }
            break;
        case SOURCE_TAKE_PICTURE:
            if (hasImageInputIngestor()) {
                input = new ImageInputImpl();
                AasUtils.resolveImage("resources/PhoenixContact.jpg", 
                    ResourceLoader.getAllRegisteredResolver(RESOURCE_DEVICES_RESOLVER), 
                    true, (n, r, m) -> {
                        input.setImage(r);
                    });

                input.setProductId("");
                input.setQrCodeDetected(false);
                System.out.println("CAM SEND PIC: " + input);
                ingestImageInput(input);
            }
            break;
        default:
            break;
        }
    }*/

}
