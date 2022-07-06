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

package de.iip_ecosphere.platform.platform;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Provides the AAS of all known services. [preliminary]
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceAas {
    
    /**
     * Holds the "nameplate" information for known services.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ServiceAasSetup extends AbstractSetup {

        private List<ApplicationSetup> services = new ArrayList<ApplicationSetup>();
        
        /**
         * Returns the known services.
         * 
         * @return the known services
         */
        public List<ApplicationSetup> getServices() {
            return services;
        }
        
        /**
         * Changes the services.
         * 
         * @param services the services
         */
        public void setServices(List<ApplicationSetup> services) {
            this.services = services;
        }
        
    }
    
    /**
     * Preliminary way to find the nameplate YML.
     * 
     * @return the setup representing the nameplate YML
     * @throws IOException if the setup file cannot be read
     */
    public static ServiceAasSetup obtainNameplateSetup() throws IOException {
        InputStream is = ResourceLoader.getResourceAsStream("services.yml"); // TODO preliminary
        if (null == is) {
            try {
                is = new FileInputStream("src/test/resources/services.yml");
            } catch (IOException e) {
                LoggerFactory.getLogger(ServiceAas.class).error(
                    "Creating services AAS: {}", e.getMessage());
            }
        }
        return AbstractSetup.readFromYaml(ServiceAasSetup.class, is); // closes is
    }
    
    /**
     * Creates the AAS.
     * 
     * @return the endpoints of the created AAS
     */
    public static Map<String, String> createAas() {
        Map<String, String> result = new HashMap<>();
        try {
            List<Aas> aasList = new ArrayList<Aas>();
            Map<Aas, String> aasSIds = new HashMap<>();
            ServiceAasSetup setup = obtainNameplateSetup();
            if (null != setup && null != setup.getServices()) {
                for (ApplicationSetup s : setup.getServices()) {
                    Aas aas = createAas(s);
                    if (null != aas) {
                        aasList.add(aas);
                        aasSIds.put(aas, s.getId());
                    }
                }
            }
            AasSetup aasSetup = PlatformSetup.getInstance().getAas();
            AasPartRegistry.remoteDeploy(aasSetup, aasList);
            
            AasFactory factory = AasFactory.getInstance();
            int count = 0;
            for (Aas aas: aasList) {
                Registry reg = factory.obtainRegistry(aasSetup.getRegistryEndpoint());
                String sid = aasSIds.get(aas);
                if (null == sid) { // fallback
                    sid = "service_" + count;
                }
                result.put(sid, reg.getEndpoint(aas));
                count++;
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(ServiceAas.class).error(
                    "Creating services AAS: {}", e.getMessage());
        }
        return result;
    }

    /**
     * Checks for/creates the AAS for {@code se}.
     * 
     * @param se the setup instance
     * @return AAS to be deployed, <b>null</b> for none
     */
    private static final Aas createAas(ApplicationSetup se) {
        Aas result = null;
        AasSetup setup = PlatformSetup.getInstance().getAas();
        AasFactory factory = AasFactory.getInstance();
        String id = AasUtils.fixId("service_" + se.getId());
        String urn = "urn:::AAS:::" + id + "#";        
        try {
            AasPartRegistry.retrieveAas(setup, urn);
        } catch (IOException e) {
            // not there, ok
            AasBuilder aasBuilder = factory.createAasBuilder(id, urn);
            SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder(
                "Software", null);
            PlatformAas.addSoftwareInfo(smBuilder, se);
            smBuilder.build();
            PlatformAas.createNameplate(aasBuilder, se).build();
            result = aasBuilder.build();
        }
        return result;
    }

}
