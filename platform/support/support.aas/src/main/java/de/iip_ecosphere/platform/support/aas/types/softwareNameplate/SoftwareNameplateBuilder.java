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

package de.iip_ecosphere.platform.support.aas.types.softwareNameplate;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;
import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import java.net.URI;

import javax.xml.datatype.XMLGregorianCalendar;

import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.types.contactInformations.ContactInformationsBuilder.ContactInformationBuilder;

/**
 * Support for <a href="https://industrialdigitaltwin.org/wp-content/uploads/2023/08/
 * IDTA-02007-1-0_Submodel_Software-Nameplate.pdf">IDTA 2007-1-0 Nameplate for Software in Manufacturing</a>.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SoftwareNameplateBuilder extends DelegatingSubmodelBuilder {

    private boolean createMultiLanguageProperties;

    /**
     * Creates the nameplate builder.
     * 
     * @param parent the parent AAS
     * @param identifier the submodel identifier
     * @param createMultiLanguageProperties whether multi-language properties shall be created, taints compliance 
     *     if {@code false}
     */
    public SoftwareNameplateBuilder(AasBuilder parent, String identifier, boolean createMultiLanguageProperties) {
        super(parent.createSubmodelBuilder("SoftwareNameplate", identifier));
        this.createMultiLanguageProperties = createMultiLanguageProperties;
        setSemanticId(iri("https://admin-shell.io/idta/SoftwareNameplate/1/0"));
    }

    /**
     * Creates the software nameplate type builder.
     * 
     * @return the builder
     */
    public SoftwareNameplateTypeBuilder createSoftwareNameplateTypeBuilder() {
        return new SoftwareNameplateTypeBuilder(this);
    }
    
    /**
     * Creates the software nameplate instance builder.
     * 
     * @return the builder
     */
    public SoftwareNameplateInstanceBuilder createSoftwareNameplateInstanceBuilder() {
        return new SoftwareNameplateInstanceBuilder(this);
    }
    
    /**
     * Defines the software nameplate type builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class SoftwareNameplateTypeBuilder extends DelegatingSubmodelElementCollectionBuilder {
        
        private boolean hasUriOfTheProduct;
        private boolean hasManufacturerName;
        private boolean hasManufacturerProductDesignation;
        private boolean hasVersion;
        private boolean hasReleaseDate;
        private boolean hasBuildDate;

        /**
         * Creates the builder.
         * 
         * @param parent the parent builder
         */
        protected SoftwareNameplateTypeBuilder(SoftwareNameplateBuilder parent) {
            super(parent.createSubmodelElementCollectionBuilder("SoftwareNameplate_Type", false, false));
            setSemanticId(iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/ SoftwareNameplateType"));
        }
        
        /**
         * Sets the unique global identifier of the product.
         * 
         * @param uri the identifier
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setUriOfTheProduct(String uri) {
            hasUriOfTheProduct = true;
            return createProperty(this, "URIOfTheProduct", irdi("0173-#02-AAY811#001"), Type.STRING, uri);
        }

        /**
         * Sets the manufacturer name.
         * 
         * @param name the manufacturer name
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setManufacturerName(LangString name) {
            hasManufacturerName = true;
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "ManufacturerName", 
                irdi("0173-1#02-AAW001#001"), name);
        }
        
        /**
         * Sets the manufacturer product designation.
         * 
         * @param designation the designation
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setManufacturerProductDesignation(LangString designation) {
            hasManufacturerProductDesignation = true;
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "ManufacturerProductDesignation", 
                irdi("0173-1#02-AAW338#001"), designation);
        }

        /**
         * Sets the manufacturer product designation.
         * 
         * @param description the description
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setManufacturerProductDescription(LangString description) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "ManufacturerProductDescription", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateType/ManufacturerProductDescription"), description);
        }

        /**
         * Sets the manufacturer product family.
         * 
         * @param family the family, 2nd level of a 3 level manufacturer specific product hierarchy
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setManufacturerProductFamily(LangString family) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "ManufacturerProductFamily", 
                irdi("0173-1#02-AAU731#001"), family);
        }

        /**
         * Sets the manufacturer product type.
         * 
         * @param type the type, characteristic to differentiate between different products of a product family or 
         *   special variants
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setManufacturerProductType(LangString type) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "ManufacturerProductType", 
                irdi("0173-1#02-AAO057#002"), type);
        }

        /**
         * Sets the software type.
         * 
         * @param type the software type
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setSoftwareType(String type) {
            return createProperty(this, "SoftwareType", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateType/SoftwareType"), Type.STRING, type);
        }

        /**
         * Sets the software version.
         * 
         * @param version the software version
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setVersion(String version) {
            hasVersion = true;
            return createProperty(this, "Version", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateType/Version"), Type.STRING, version);
        }

        /**
         * Sets the software version name.
         * 
         * @param name the version name
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setVersionName(LangString name) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "VersionName", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                        + "/SoftwareNameplateType/VersionName"), name);
        }

        /**
         * Sets the software version info.
         * 
         * @param info the version info
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setVersionInfo(LangString info) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "VersionInfo", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                        + "/SoftwareNameplateType/VersionInfo"), info);
        }

        /**
         * Sets the software release date.
         * 
         * @param date the software release date
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setReleaseDate(XMLGregorianCalendar date) {
            hasReleaseDate = true;
            return createProperty(this, "ReleaseDate", // spec shall be DATE not DATE_TIME, spec AASX uses DATE_TIME
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateType/ReleaseDate"), Type.DATE_TIME, date);
        }

        /**
         * Sets the release notes.
         * 
         * @param notes the release notes
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setReleaseNotes(LangString notes) {
            return createMultiLanguageProperty(this, createMultiLanguageProperties, "ReleaseNotes", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                        + "/SoftwareNameplateType/ReleaseNotes"), notes);
        }

        /**
         * Sets the software build date.
         * 
         * @param date the software build date
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setBuildDate(XMLGregorianCalendar date) {
            hasBuildDate = true;
            return createProperty(this, "BuildDate", // spec shall be DATE not DATE_TIME, spec AASX uses DATE_TIME
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateType/BuildDate"), Type.DATE_TIME, date);
        }

        /**
         * Sets the installation URI.
         * 
         * @param uri the resource where the software is being provided by the manufacturer
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setInstallationURI(URI uri) {
            return createProperty(this, "InstallationURI", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateType/InstallationURI"), Type.ANY_URI, uri);
        }

        /**
         * Sets the installation URI.
         * 
         * @param file the installation code
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setInstallationFile(String file) {
            // spec: shall be a property and a blob, spec AAS uses property of type string
            return createProperty(this, "InstallationFile",  
                    iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                        + "/SoftwareNameplateType/InstallationFile"), Type.STRING, file);
        }

        /**
         * Sets the installer type.
         * 
         * @param type the installer type, the type of the installation package
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setInstallerType(String type) {
            return createProperty(this, "InstallerType", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateType/InstallerType"), Type.STRING, type);
        }

        /**
         * Sets the installation checksum.
         * 
         * @param checksum the checksum for the software at {@link #setInstallationURI(URI) the installation URI}
         * @return <b>this</b>
         */
        public SoftwareNameplateTypeBuilder setInstallationChecksum(String checksum) {
            return createProperty(this, "InstallationChecksum", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateType/InstallationChecksum"), Type.STRING, checksum);
        }

        @Override
        public SubmodelElementCollection build() {
            assertThat(hasUriOfTheProduct, "UriOfTheProduct must be set");
            assertThat(hasManufacturerName, "ManufacturerName must be set");
            assertThat(hasManufacturerProductDesignation, "ManufacturerProductDesignation must be set");
            assertThat(hasVersion, "Version must be set");
            assertThat(hasReleaseDate, "ReleaseDate must be set");
            assertThat(hasBuildDate, "BuildDate must be set");
            return super.build();
        }
        
    }

    /**
     * Defines the software nameplate instance builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class SoftwareNameplateInstanceBuilder extends DelegatingSubmodelElementCollectionBuilder {

        /**
         * Creates the builder.
         * 
         * @param parent the parent builder
         */
        protected SoftwareNameplateInstanceBuilder(SoftwareNameplateBuilder parent) {
            super(parent.createSubmodelElementCollectionBuilder("SoftwareNameplate_Instance", false, false));
            setSemanticId(iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplateInstance"));
        }

        /**
         * Sets the serial number.
         * 
         * @param serial the serial number
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setSerialNumber(String serial) {
            return createProperty(this, "SerialNumber", 
                irdi("0173-1#02-AAM556#002"), Type.STRING, serial);
        }

        /**
         * Sets the instance name.
         * 
         * @param name the instance name
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setInstanceName(String name) {
            return createProperty(this, "InstanceName", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplateInstance/InstanceName"), 
                Type.STRING, name);
        }

        /**
         * Sets the installed version.
         * 
         * @param version the installed version
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setInstalledVersion(String version) {
            return createProperty(this, "InstalledVersion", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplateInstance/InstalledVersion"), 
                Type.STRING, version);
        }
        
        /**
         * Sets the installation date.
         * 
         * @param date the installation date
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setInstallationDate(XMLGregorianCalendar date) {
            return createProperty(this, "InstallationDate", // spec shall be DATE not DATE_TIME,spec AASX uses DATE_TIME
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateInstance/InstallationDate"), Type.DATE_TIME, date);
        }

        /**
         * Sets the path to the installed software.
         * 
         * @param uri the path to the installed software
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setInstallationPath(URI uri) {
            return createProperty(this, "InstallationPath", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateInstance/InstallationPath"), Type.ANY_URI, uri);
        }

        /**
         * Sets the path to the installation files used in this instance.
         * 
         * @param uri the path to the installation files
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setInstallationSource(URI uri) {
            return createProperty(this, "InstallationSource", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateInstance/InstallationSource"), Type.ANY_URI, uri);
        }

        /**
         * Sets the processor architecture this instance is installed on.
         * 
         * @param arch the processor architecture
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setInstalledOnArchitecture(String arch) {
            return createProperty(this, "InstalledOnArchitecture", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateInstance/InstalledOnArchitecture"), Type.STRING, arch);
        }

        /**
         * Sets the operating system this instance is installed on.
         * 
         * @param os the operating system
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setInstalledOnOS(String os) {
            return createProperty(this, "InstalledOnOS", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateInstance/InstalledOnOS"), Type.STRING, os);
        }

        /**
         * Sets the host system in case of a virtual installation.
         * 
         * @param host the host
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setInstalledOnHost(String host) {
            return createProperty(this, "InstalledOnHost", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateInstance/InstalledOnHost"), Type.STRING, host);
        }
        
        /**
         * Creates the installed modules builder.
         * 
         * @return the builder
         */
        public SubmodelElementCollectionBuilder createInstalledModulesBuilder() {
            return createSubmodelElementCollectionBuilder("InstalledModules", false, false);
        }

        /**
         * Creates the installed modules builder.
         * 
         * @return the builder
         */
        public ConfigurationPathsBuilder createConfigurationPathsBuilder() {
            return new ConfigurationPathsBuilder(this);
        }
        
        /**
         * The contact of the person responsible for the installation.
         * 
         * @return the contact
         */
        public ContactInformationBuilder createContactInformationBuilder() {
            return new ContactInformationBuilder(this, "Contact", createMultiLanguageProperties);
        }

        /**
         * Sets the actual service level agreements.
         * 
         * @param info the information
         * @return <b>this</b>
         */
        public SoftwareNameplateInstanceBuilder setSLAInformation(String info) {
            return createProperty(this, "SLAInformation", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateInstance/SLAInformation"), Type.STRING, info);
        }

    }

    /**
     * A configuration paths builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class ConfigurationPathsBuilder extends DelegatingSubmodelElementCollectionBuilder {

        private int configurationPathCounter;
        
        /**
         * Creates the builder.
         * 
         * @param parent the parent builder
         */
        private ConfigurationPathsBuilder(SoftwareNameplateInstanceBuilder parent) {
            super(parent.createSubmodelElementCollectionBuilder("ConfigurationPaths", false, false));
        }

        /**
         * Creates a configuration path builder.
         * 
         * @return a configuration path builder
         */
        public ConfigurationPathBuilder createConfigurationPathBuilder() {
            // spec unclear... contains a single entry with [1..*]
            return new ConfigurationPathBuilder(this, ++configurationPathCounter);
        }

        @Override
        public SubmodelElementCollection build() {
            assertThat(configurationPathCounter >= 1, "Must have at least one configuration path");
            return super.build();
        }

    }

    /**
     * A configuration path builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class ConfigurationPathBuilder extends DelegatingSubmodelElementCollectionBuilder {

        /**
         * Creates the builder.
         * 
         * @param parent the parent builder
         * @param nr the instance number
         */
        private ConfigurationPathBuilder(ConfigurationPathsBuilder parent, int nr) {
            super(parent.createSubmodelElementCollectionBuilder(
                getCountingIdShort("ConfigurationPath", nr), // nr not explicitly stated in spec ??
                false, false));
            setSemanticId(iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                + "/SoftwareNameplateInstance/ConfigurationPath"));
        }
        
        /**
         * Sets the path to the configuration.
         * 
         * @param uri the path to the configuration
         * @return <b>this</b>
         */
        public ConfigurationPathBuilder setConfigurationURI(URI uri) {
            return createProperty(this, "ConfigurationURI", 
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateInstance/ConfigurationURI"), Type.ANY_URI, uri);
        }

        /**
         * Sets the type of the configuration.
         * 
         * @param type the type of the configuration
         * @return <b>this</b>
         */
        public ConfigurationPathBuilder setConfigurationType(String type) {
            return createProperty(this, "ConfigurationType", // spec UML diagram says integer
                iri("https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate"
                    + "/SoftwareNameplateInstance/ConfigurationType"), Type.STRING, type);
        }

    }

}
