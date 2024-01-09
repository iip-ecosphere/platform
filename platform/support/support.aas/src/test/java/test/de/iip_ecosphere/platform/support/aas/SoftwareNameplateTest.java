/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas;

import static de.iip_ecosphere.platform.support.aas.types.common.Utils.parseCalendar;

import java.io.File;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder.ConfigurationPathsBuilder;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder.SoftwareNameplateInstanceBuilder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;

/**
 * Example for software nameplate.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SoftwareNameplateTest extends AbstractAasExample {

    @Override
    protected String getFolderName() {
        return "softwareNameplate";
    }
    
    @Override
    public File[] getTargetFiles() {
        return new File[] {new File("./output/softwareNameplate.aasx")};
    }

    @Override
    protected void createAas() {
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder("SoftwareNameplateExample", 
            "urn:::AAS:::SoftwareNameplateExample#");
        aasBuilder.createAssetBuilder("ci", "urn:::Asset:::sn#", AssetKind.INSTANCE).build();
        SoftwareNameplateBuilder snp = new SoftwareNameplateBuilder(aasBuilder, "urn:::SM:::SwNp#", 
            isCreateMultiLanguageProperties());
        snp.createSoftwareNameplateTypeBuilder()
            .setUriOfTheProduct("ZVEI.I40.ITinAutomation.DemoSW_123456")
            .setManufacturerName(new LangString("de", "ZVEI AK IT in Automation"))
            .setManufacturerProductDesignation(new LangString("en", "My Software Package for Demonstration"))
            .setManufacturerProductDescription(new LangString("en", "A first software..."))
            .setManufacturerProductFamily(new LangString("en", "Demo Products for IT in Automation"))
            .setManufacturerProductType(new LangString("en", "DP-AKIT-A"))
            .setSoftwareType("PLC Runtime")
            .setVersion("0.9.1.0")
            .setVersionName(new LangString("en", "R2021 beta"))
            .setVersionInfo(new LangString("en", "Please do not install in productive environments!"))
            .setReleaseDate(parseCalendar("2022-02-07T10:00:00.000+00:00"))
            .setReleaseNotes(new LangString("en", "This release..."))
            .setBuildDate(parseCalendar("2022-11-19T10:00:00.000+00:00"))
            .setInstallationURI(createURI("https://tud.de/inf/pk/demos/download/DemoFirmware_09.zip"))
            .setInstallationFile("")
            .setInstallerType("MSI")
            .setInstallationChecksum("0x2783")
            .build();
        SoftwareNameplateInstanceBuilder snpi = snp.createSoftwareNameplateInstanceBuilder();
        snpi.setSerialNumber("123456")
            .setInstanceName("My Software Instance")
            .setInstalledVersion("0.9.1.0")
            .setInstallationDate(parseCalendar("2020-11-19T09:30:20.000+00:00"))
            .setInstallationPath(createFileURI("C:\\Windows\\Program Files\\Demo\\Firmware"))
            .setInstallationSource(createURI("https://tud.de/inf/pk/installation/firmware/src"))
            .setInstalledOnArchitecture("x86-32")
            .setInstalledOnOS("Windows 10")
            .setInstalledOnHost("IPC_42")
            .setSLAInformation("Service level GOLD USER");
        ContactInformationsTest.populate(snpi.createContactInformationBuilder()).build();
        
        snpi.createInstalledModulesBuilder().build();
        ConfigurationPathsBuilder cpb = snpi.createConfigurationPathsBuilder();
        cpb.createConfigurationPathBuilder()
            .setConfigurationURI(createFileURI("C:\\Users\\mw30\\Documents\\ZVEI\\AKITinAutomation\\20201013"))
            .setConfigurationType("initial configuration")
            .build();
        cpb.build();
            
        snpi.build();
        snp.build();
        
        registerAas(aasBuilder);
    }

    @Override
    protected File getThumbnail() {
        return null;
    }
    
}
