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

import static de.iip_ecosphere.platform.support.aas.types.common.Utils.parseDate;

import java.io.File;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder.ConfigurationPathsBuilder;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder.ContactInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder.RoleOfContactPerson;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder
    .SoftwareNameplate_InstanceBuilder;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder.TypeOfEmailAddress;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder.TypeOfFaxNumber;
import de.iip_ecosphere.platform.support.aas.types.softwareNameplate.SoftwareNameplateBuilder.TypeOfTelephone;
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
        aasBuilder.createAssetInformationBuilder("ci", "urn:::Asset:::sn#", AssetKind.INSTANCE).build();
        SoftwareNameplateBuilder snp = new SoftwareNameplateBuilder(aasBuilder, "urn:::SM:::SwNp#");
        snp.setCreateMultiLanguageProperties(isCreateMultiLanguageProperties());
        snp.createSoftwareNameplate_TypeBuilder()
            .setURIOfTheProduct("ZVEI.I40.ITinAutomation.DemoSW_123456")
            .setManufacturerName(new LangString("de", "ZVEI AK IT in Automation"))
            .setManufacturerProductDesignation(new LangString("en", "My Software Package for Demonstration"))
            .setManufacturerProductDescription(new LangString("en", "A first software..."))
            .setManufacturerProductFamily(new LangString("en", "Demo Products for IT in Automation"))
            .setManufacturerProductType(new LangString("en", "DP-AKIT-A"))
            .setSoftwareType("PLC Runtime")
            .setVersion("0.9.1.0")
            .setVersionName(new LangString("en", "R2021 beta"))
            .setVersionInfo(new LangString("en", "Please do not install in productive environments!"))
            .setReleaseDate(parseDate("2022-02-07T10:00:00.000+00:00"))
            .setReleaseNotes(new LangString("en", "This release..."))
            .setBuildDate(parseDate("2022-11-19T10:00:00.000+00:00"))
            .setInstallationURI(createURI("https://tud.de/inf/pk/demos/download/DemoFirmware_09.zip"))
            .setInstallationFile("", "")
            .setInstallerType("MSI")
            .setInstallationChecksum("0x2783")
            .build();
        SoftwareNameplate_InstanceBuilder snpi = snp.createSoftwareNameplate_InstanceBuilder();
        snpi.setSerialNumber("123456")
            .setInstanceName("My Software Instance")
            .setInstalledVersion("0.9.1.0")
            .setInstallationDate(parseDate("2020-11-19T09:30:20.000+00:00"))
            .setInstallationPath(createFileURI("C:\\Windows\\Program Files\\Demo\\Firmware"))
            .setInstallationSource(createURI("https://tud.de/inf/pk/installation/firmware/src"))
            .setInstalledOnArchitecture("x86-32")
            .setInstalledOnOS("Windows 10")
            .setInstalledOnHost("IPC_42")
            .setSLAInformation("Service level GOLD USER");
        populate(snpi.createContactBuilder()).build();
        
        snpi.createInstalledModulesBuilder()
            .setInstalledModule("Module")
            .build();
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
    
    /**
     * Populates an example contact information builder.
     * 
     * @param ci the builder
     * @return {@code ci}
     */
    public static ContactInformationBuilder populate(ContactInformationBuilder ci) { // TODO avoid duplication
        ci.setNameOfContact(new LangString("en", "Doe"));
        ci.setFirstName(new LangString("en", "John"));
        ci.setMiddleNames(new LangString("en", "M."));
        ci.setAcademicTitle(new LangString("de", "Dr."));
        ci.setTitle(new LangString("en", "Rev."));
        ci.setCityTown(new LangString("en", "Doecit"));
        ci.setNationalCode(new LangString("en", "US"));
        ci.setStreet(new LangString("en", "John Avenue"));
        ci.setPOBox(new LangString("en", "PO 12345"));
        ci.setFurtherDetailsOfContact(new LangString("en", "unknown"));
        ci.setAddressOfAdditionalLink("http://me.here.de");
        ci.setCompany(new LangString("en", "Doe Ltd."));
        ci.setDepartment(new LangString("en", "top management"));
        ci.setRoleOfContactPerson(RoleOfContactPerson.ADMINISTRATIV_CONTACT);
        ci.setTimeZone("-6:00");
        ci.createEmailBuilder()
            .setEmailAddress("john@doe.com")
            .setPublicKey(new LangString("en", "???"))
            .setTypeOfEmailAddress(TypeOfEmailAddress.OFFICE)
            .setTypeOfPublicKey(new LangString("en", "N/A"))
            .build();
        ci.createFaxBuilder()
            .setFaxNumber(new LangString("en", "000-000-0003"))
            .setTypeOfFaxNumber(TypeOfFaxNumber.SECRETARY)
            .build();
        ci.createPhoneBuilder()
            .setTelephoneNumber(new LangString("en", "000-000-0002"))
            .setTypeOfTelephone(TypeOfTelephone.SECRETARY)
            .setAvailableTime(new LangString("en", "0-24"))
            .build();
        ci.createIPCommunicationBuilder()
            .setAddressOfAdditionalLink("http://comm.doe.com")
            .setAvailableTime(new LangString("en", "24/7"))
            .setTypeOfCommunication("Skype4business")
            .build();
        ci.createIPCommunicationBuilder()
            .setAddressOfAdditionalLink("sip://comm.doe.com")
            .setAvailableTime(new LangString("en", "8-17"))
            .setTypeOfCommunication("SIP phone")
            .build();
        return ci;
    }

    @Override
    protected File getThumbnail() {
        return null;
    }
    
}
