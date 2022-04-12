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

package de.iip_ecosphere.platform.support.iip_aas;

/**
 * Describes static information about an application, e.g., taken from the configuration. May be read from yaml,
 * therefore already in snakeyaml style.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ApplicationSetup {

    private String id;
    private String name;
    private Version version;
    private String description = "";
    private String manufacturerName;
    private Address address = new Address();

    /**
     * Represents part of an address.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Address {
        
        private String department = "";
        private String street = "";
        private String zipCode = "";
        // ...
        private String cityTown = "";

        /**
         * Creates an address instance. [snakeyaml]
         */
        public Address() {
        }

        /**
         * Returns the department.
         * 
         * @return the department
         */
        public String getDepartment() {
            return department;
        }

        /**
         * Changes the department. [snakeyaml]
         * 
         * @param department the department
         */
        public void setDepartment(String department) {
            this.department = department;
        }

        /**
         * Returns the street.
         * 
         * @return the street
         */
        public String getStreet() {
            return street;
        }

        /**
         * Changes the street. [snakeyaml]
         * 
         * @param street the street
         */
        public void setStreet(String street) {
            this.street = street;
        }

        /**
         * Returns the ZIP code.
         * 
         * @return the ZIP code
         */
        public String getZipCode() {
            return zipCode;
        }

        /**
         * Changes the ZIP code. [snakeyaml]
         * 
         * @param zipCode the ZIP code
         */
        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        /**
         * Returns the city/town.
         * 
         * @return the city/town
         */
        public String getCityTown() {
            return cityTown;
        }

        /**
         * Changes the city/town. [snakeyaml]
         * 
         * @param cityTown the city/town
         */
        public void setCityTown(String cityTown) {
            this.cityTown = cityTown;
        }
        
    }
    
    /**
     * For snakeyaml.
     */
    public ApplicationSetup() {
    }

    /**
     * Copy constructor.
     * 
     * @param setup the instance to copy from
     */
    public ApplicationSetup(ApplicationSetup setup) {
        this.id = setup.id;
        this.name = setup.name;
    }

    /**
     * Returns the application id.
     * 
     * @return the application id
     */
    public String getId() {
        return id;
    }
    
    /**
     * Changes the application id. [snakeyaml]
     * 
     * @param id the application id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the application name.
     * 
     * @return the application name
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the application name. [snakeyaml]
     * 
     * @param name the application name
     */
    public void setName(String name) {
        this.name = name;
    } 

    /**
     * Returns the version of the application.
     * 
     * @return the version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Changes the version. [snakeyaml]
     * 
     * @param version the version
     */
    public void setVersion(Version version) {
        this.version = version;
    } 

    /**
     * Changes the version. [convenience]
     * 
     * @param version the version
     */
    public void setVersion(String version) {
        this.version = new Version(version);
    }

    /**
     * Returns the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Changes the description.
     * 
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the manufacturer name.
     * 
     * @return the manufacturer name
     */
    public String getManufacturerName() {
        return manufacturerName;
    }

    /**
     * Changes the manufacturer name.
     * 
     * @param manufacturerName the manufacturer name
     */
    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    /**
     * Returns the address.
     * 
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Changes the address.
     * 
     * @param address the address
     */
    public void setAddress(Address address) {
        this.address = address;
    } 

}
