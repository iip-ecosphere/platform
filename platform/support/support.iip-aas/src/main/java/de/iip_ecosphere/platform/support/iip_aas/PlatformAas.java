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
import de.iip_ecosphere.platform.support.resources.ResourceResolver;

/**
 * The platform name/typeplate.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAas implements AasContributor {

    public static final String NAME_SUBMODEL = "platform";
    public static final String SUBMODEL_NAMEPLATE = "TechnicalData"; // preliminary, the software "Nameplate"
    public static final String NAME_PROPERTY_NAME = "name";
    public static final String NAME_PROPERTY_VERSION = "version";
    public static final String NAME_PROPERTY_RELEASE = "isRelease";
    public static final String NAME_PROPERTY_BUILDID = "buildId";
    public static final String NAME_OPERATION_SNAPSHOTAAS = "snapshotAas";
    public static final String NAME_PROPERTY_ID = "Id";
    public static final String NAME_PROPERTY_PRODUCTIMAGE = "ProductImage";
    public static final String NAME_PROPERTY_MANUFACTURER_LOGO = "ManufacturerLogo";
    public static final String NAME_PROPERTY_MANUFACTURER_NAME = "ManufacturerName";
    public static final String NAME_PROPERTY_MANUFACTURER_PRODUCT_DESIGNATION = "ManufacturerProductDesignation";
    public static final String NAME_SMC_ADDRESS = "Address";
    public static final String NAME_PROPERTY_CITYTOWN = "CityTown";
    public static final String NAME_PROPERTY_DEPARTMENT = "Department";
    public static final String NAME_PROPERTY_STREET = "Street";
    public static final String NAME_PROPERTY_ZIPCODE = "ZipCode";
    
    private static final String MAVEN_SNAPSHOT_POSTFIX = "-SNAPSHOT";
    private static ResourceResolver imageResolver = AasUtils.CLASSPATH_RESOURCE_RESOLVER;
    
    /**
     * Changes the image resolver. [public for testing]
     * 
     * @param resolver the resolver
     */
    public static void setImageResolver(ResourceResolver resolver) {
        if (null != resolver) {
            imageResolver = resolver;
        }
    }

    /**
     * Returns the image resolver. [public for testing]
     * 
     * @return the image resolver
     */
    public static ResourceResolver getImageResolver() {
        return imageResolver;
    }

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
            setup.setManufacturerName("IIP-Ecosphere Consortium@de");
            setup.setManufacturerLogo("IIP-Ecosphere-Logo.png"); // in software
            setup.setProductImage("IIP-Ecosphere-Platform.png");
            setup.setDescription("The IIP-Ecosphere AI-enabled I4.0 platform.@de");
            Address addr = new Address();
            addr.setDepartment("University of Hildesheim, SSE - Software Systems Engineering@de");
            addr.setStreet("Universitätsplatz 1@de");
            addr.setZipCode("31141@de");
            addr.setCityTown("Hildesheim/Hannover@de");
            setup.setAddress(addr);
            
            SubmodelBuilder smBuilder = createNameplate(aasBuilder, setup);
            addSoftwareInfo(smBuilder, setup);
            smBuilder.build();
            addSoftwareInfo(smB, setup); // old style
            smB.createPropertyBuilder(NAME_PROPERTY_RELEASE)
                .setValue(Type.BOOLEAN, isRelease)
                .build();
            smB.createPropertyBuilder(NAME_PROPERTY_BUILDID)
                .setValue(Type.STRING, buildId)
                .build();
            smB.createOperationBuilder(NAME_OPERATION_SNAPSHOTAAS) // TODO restrict access
                .addInputVariable("id", Type.STRING)
                .setInvocable(iCreator.createInvocable(NAME_OPERATION_SNAPSHOTAAS))
                .build(Type.STRING);
            smB.build();
        }
        return null;
    }
    
    /**
     * Add software-related information to the given submodel.
     * 
     * @param smB the submodel builder
     * @param appSetup the application setup
     */
    public static void addSoftwareInfo(SubmodelBuilder smB, ApplicationSetup appSetup) {
        if (null != appSetup.getId()) {
            smB.createPropertyBuilder(NAME_PROPERTY_ID)
                .setValue(Type.STRING, appSetup.getId())
                .build();
        }
        smB.createPropertyBuilder(NAME_PROPERTY_NAME)
            .setValue(Type.STRING, appSetup.getName())
            .build();
        smB.createPropertyBuilder(NAME_PROPERTY_VERSION)
            .setValue(Type.STRING, null == appSetup.getVersion() ? "" : appSetup.getVersion().toString())
            .build();
    }

    /**
     * Creates the "nameplate". A bit of ZVEI Digital Nameplate for industrial equipment V1.0.
     * 
     * @param aasBuilder the AAS builder, do not call {@link AasBuilder#build()} in here!
     * @param appSetup application setup
     * @return submodel builder if something needs to be added
     */
    public static SubmodelBuilder createNameplate(AasBuilder aasBuilder, ApplicationSetup appSetup) {
        SubmodelBuilder sBuilder = aasBuilder.createSubmodelBuilder(SUBMODEL_NAMEPLATE, null);
        AasUtils.resolveImage(appSetup.getProductImage(), imageResolver, true, (n, r, m) -> {
            sBuilder.createFileDataElementBuilder(NAME_PROPERTY_PRODUCTIMAGE, r, m).build();
        });
        AasUtils.resolveImage(appSetup.getManufacturerLogo(), imageResolver, true, (n, r, m) -> {
            sBuilder.createFileDataElementBuilder(NAME_PROPERTY_MANUFACTURER_LOGO, r, m).build();
        });
        sBuilder.createPropertyBuilder(NAME_PROPERTY_MANUFACTURER_NAME)
            .setValue(Type.LANG_STRING, appSetup.getManufacturerName())
            .build();
        sBuilder.createPropertyBuilder(NAME_PROPERTY_MANUFACTURER_PRODUCT_DESIGNATION)
            .setValue(Type.LANG_STRING, appSetup.getManufacturerProductDesignation())
            .build();
        createAddress(sBuilder, appSetup.getAddress());
        return sBuilder;
    }
    
    /**
     * Creates (part) of a nameplate address. A bit of ZVEI Digital Nameplate for industrial equipment V1.0.
     * 
     * @param smBuilder the builder, do not call {@link SubmodelBuilder#build()} in here!
     * @param address the address to use
     */
    public static void createAddress(SubmodelElementContainerBuilder smBuilder, Address address) {
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
