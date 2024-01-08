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

import java.io.File;

import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.types.contactInformations.ContactInformationsBuilder;
import de.iip_ecosphere.platform.support.aas.types.contactInformations.ContactInformationsBuilder.ContactInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.contactInformations.ContactInformationsBuilder.RoleOfContactPerson;
import de.iip_ecosphere.platform.support.aas.types.contactInformations.ContactInformationsBuilder.TypeOfEmailAddress;
import de.iip_ecosphere.platform.support.aas.types.contactInformations.ContactInformationsBuilder.TypeOfFaxNumber;
import de.iip_ecosphere.platform.support.aas.types.contactInformations.ContactInformationsBuilder.TypeOfTelephone;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;

/**
 * Example for contact informations.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ContactInformationsTest extends AbstractAasExample {

    @Override
    protected String getFolderName() {
        return "contactInformations";
    }
    
    @Override
    public File[] getTargetFiles() {
        return new File[] {new File("./output/contactInformations.aasx")};
    }

    @Override
    protected void createAas() {
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder("ContactInformationsExample", 
            "urn:::AAS:::ContactInformationsExample#");
        aasBuilder.createAssetBuilder("ci", "urn:::Asset:::ci#", AssetKind.INSTANCE).build();
        ContactInformationsBuilder cis = new ContactInformationsBuilder(aasBuilder, "urn:::SM:::ContactInformations#", 
            isCreateMultiLanguageProperties());
        ContactInformationBuilder ci = cis.createContactInformationBuilder();
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
        ci.setRoleOfContactPerson(RoleOfContactPerson.ADMINISTRATIVE);
        ci.setTimeZone("-6:00");
        ci.createEmailBuilder("john@doe.com")
            .setPublicKey(new LangString("en", "???"))
            .setTypeOfFaxNumber(TypeOfEmailAddress.OFFICE)
            .setTypeOfPublicKey(new LangString("en", "N/A"))
            .build();
        ci.createFaxBuilder(new LangString("en", "000-000-0003"))
            .setTypeOfFaxNumber(TypeOfFaxNumber.SECRETARY)
            .build();
        ci.createPhoneBuilder(new LangString("en", "000-000-0002"))
            .setTypeOfTelephone(TypeOfTelephone.SECRETARY)
            .setAvailableTime(new LangString("en", "0-24"))
            .build();
        ci.createIPCommunicationBuilder("http://comm.doe.com")
            .setAvailableTime(new LangString("en", "24/7"))
            .setTypeOfCommunication("Skype4business")
            .build();
        ci.createIPCommunicationBuilder("sip://comm.doe.com")
            .setAvailableTime(new LangString("en", "8-17"))
            .setTypeOfCommunication("SIP phone")
            .build();
        ci.build();
        cis.build();
        
        registerAas(aasBuilder);
    }

    @Override
    protected File getThumbnail() {
        return null;
    }
    
    /**
     * Tests defined enumerations.
     */
    @Test
    public void testEnums() {
        assertEnum(ContactInformationsBuilder.RoleOfContactPerson.values(), 
            v -> v.getValue() != null && v.getValueId() != null);
        assertEnum(ContactInformationsBuilder.TypeOfTelephone.values(), 
            v -> v.getValue() != null && v.getValueId() != null);
        assertEnum(ContactInformationsBuilder.TypeOfEmailAddress.values(), 
            v -> v.getValue() != null && v.getValueId() != null);
        assertEnum(ContactInformationsBuilder.TypeOfFaxNumber.values(), 
            v -> v.getValue() != null && v.getValueId() != null);
    }
    
}
