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

package de.iip_ecosphere.platform.ecsRuntime.deviceAas;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Registry;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.net.UriResolver;

/**
 * Creates an AAS for this device, deploys it to the platform AAS server and returns the address of the AAS. This may
 * be useful for devices that do not provide an AAS by themselves.
 * 
 * Resolution sequence:
 * <ol>
 *   <li>Classpath, <code>device.aasx</code></li>
 *   <li><code>src/test/resources/device.aasx</code> (for testing)</li>
 *   <li>Classpath, <code><i>deviceId</i>.aasx</code></li>
 * </ol>
 * 
 * Based on BaSyx / Generic Frame for Technical Data for Industrial Equipment in Manufacturing (Version 1.1)
 * and for address a bit of ZVEI Digital Nameplate for industrial equipment V1.0.
 * 
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasxDeviceAasProvider extends DeviceAasProvider {

    private String aasAddress = null;
    private String identifier;
    private String idShort;

    /**
     * Implements the JSL descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements DeviceAasProviderDescriptor {

        @Override
        public DeviceAasProvider createInstance() {
            return new AasxDeviceAasProvider();
        }

        @Override
        public boolean createsMultiProvider() {
            return false;
        }
        
    }
    
    @Override
    public String getURN() {
        return identifier;        
    }
    
    @Override
    public String getIdShort() {
        return idShort;
    }
    
    @Override
    public String getDeviceAasAddress() {
        if (null == aasAddress) {
            List<Aas> loadedAas;
            try {
                loadedAas = obtainAas();
            } catch (IOException e) {
                loadedAas = null;
                LoggerFactory.getLogger(getClass()).error("Loading device AASX: {}", e.getMessage());
            }
            Aas aas = null;
            if (null != loadedAas) {
                List<Aas> toDeploy = new ArrayList<Aas>();
                for (Aas a: loadedAas) {
                    if (null == aas) {
                        aas = a; // for now, grab the first one; if there are multiple ones, which is the correct one?
                        identifier = a.getIdentification();
                        idShort = aas.getIdShort();
                    }
                    try {
                        AasPartRegistry.retrieveAas(a.getIdentification());
                    } catch (IOException e) {
                        // not there, ok, deploy
                        toDeploy.add(a);
                    }
                }
                if (!toDeploy.isEmpty()) {
                    try {
                        AasPartRegistry.remoteDeploy(toDeploy);
                    } catch (IOException e) {
                        LoggerFactory.getLogger(getClass()).error("Deploying device AASX: {}", e.getMessage());
                    }
                }
            }
            
            if (null != aas) {
                try {
                    Registry reg = AasFactory.getInstance().obtainRegistry(
                        AasPartRegistry.getSetup().getRegistryEndpoint());
                    aasAddress = reg.getEndpoint(aas);
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Obtaining factory/endpoint: {}", e.getMessage());
                }
            } else {
                LoggerFactory.getLogger(getClass()).warn("No device AAS found");
            }
        }
        return aasAddress;
    }

    /**
     * Preliminary way to find an AASX file for the nameplate.
     * 
     * @return the AAS (one or multiple) loaded from
     * @throws IOException if the setup file cannot be read
     */
    private static List<Aas> obtainAas() throws IOException {
        List<Aas> result = null;
        ClassLoader loader = AasxDeviceAasProvider.class.getClassLoader();
        URL url = loader.getResource("device.aasx");
        if (null == url) {
            File f = new File("src/test/resources/device.aasx"); // for testing
            if (f.exists()) {
                url = f.toURI().toURL();
            } else {
                LoggerFactory.getLogger(AasxDeviceAasProvider.class).info("Checking AAS for id {}", Id.getDeviceId());
                url = loader.getResource(Id.getDeviceId().toUpperCase() + ".aasx");
            }
        }
        if (null != url) {
            try {
                File aasxFile = UriResolver.resolveToFile(url.toURI(), null);
                result = AasFactory.getInstance().createPersistenceRecipe().readFrom(aasxFile);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
        return result;
    }

}
