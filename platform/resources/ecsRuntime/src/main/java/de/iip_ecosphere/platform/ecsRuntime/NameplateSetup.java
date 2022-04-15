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

import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;

/**
 * Describes static information about a device in the style of an ZVEI Digital Nameplate for industrial equipment V1.0.
 * This class is intentionally neither a base nor a derived class of {@link ApplicationSetup} as this class shall
 * (somewhen) follow the spec, {@link ApplicationSetup} shall then follow the software nameplate.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NameplateSetup {

    private String manufacturerName;
    private String manufacturerProductDesignation;
    // TODO complete me
    private String image = "";
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
        // TODO complete me
        private String cityTown = "";

        /**
         * Creates an address instance. [snakeyaml]
         */
        public Address() {
        }
        
        /**
         * Copy constructor.
         * 
         * @param addr the address to copy from
         */
        public Address(Address addr) {
            this.department = addr.department;
            this.cityTown = addr.cityTown;
            this.zipCode = addr.zipCode;
            this.cityTown = addr.cityTown;
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
    public NameplateSetup() {
    }

    /**
     * Copy constructor.
     * 
     * @param setup the instance to copy from
     */
    public NameplateSetup(NameplateSetup setup) {
        this.address = new Address(setup.address);
        this.image = setup.image;
        this.manufacturerName = setup.manufacturerName;
        this.manufacturerProductDesignation = setup.manufacturerProductDesignation;
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
     * Returns the manufacturer product designation.
     * 
     * @return the designation
     */
    public String getManufacturerProductDesignation() {
        return manufacturerProductDesignation;
    }

    /**
     * Changes the manufacturer product designation.
     * 
     * @param manufacturerProductDesignation the designation
     */
    public void setManufacturerProductDesignation(String manufacturerProductDesignation) {
        this.manufacturerProductDesignation = manufacturerProductDesignation;
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

    /**
     * Returns the optional image.
     * 
     * @return the image (serialized image data as string or URI to image)
     */
    public String getImage() {
        return image;
    }

    /**
     * Changes the optional image.
     * 
     * @param image the image (serialized image data as string or URI to image)
     */
    public void setImage(String image) {
        this.image = image;
    }
    
    // complete

}
