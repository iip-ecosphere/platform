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
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.NameplateSetup.Address;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Creates an AAS for this device, deploys it to the platform AAS server and returns the address of the AAS. This may
 * be useful for devices that do not provide an AAS by themselves.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SelfDeviceAasProvider extends DeviceAasProvider {

    public static final String SUBMODEL_NAMEPLATE = "Nameplate";
    public static final String NAME_PROPERTY_IMAGE = "Image";
    public static final String NAME_PROPERTY_MANUFACTURER_NAME = "ManufacturerName";
    public static final String NAME_PROPERTY_MANUFACTURER_PRODUCT_DESIGNATION = "ManufacturerProductDesignation";
    public static final String NAME_SMC_ADDRESS = "Address";
    public static final String NAME_PROPERTY_CITYTOWN = "CityTown";
    public static final String NAME_PROPERTY_DEPARTMENT = "Department";
    public static final String NAME_PROPERTY_STREET = "Street";
    public static final String NAME_PROPERTY_ZIPCODE = "ZipCode";
    
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
                    AasBuilder aasBuilder = factory.createAasBuilder(id, urn);
                    SubmodelBuilder smBuilder = aasBuilder.createSubmodelBuilder(SUBMODEL_NAMEPLATE, null);
                    NameplateSetup nSetup = obtainNameplateSetup(); 
                    createNameplate(smBuilder, nSetup);
                    smBuilder.build();
                    aas = aasBuilder.build();
                    AasPartRegistry.remoteDeploy(CollectionUtils.addAll(new ArrayList<Aas>(), aas));
                } catch (IOException e1) {
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
        InputStream is = SelfDeviceAasProvider.class.getResourceAsStream("/nameplate.yml"); // TODO preliminary
        if (null == is) {
            try {
                is = new FileInputStream("src/test/resources/nameplate.yml");
            } catch (IOException e) {
                is = SelfDeviceAasProvider.class.getResourceAsStream("/" + Id.getDeviceIdAas() + ".yml");
            }
        }
        return AbstractSetup.readFromYaml(NameplateSetup.class, is); // closes is
    }
    
    /**
     * Creates the "nameplate". A bit of ZVEI Digital Nameplate for industrial equipment V1.0.
     * 
     * @param smBuilder the builder, do not call {@link SubmodelBuilder#build()} in here!
     * @param appSetup application setup
     */
    public static void createNameplate(SubmodelElementContainerBuilder smBuilder, NameplateSetup appSetup) {
        smBuilder.createPropertyBuilder(NAME_PROPERTY_IMAGE)
            .setValue(Type.LANG_STRING, appSetup.getImage())
            .build();
        smBuilder.createPropertyBuilder(NAME_PROPERTY_MANUFACTURER_NAME)
            .setValue(Type.LANG_STRING, appSetup.getManufacturerName())
            .build();
        smBuilder.createPropertyBuilder(NAME_PROPERTY_MANUFACTURER_PRODUCT_DESIGNATION)
            .setValue(Type.LANG_STRING, appSetup.getManufacturerProductDesignation())
            .build();
        createAddress(smBuilder, appSetup.getAddress());
    }
    
    /**
     * Creates (part) of a nameplate address. A bit of ZVEI Digital Nameplate for industrial equipment V1.0.
     * 
     * @param smBuilder the builder, do not call {@link SubmodelBuilder#build()} in here!
     * @param address the address to use
     */
    protected static void createAddress(SubmodelElementContainerBuilder smBuilder, Address address) {
        if (null != address) {
            SubmodelElementCollectionBuilder aBuilder 
                = smBuilder.createSubmodelElementCollectionBuilder(NAME_SMC_ADDRESS, false, false);
            aBuilder.createPropertyBuilder(NAME_PROPERTY_CITYTOWN)
                .setValue(Type.LANG_STRING, address.getCityTown())
                .build();
            aBuilder.createPropertyBuilder(NAME_PROPERTY_DEPARTMENT)
                .setValue(Type.LANG_STRING, address.getDepartment())
                .build();
            aBuilder.createPropertyBuilder(NAME_PROPERTY_STREET)
                .setValue(Type.LANG_STRING, address.getStreet())
                .build();
            aBuilder.createPropertyBuilder(NAME_PROPERTY_ZIPCODE)
                .setValue(Type.LANG_STRING, address.getZipCode())
                .build();
            aBuilder.build();
        }
    }

}
