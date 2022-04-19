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
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup.Address;

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
    private String productImage = "";
    private String manufacturerLogo = "";
    private Address address = new Address(); // not official
    
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
        this.productImage = setup.productImage;
        this.manufacturerLogo = setup.manufacturerLogo;
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
     * Returns the optional product image.
     * 
     * @return the image (local resolvable name or URI to image)
     */
    public String getProductImage() {
        return productImage;
    }

    /**
     * Changes the optional product image.
     * 
     * @param productImage the image (local resolvable name or URI to image)
     */
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    /**
     * Returns the optional manufacturer logo.
     * 
     * @return the logo (local resolvable name or URI to image)
     */
    public String getManufacturerLogo() {
        return manufacturerLogo;
    }

    /**
     * Defines the optional manufacturer logo.
     * 
     * @param manufacturerLogo the logo (local resolvable name or URI to image)
     */
    public void setManufacturerLogo(String manufacturerLogo) {
        this.manufacturerLogo = manufacturerLogo;
    }
    
    // complete

}
