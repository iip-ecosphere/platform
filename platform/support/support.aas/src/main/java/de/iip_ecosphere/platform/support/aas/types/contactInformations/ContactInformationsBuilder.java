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

package de.iip_ecosphere.platform.support.aas.types.contactInformations;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;
import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollectionBuilder;

/**
 * Support for <a href="https://industrialdigitaltwin.org/wp-content/uploads/2022/10/
 * IDTA-02002-1-0_Submodel_ContactInformation.pdf">IDTA 02002-1-0 Submodel for Contact Information</a>.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ContactInformationsBuilder extends DelegatingSubmodelBuilder {
    
    private int contactInformationCount;
    private boolean createMultiLanguageProperties;

    /**
     * Creates a builder instance.
     * 
     * @param aas the parent AAS
     * @param identifier the identifier of this submodel
     * @param createMultiLanguageProperties whether multi-language properties shall be created, taints compliance 
     *     if {@code false}
     */
    public ContactInformationsBuilder(AasBuilder aas, String identifier, boolean createMultiLanguageProperties) {
        super(aas.createSubmodelBuilder("ContactInformations", identifier));
        this.createMultiLanguageProperties = createMultiLanguageProperties;
        setSemanticId(iri("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations"));
    }

    /**
     * Creates a contact information builder.
     * 
     * @return the builder instance
     */
    public ContactInformationBuilder createContactInformationBuilder() {
        return new ContactInformationBuilder(getDelegate(), ++contactInformationCount);
    }
    
    @Override
    public Submodel build() {
        assertThat(contactInformationCount >= 1, "There must be at least one contact information.");
        return super.build();
    }
    
    /**
     * Denotes the role of a contact person.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum RoleOfContactPerson {
        
        ADMINISTRATIVE("administrative contact", irdi("0173-1#07-AAS927#001")),
        COMMERCIAL("commercial contact", irdi("0173-1#07-AAS928#001")),
        OTHER("other contact", irdi("0173-1#07-AAS929#001")),
        HAZARDOUS_GOODS("hazardous goods contact", irdi("0173-1#07-AAS930#001")),
        TECHNICAL("technical contact", irdi("0173-1#07-AAS931#001"));
        
        private String value;
        private String valueId;
        
        /**
         * Creates a constant.
         * 
         * @param value the value
         * @param valueId the value id
         */
        private RoleOfContactPerson(String value, String valueId) {
            this.value = value;
            this.valueId = valueId;
        }
        
        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }
        
        /**
         * Returns the value id.
         * 
         * @return the (semantic) id of the value
         */
        public String getValueId() {
            return valueId;
        }
        
    }
    
    /**
     * Denotes the type of telephone.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum TypeOfTelephone {
        
        OFFICE("office", irdi("0173-1#07-AAS754#001")),
        OFFICE_MOBILE("office mobile", irdi("0173-1#07-AAS755#001")),
        SECRETARY("secretary", irdi("0173-1#07-AAS756#001")),
        SUBSTITUTE("substitute", irdi("0173-1#07-AAS757#001")),
        HOME("home", irdi("0173-1#07-AAS758#001")),
        PRIVATE_MOBILE("private mobile", irdi("0173-1#07-AAS759#001"));
        
        private String value;
        private String valueId;
        
        /**
         * Creates a constant.
         * 
         * @param value the value
         * @param valueId the value id
         */
        private TypeOfTelephone(String value, String valueId) {
            this.value = value;
            this.valueId = valueId;
        }
        
        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }
        
        /**
         * Returns the value id.
         * 
         * @return the (semantic) id of the value
         */
        public String getValueId() {
            return valueId;
        }
        
    }

    /**
     * Denotes the type of a fax number.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum TypeOfFaxNumber {
        
        OFFICE("office", irdi("0173-1#07-AAS754#001")),
        SECRETARY("secretary", irdi("0173-1#07-AAS756#001")),
        HOME("home", irdi("0173-1#07-AAS758#001"));
        
        private String value;
        private String valueId;
        
        /**
         * Creates a constant.
         * 
         * @param value the value
         * @param valueId the value id
         */
        private TypeOfFaxNumber(String value, String valueId) {
            this.value = value;
            this.valueId = valueId;
        }
        
        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }
        
        /**
         * Returns the value id.
         * 
         * @return the (semantic) id of the value
         */
        public String getValueId() {
            return valueId;
        }
        
    }

    /**
     * Denotes the type of an email address.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum TypeOfEmailAddress {
        
        OFFICE("office", irdi("0173-1#07-AAS754#001")),
        SECRETARY("secretary", irdi("0173-1#07-AAS756#001")),
        SUBSTITUTE("substitute", irdi("0173-1#07-AAS757#001")),
        HOME("home", irdi("0173-1#07-AAS758#001"));
        
        private String value;
        private String valueId;
        
        /**
         * Creates a constant.
         * 
         * @param value the value
         * @param valueId the value id
         */
        private TypeOfEmailAddress(String value, String valueId) {
            this.value = value;
            this.valueId = valueId;
        }
        
        /**
         * Returns the value.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }
        
        /**
         * Returns the value id.
         * 
         * @return the (semantic) id of the value
         */
        public String getValueId() {
            return valueId;
        }
        
    }
    
    /**
     * The contact information builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class ContactInformationBuilder extends DelegatingSubmodelElementCollectionBuilder {

        private int languageCount;
        private int ipCommunicationCount;
        
        /**
         * Creates a builder instance.
         * 
         * @param parent the parent builder
         * @param nr the contact information number
         */
        protected ContactInformationBuilder(SubmodelBuilder parent, int nr) {
            super(parent.createSubmodelElementCollectionBuilder(getCountingIdShort("ContactInformation", nr), 
                false, false));
            setSemanticId(iri("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation"));
        }

        /**
         * Sets the function of a contact person in a process.
         * 
         * @param role the role
         * @return <b>this</b>
         */
        public ContactInformationBuilder setRoleOfContactPerson(RoleOfContactPerson role) {
            return createProperty(this, "RoleOfContactPerson", irdi("0173-1#02-AAO204#003"), 
                Type.STRING, role.getValueId()); // preferable, see spec
        }

        /**
         * Sets the national code of the contact.
         * 
         * @param code the code of a country according to ISO 3166-1
         * @return <b>this</b>
         */
        public ContactInformationBuilder setNationalCode(LangString code) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "NationalCode", 
                irdi("0173-1#02-AAO134#002"), code);
        }
        
        /**
         * Adds an available language of the contact.
         * 
         * @param language the language according to ISO 639-1
         * @return <b>this</b>
         */
        public ContactInformationBuilder addLanguage(String language) {
            return createProperty(this, getCountingIdShort("Language", ++languageCount), 
                iri("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/Language"), 
                    Type.STRING, language);
        }

        /**
         * Adds the time zone of the contact.
         * 
         * @param timeZone the tine zone according to ISO 8601
         * @return <b>this</b>
         */
        public ContactInformationBuilder setTimeZone(String timeZone) {
            return createProperty(this, "TimeZone", 
                iri("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/TimeZone"), 
                    Type.STRING, timeZone);
        }

        /**
         * Sets the town or city of the contact.
         * 
         * @param cityTown the city/town 
         * @return <b>this</b>
         */
        public ContactInformationBuilder setCityTown(LangString cityTown) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "CityTown", 
                irdi("0173-1#02-AAO132#002"), cityTown);
        }

        /**
         * Sets the company of the contact.
         * 
         * @param company the company
         * @return <b>this</b>
         */
        public ContactInformationBuilder setCompany(LangString company) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "Company", 
                irdi("0173-1#02-AAW001#001"), company);
        }

        /**
         * Sets the department of the contact.
         * 
         * @param department the department
         * @return <b>this</b>
         */
        public ContactInformationBuilder setDepartment(LangString department) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "Department", 
                irdi("0173-1#02-AAO127#003"), department);
        }
        
        /**
         * Creates a builder for the optional phone entry.
         * 
         * @param telephoneNumber the telephone number
         * @return the builder
         */
        public PhoneBuilder createPhoneBuilder(LangString telephoneNumber) {
            return new PhoneBuilder(this, telephoneNumber);
        }

        /**
         * Creates a builder for the optional fax entry.
         * 
         * @param faxNumber the fax number
         * @return the builder
         */
        public FaxBuilder createFaxBuilder(LangString faxNumber) {
            return new FaxBuilder(this, faxNumber);
        }

        /**
         * Creates a builder for the optional email entry.
         * 
         * @param emailAddress address the email address
         * @return the builder
         */
        public EmailBuilder createEmailBuilder(String emailAddress) {
            return new EmailBuilder(this, emailAddress);
        }

        /**
         * Creates a builder for optional IPCommunication entries.
         * 
         * @param webSiteAddress the address of the communication web site
         * @return the builder
         */
        public IPCommunicationBuilder createIPCommunicationBuilder(String webSiteAddress) {
            return new IPCommunicationBuilder(this, ++ipCommunicationCount, webSiteAddress);
        }
        
        /**
         * Sets the street of the contact.
         * 
         * @param street the street
         * @return <b>this</b>
         */
        public ContactInformationBuilder setStreet(LangString street) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "Street", 
                irdi("0173-1#02-AAO128#002"), street);
        }

        /**
         * Sets the zip code of the contact.
         * 
         * @param zipCode the zip code
         * @return <b>this</b>
         */
        public ContactInformationBuilder setZipcode(LangString zipCode) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "Zipcode", 
                irdi("0173-1#02-AAO129#002"), zipCode);
        }

        /**
         * Sets the PO box of the contact.
         * 
         * @param pOBox the PO box
         * @return <b>this</b>
         */
        public ContactInformationBuilder setPOBox(LangString pOBox) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "POBox", 
                irdi("0173-1#02-AAO130#002"), pOBox);
        }

        /**
         * Sets the zip code of the PO box of the contact.
         * 
         * @param zipCodeOfPOBox the zip code of the PO box
         * @return <b>this</b>
         */
        public ContactInformationBuilder setZipCodeOfPOBox(LangString zipCodeOfPOBox) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "ZipCodeOfPOBox", 
                irdi("0173-1#02-AAO131#002"), zipCodeOfPOBox);
        }

        /**
         * Sets the state/county of the contact.
         * 
         * @param stateCounty the state/county
         * @return <b>this</b>
         */
        public ContactInformationBuilder setStateCounty(LangString stateCounty) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "StateCounty", 
                irdi("0173-1#02-AAO133#002"), stateCounty);
        }        

        /**
         * Sets the name of the contact.
         * 
         * @param nameOfContact the name of the contact
         * @return <b>this</b>
         */
        public ContactInformationBuilder setNameOfContact(LangString nameOfContact) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "NameOfContact", 
                irdi("0173-1#02-AAO205#002"), nameOfContact);
        }        

        /**
         * Sets the first name of the contact.
         * 
         * @param firstName the first name of the contact
         * @return <b>this</b>
         */
        public ContactInformationBuilder setFirstName(LangString firstName) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "FirstName", 
                irdi("0173-1#02-AAO206#002"), firstName);
        }        

        /**
         * Sets the middle names of the contact.
         * 
         * @param middleNames the middle names of the contact
         * @return <b>this</b>
         */
        public ContactInformationBuilder setMiddleNames(LangString middleNames) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "MiddleNames", 
                irdi("0173-1#02-AAO207#002"), middleNames);
        }        

        /**
         * Sets the title of the contact.
         * 
         * @param title the title of the contact (common, formal, religious, or other title preceding a contact
         *   person's name)
         * @return <b>this</b>
         */
        public ContactInformationBuilder setTitle(LangString title) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "Title", 
                irdi("0173-1#02-AAO208#003"), title);
        }        

        /**
         * Sets the academic title of the contact.
         * 
         * @param title the title of the contact (academic title preceding a contact person's name)
         * @return <b>this</b>
         */
        public ContactInformationBuilder setAcademicTitle(LangString title) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "AcademicTitle", 
                irdi("0173-1#02-AAO209#003"), title);
        }        

        /**
         * Sets additional information of the contact person.
         * 
         * @param info additional information of the contact person
         * @return <b>this</b>
         */
        public ContactInformationBuilder setFurtherDetailsOfContact(LangString info) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "FurtherDetailsOfContact", 
                irdi("0173-1#02-AAO210#002"), info);
        }        
        
        /**
         * Sets the web site address where information about the product or contact is given.
         * 
         * @param address the web site address
         * @return <b>this</b>
         */
        public ContactInformationBuilder setAddressOfAdditionalLink(String address) {
            return createProperty(this, "AddressOfAdditionalLink", irdi("0173-1#02-AAQ326#002"), 
                Type.STRING, address);
        }
        
        

        /**
         * Builder for the phone entry.
         * 
         * @author Holger Eichelberger, SSE
         */
        public class PhoneBuilder extends DelegatingSubmodelElementCollectionBuilder {

            /**
             * Creates an instance.
             * 
             * @param parent the parent builder
             * @param telephoneNumber the telephone number
             */
            protected PhoneBuilder(SubmodelElementCollectionBuilder parent, LangString telephoneNumber) {
                super(parent.createSubmodelElementCollectionBuilder("", false, false));
                setSemanticId(iri("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/"
                    + "ContactInformation/Phone"));
                createMultiLanguageProperty(this, createMultiLanguageProperties, "TelephoneNumber", 
                    irdi("0173-1#02-AAO136#002"), telephoneNumber);
            }
            
            /**
             * Sets the type of telephone.
             * 
             * @param type the type of telephone
             * @return <b>this</b>
             */
            public PhoneBuilder setTypeOfTelephone(TypeOfTelephone type) {
                return createProperty(this, "TelephoneNumber", 
                    irdi("0173-1#02-AAO137#003"), Type.STRING, type.getValueId()); // preferable, see spec
            }        

            /**
             * Sets the specification of the available time window.
             * 
             * @param timeWindow specification of the available time window
             * @return <b>this</b>
             */
            public PhoneBuilder setAvailableTime(LangString timeWindow) {
                return createMultiLanguageProperty(this, createMultiLanguageProperties, "FurtherDetailsOfContact", 
                    iri("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation"
                        + "/AvailableTime/"), timeWindow);
            }        
            
        }

        /**
         * Builder for the fax entry.
         * 
         * @author Holger Eichelberger, SSE
         */
        public class FaxBuilder extends DelegatingSubmodelElementCollectionBuilder {

            /**
             * Creates an instance.
             * 
             * @param parent the parent builder
             * @param faxNumber the fax number
             */
            protected FaxBuilder(SubmodelElementCollectionBuilder parent, LangString faxNumber) {
                super(parent.createSubmodelElementCollectionBuilder("", false, false));
                setSemanticId(irdi("0173-1#02-AAQ834#005"));
                createMultiLanguageProperty(this, createMultiLanguageProperties, "FaxNumber", 
                    irdi("0173-1#02-AAO195#002"), faxNumber);
            }
            
            /**
             * Sets the type of fax number.
             * 
             * @param type the type of fax number
             * @return <b>this</b>
             */
            public FaxBuilder setTypeOfFaxNumber(TypeOfFaxNumber type) {
                return createProperty(this, "TypeOfFaxNumber", // unclear, in spec it's a (index) number?
                    irdi("0173-1#02-AAO196#003"), Type.STRING, type.getValueId()); 
            }
            
        }

        /**
         * Builder for the email entry.
         * 
         * @author Holger Eichelberger, SSE
         */
        public class EmailBuilder extends DelegatingSubmodelElementCollectionBuilder {

            /**
             * Creates an instance.
             * 
             * @param parent the parent builder
             * @param emailAddress address the email address
             */
            protected EmailBuilder(SubmodelElementCollectionBuilder parent, String emailAddress) {
                super(parent.createSubmodelElementCollectionBuilder("", false, false));
                setSemanticId(irdi("0173-1#02-AAQ836#005"));
                createProperty(this, "EmailAddress",
                    irdi("0173-1#02-AAO198#002"), Type.STRING, emailAddress); 
            }
            
            /**
             * Sets the public part of an unsymmetrical key pair to sign or encrypt text or messages.
             * 
             * @param key the public part of an unsymmetrical key pair to sign or encrypt text or messages
             * @return <b>this</b>
             */
            public EmailBuilder setPublicKey(LangString key) {
                return createMultiLanguageProperty(this, createMultiLanguageProperties, "PublicKey", 
                    irdi("0173-1#02-AAO200#002"), key); 
            }
            
            /**
             * Sets the characterization of an e-mail address according to its location or usage.
             * 
             * @param type the characterization of an e-mail address according to its location or usage
             * @return <b>this</b>
             */
            public EmailBuilder setTypeOfFaxNumber(TypeOfEmailAddress type) {
                return createProperty(this, "TypeOfEmailAddress", 
                    irdi("0173-1#02-AAO199#003"), Type.STRING, type.getValueId()); 
            }

            /**
             * Sets the characterization of a public key according to its encryption process.
             * 
             * @param type the characterization of a public key according to its encryption process
             * @return <b>this</b>
             */
            public EmailBuilder setTypeOfPublicKey(LangString type) {
                return createMultiLanguageProperty(this, createMultiLanguageProperties, "TypeOfPublicKey", 
                    irdi("0173-1#02-AAO201#002"), type); 
            }

        }

        /**
         * Builder for the IPcommunication entry.
         * 
         * @author Holger Eichelberger, SSE
         */
        public class IPCommunicationBuilder extends DelegatingSubmodelElementCollectionBuilder {

            /**
             * Creates an instance.
             * 
             * @param parent the parent builder
             * @param nr the counting number
             * @param webSiteAddress the address of the communication web site
             */
            protected IPCommunicationBuilder(SubmodelElementCollectionBuilder parent, int nr, String webSiteAddress) {
                super(parent.createSubmodelElementCollectionBuilder(getCountingIdShort("", nr), false, false));
                setSemanticId(iri("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/"
                    + "IPCommunication/"));
                createProperty(this, "AddressOfAdditionalLink",
                    irdi("0173-1#02-AAQ326#002"), Type.STRING, webSiteAddress); 
            }

            /**
             * Sets the characterization of an IP-based communication channel.
             * 
             * @param type the characterization of an IP-based communication channel
             * @return <b>this</b>
             */
            public IPCommunicationBuilder setTypeOfCommunication(String type) {
                return createProperty(this, "TypeOfCommunication", 
                    iri("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/"
                        + "IPCommunication/TypeOfCommunication"), Type.STRING, type); 
            }

            /**
             * Sets the specification of the available time window.
             * 
             * @param timeWindow specification of the available time window
             * @return <b>this</b>
             */
            public IPCommunicationBuilder setAvailableTime(LangString timeWindow) {
                return createMultiLanguageProperty(this, createMultiLanguageProperties, "AvailableTime", 
                    iri("https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/"
                        + "AvailableTime/"), timeWindow);
            }        
            
        }

    }

}
