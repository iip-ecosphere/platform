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

import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.ecsRuntime.Monitor;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Creates an AAS for this device, deploys it to the platform AAS server and returns the address of the AAS. This may
 * be useful for devices that do not provide an AAS by themselves.
 * 
 * Resolution sequence:
 * <ol>
 *   <li>Classpath, <code>nameplate.yml</code></li>
 *   <li><code>src/test/resources/nameplate.yml</code> (for testing)</li>
 *   <li>Classpath, <code><i>deviceId</i>.yml</code></li>
 * </ol>
 * 
 * Based on BaSyx / Generic Frame for Technical Data for Industrial Equipment in Manufacturing (Version 1.1)
 * and for address a bit of ZVEI Digital Nameplate for industrial equipment V1.0.
 * 
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlDeviceAasProvider extends DeviceAasProvider {

    public static final String SUBMODEL_NAMEPLATE = "Nameplate";
    public static final String NAME_PROPERTY_PRODUCTIMAGE = "ProductImage";
   
    private String aasAddress = null;
    
    /**
     * Implements the JSL descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Descriptor implements DeviceAasProviderDescriptor {

        @Override
        public DeviceAasProvider createInstance() {
            return new YamlDeviceAasProvider();
        }

        @Override
        public boolean createsMultiProvider() {
            return false;
        }
        
    }
    
    @Override
    public String getURN() {
        return "urn:::AAS:::" +  getIdShort() + "#";        
    }
    
    @Override
    public String getIdShort() {
        return AasUtils.fixId("device" + Id.getDeviceIdAas());
    }
    
    @Override
    public String getDeviceAasAddress() {
        if (null == aasAddress) {
            String id = getIdShort();
            String urn = getURN();
            Aas aas = null;

            try {
                NameplateSetup nSetup = NameplateSetup.obtainNameplateSetup(); 
                aas = nSetup.createAas(urn, id, ab -> {
                    SubmodelBuilder smb = ab.createSubmodelBuilder("metrics", null);
                    MetricsAasConstructor.addProviderMetricsToAasSubmodel(smb, null, Monitor.TRANSPORT_METRICS_CHANNEL, 
                        Id.getDeviceId(), EcsFactory.getSetup().getTransport());
                    smb.build();
                });
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Device AAS cannot be created: : {}", e.getMessage());
            }

            if (null != aas) {
                try {
                    AasFactory factory = AasFactory.getInstance();
                    Registry reg = factory.obtainRegistry(AasPartRegistry.getSetup().getRegistryEndpoint());
                    aasAddress = reg.getEndpoint(aas);
                    LoggerFactory.getLogger(getClass()).info("Device AAS address for {}/{}: {}", id, urn, aasAddress);
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).error("Obtaining factory/endpoint: {}", e.getMessage());
                }
            }
        }
        return aasAddress;
    }

    /**
     * Preliminary way to find the nameplate YML.
     * 
     * @return the setup representing the nameplate YML
     * @throws IOException if the setup file cannot be read
     */
    public static NameplateSetup obtainNameplateSetup() throws IOException {
        InputStream is = AasUtils.CLASSPATH_RESOURCE_RESOLVER.resolve("nameplate.yml"); // preliminary
        if (null == is) {
            try {
                is = new FileInputStream("src/test/resources/nameplate.yml");
            } catch (IOException e) {
                String fName = Id.getDeviceId().toUpperCase() + ".yml";
                LoggerFactory.getLogger(YamlDeviceAasProvider.class).info("Checking AAS for id {} in resource file {}", 
                    Id.getDeviceId(), fName);
                is = AasUtils.CLASSPATH_RESOURCE_RESOLVER.resolve(fName);
            }
        }
        return AbstractSetup.readFromYaml(NameplateSetup.class, is); // closes is
    }

}
