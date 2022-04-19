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

package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation.FurtherInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation.GeneralInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel.TechnicalDataSubmodelBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Creates an AAS for this device, deploys it to the platform AAS server and returns the address of the AAS. This may
 * be useful for devices that do not provide an AAS by themselves.
 * 
 * Based on BaSyx / Generic Frame for Technical Data for Industrial Equipment in Manufacturing (Version 1.1)
 * and for address a bit of ZVEI Digital Nameplate for industrial equipment V1.0.
 * 
 * 
 * @author Holger Eichelberger, SSE
 */
public class SelfDeviceAasProvider extends DeviceAasProvider {

    public static final String SUBMODEL_NAMEPLATE = "Nameplate";
    public static final String NAME_PROPERTY_PRODUCTIMAGE = "ProductImage";
   
    private String aasAddress = null;
    
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

            AasFactory factory = AasFactory.getInstance();
            Aas aas = null;
            try {
                aas = AasPartRegistry.retrieveAas(urn);
            } catch (IOException e) {
                // not there, ok
                try {
                    NameplateSetup nSetup = obtainNameplateSetup(); 
                    AasBuilder aasBuilder = factory.createAasBuilder(id, urn);
                    TechnicalDataSubmodelBuilder tdBuilder = aasBuilder.createTechnicalDataSubmodelBuilder(null);
                    GeneralInformationBuilder giBuilder = tdBuilder.createGeneralInformationBuilder(
                        nSetup.getManufacturerName(), 
                        LangString.create(nSetup.getManufacturerProductDesignation()), "", "");
                    PlatformAas.createAddress(giBuilder, nSetup.getAddress()); // inofficial, not in Generic Frame
                    AasUtils.resolveImage(nSetup.getProductImage(), AasUtils.CLASSPATH_RESOURCE_RESOLVER, false, 
                        (n, r, m) -> giBuilder.addProductImageFile(n, r, m));
                    AasUtils.resolveImage(nSetup.getManufacturerLogo(), AasUtils.CLASSPATH_RESOURCE_RESOLVER, true, 
                        (n, r, m) -> giBuilder.setManufacturerLogo(r, m));
                    giBuilder.build();
                    final GregorianCalendar now = new GregorianCalendar();
                    XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
                    FurtherInformationBuilder fiBuilder = tdBuilder.createFurtherInformationBuilder(cal);
                    fiBuilder.build();
                    tdBuilder.createTechnicalPropertiesBuilder().build();
                    tdBuilder.createProductClassificationsBuilder().build();
                    tdBuilder.build();
                    aas = aasBuilder.build();
                    AasPartRegistry.remoteDeploy(CollectionUtils.addAll(new ArrayList<Aas>(), aas));
                } catch (IOException | DatatypeConfigurationException e1) {
                    LoggerFactory.getLogger(getClass()).error("Creating nameplate AAS: {}", e.getMessage());
                }
            }
            if (null != aas) {
                try {
                    Registry reg = factory.obtainRegistry(AasPartRegistry.getSetup().getRegistryEndpoint());
                    aasAddress = reg.getEndpoint(aas);
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
                is = AasUtils.CLASSPATH_RESOURCE_RESOLVER.resolve(Id.getDeviceIdAas() + ".yml");
            }
        }
        return AbstractSetup.readFromYaml(NameplateSetup.class, is); // closes is
    }

}
