/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.transport;

/**
 * A test data class.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Product {

    private String description;
    private double price;

    /**
     * Creates a product instance.
     * 
     * @param description the description
     * @param price the price
     */
    public Product(String description, double price) {
        this.description = description;
        this.price = price;
    }

    /**
     * Returns the description of the product.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the price of the product.
     * 
     * @return the price
     */
    public double getPrice() {
        return price;
    }

}
