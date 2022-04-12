/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup.Address;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;

/**
 * The platform name/typeplate.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAas implements AasContributor {

    public static final String NAME_SUBMODEL = "platform";
    public static final String NAME_PROPERTY_NAME = "name";
    public static final String NAME_PROPERTY_VERSION = "version";
    public static final String NAME_PROPERTY_RELEASE = "isRelease";
    public static final String NAME_PROPERTY_BUILDID = "buildId";
    public static final String NAME_OPERATION_SNAPSHOTAAS = "snapshotAas";
    public static final String NAME_PROPERTY_ID = "Id";
    public static final String NAME_PROPERTY_MANUFACTURER_NAME = "ManufacturerName";
    public static final String NAME_PROPERTY_MANUFACTURER_PRODUCT_DESIGNATION = "ManufacturerProductDesignation";
    public static final String NAME_SMC_ADDRESS = "Address";
    public static final String NAME_PROPERTY_CITYTOWN = "CityTown";
    public static final String NAME_PROPERTY_DEPARTMENT = "Department";
    public static final String NAME_PROPERTY_STREET = "Street";
    public static final String NAME_PROPERTY_ZIPCODE = "ZipCode";
    
    private static final String MAVEN_SNAPSHOT_POSTFIX = "-SNAPSHOT";
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        if (smB.isNew()) { // incremental remote deployment, avoid double creation
            String ver = "";
            String buildId = "??";
            boolean isRelease = false;
            InputStream is = getClass().getClassLoader().getResourceAsStream("iip-version.properties");
            if (null != is) {
                Properties prop = new Properties();
                try {
                    prop.load(is);
                    is.close();
                } catch (IOException e) {
                }
                ver = prop.getOrDefault("version", ver).toString();
                if (ver.endsWith(MAVEN_SNAPSHOT_POSTFIX)) {
                    ver = ver.substring(0, ver.length() - MAVEN_SNAPSHOT_POSTFIX.length());
                } else {
                    isRelease = true;
                }
                buildId = prop.getOrDefault("buildId", buildId).toString();
            }
            ApplicationSetup setup = new ApplicationSetup();
            setup.setVersion(ver);
            setup.setName("IIP-Ecosphere platform");
            setup.setManufacturerName("IIP-Ecosphere Consortium@DE");
            setup.setDescription("The IIP-Ecosphere AI-enabled I4.0 platform.@DE");
            Address addr = new Address();
            addr.setDepartment("University of Hildesheim, SSE - Software Systems Engineering@DE");
            addr.setStreet("Universitätsplatz 1@DE");
            addr.setZipCode("31141@DE");
            addr.setCityTown("Hildesheim/Hannover@DE");
            setup.setAddress(addr);
            
            createNameplate(smB, setup);
            smB.createPropertyBuilder(NAME_PROPERTY_RELEASE)
                .setValue(Type.BOOLEAN, isRelease)
                .build();
            smB.createPropertyBuilder(NAME_PROPERTY_BUILDID)
                .setValue(Type.STRING, buildId)
                .build();
            smB.createOperationBuilder(NAME_OPERATION_SNAPSHOTAAS)
                .addInputVariable("id", Type.STRING)
                .setInvocable(iCreator.createInvocable(NAME_OPERATION_SNAPSHOTAAS))
                .build(Type.STRING);
            smB.build();
        }
        return null;
    }

    /**
     * Creates the "nameplate".
     * 
     * @param smBuilder the builder, do not call {@link SubmodelBuilder#build()} in here!
     * @param appSetup application setup
     */
    public static void createNameplate(SubmodelElementContainerBuilder smBuilder, ApplicationSetup appSetup) {
        if (null != appSetup.getId()) {
            smBuilder.createPropertyBuilder(NAME_PROPERTY_ID)
                .setValue(Type.STRING, appSetup.getId())
                .build();
        }
        smBuilder.createPropertyBuilder(NAME_PROPERTY_NAME)
            .setValue(Type.STRING, appSetup.getName())
            .build();
        smBuilder.createPropertyBuilder(NAME_PROPERTY_VERSION)
            .setValue(Type.STRING, null == appSetup.getVersion() ? "" : appSetup.getVersion().toString())
            .build();
        smBuilder.createPropertyBuilder(NAME_PROPERTY_MANUFACTURER_NAME)
            .setValue(Type.LANG_STRING, "")
            .build();
        smBuilder.createPropertyBuilder(NAME_PROPERTY_MANUFACTURER_PRODUCT_DESIGNATION)
            .setValue(Type.LANG_STRING, "")
            .build();
        createAddress(smBuilder, appSetup.getAddress());
    }
    
    /**
     * Creates (part) of a nameplate address.
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


    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(NAME_OPERATION_SNAPSHOTAAS, new JsonResultWrapper(p -> { 
            return snapshotAas(AasUtils.readString(p));
        }));
    }
    
    /**
     * Snapshots an AAS, i.e., the {@link AasPartRegistry#getIipAasInstance()}.
     * 
     * @param id an optional id to be placed into the file name, may be <b>null</b> or empty
     * @return the name of the file written, may be empty for none 
     * @throws ExecutionException if there is no IIP AAS instance or writing the instance fails for some reason
     */
    static String snapshotAas(String id) throws ExecutionException {
        String result = "";
        List<Aas> aas = AasPartRegistry.getIipAasInstance();
        if (null != aas) {
            String name = "platform-";
            if (null != id && id.length() > 0) {
                name += id + "-";
            }
           // for now, we just assume that AASX is supported
            name += System.currentTimeMillis() + ".aasx";
            try {
                File file = new File(FileUtils.getTempDirectory(), name);
                AasFactory.getInstance().createPersistenceRecipe().writeTo(aas, file);
                result = file.getAbsolutePath();  
            } catch (IOException e) {
                throw new ExecutionException(e);
            }
        } else {
            throw new ExecutionException("No suitable AAS instance available. Cannot write AAS.", null);
        }
        return result;
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    @Override
    public boolean isValid() {
        return true;
    }

}
