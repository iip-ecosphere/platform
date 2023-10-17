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

package de.iip_ecosphere.platform.examples.hm23.mock;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import de.iip_ecosphere.platform.examples.hm23.Commands;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Robot, image and QR data source.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CamSource extends de.iip_ecosphere.platform.examples.hm23.CamSource {

    private int picNr = 1;
    
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
    protected String requestBase64Image(Commands command, String imageId) {
        AtomicReference<String> result = new AtomicReference<>("");
        File f = new File("src/test/resources", "test" + picNr + ".png").getAbsoluteFile();
        System.out.println(">>>>>>>>>>>>> Requesting Mock Image for Cam Source " + command + " " + f);
        String imgResource;
        if (f.exists()) {
            setLastRequestedUri(f.toURI().toString());
            imgResource = "test" + picNr + ".png";
        } else {
            setLastRequestedUri("http://me.here.de/PhoenixContact.jpg");
            imgResource = "resources/PhoenixContact.jpg";
        }
        AasUtils.resolveImage(imgResource,
            ResourceLoader.getAllRegisteredResolver(RESOURCE_DEVICES_RESOLVER, ResourceLoader.MAVEN_RESOLVER), 
            true, (n, r, m) -> {
                result.set(r);
            });
        picNr++;
        if (picNr > 3) {
            picNr = 1;
        }
        return result.get();
    }

}
