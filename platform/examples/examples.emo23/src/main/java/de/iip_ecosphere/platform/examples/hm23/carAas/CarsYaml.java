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

package de.iip_ecosphere.platform.examples.hm23.carAas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.setup.AbstractSetup;

/**
 * Represents the cars in the car production AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CarsYaml extends AbstractSetup {

    private List<Car> cars = new ArrayList<Car>();

    /**
     * Represents a single car.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Car {
        
        private String id;
        private String length;
        private String thickness;
        private String weight;
        private String hardwareRevision;
        private int windows;
        private String tiresColor;
        private boolean pattern;
        private String engravingText;
        private String manufacturerLogo;
        private String productImage;
        private String diagnosis;
        
        /**
         * Returns the car id (not the AAS URL).
         * 
         * @return the id
         */
        public String getId() {
            return id;
        }
        
        /**
         * Defines the car id (not the AAS URL). [snakeyaml]
         * @param id the id to set
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Returns the length of the car. 
         * 
         * @return the length of the car, including measurement unit, e.g., "127 mm"
         */
        public String getLength() {
            return length;
        }
        
        /**
         * Defines the length of the car. [snakeyaml]
         * 
         * @param length the length of the car, including measurement unit, e.g., "127 mm"
         */
        public void setLength(String length) {
            this.length = length;
        }
        
        /**
         * Returns the thickness of the car. 
         * 
         * @return the thickness of the car, including measurement unit, e.g., "127 mm"
         */
        public String getThickness() {
            return thickness;
        }

        /**
         * Defines the thickness of the car.  [snakeyaml]
         * 
         * @param thickness the thickness of the car, including measurement unit, e.g., "127 mm"
         */
        public void setThickness(String thickness) {
            this.thickness = thickness;
        }
        
        /**
         * Returns the weight of the car. 
         * 
         * @return the weight of the car, including measurement unit, e.g., "110 g"
         */
        public String getWeight() {
            return weight;
        }

        /**
         * Defines the weight of the car.  [snakeyaml]
         * 
         * @param weight the weight of the car, including measurement unit, e.g., "110 g"
         */
        public void setWeight(String weight) {
            this.weight = weight;
        }
        
        /**
         * Returns the hardware revision.
         * 
         * @return the hardwareRevision the hardware revision, e.g., "MobileFabrik_v0.1"
         */
        public String getHardwareRevision() {
            return hardwareRevision;
        }
        
        /**
         * Defines the hardware revision. [snakeyaml]
         * 
         * @param hardwareRevision the hardwareRevision the hardware revision, e.g., "MobileFabrik_v0.1"
         */
        public void setHardwareRevision(String hardwareRevision) {
            this.hardwareRevision = hardwareRevision;
        }
        
        /**
         * Returns the number of windows of the car.
         * 
         * @return the windows the number of windows
         */
        public int getWindows() {
            return windows;
        }
        
        /**
         * Defines the number of windows of the car. [snakeyaml]
         * 
         * @param windows the windows the number of windows
         */
        public void setWindows(int windows) {
            this.windows = windows;
        }
        
        /**
         * Returns the color of the tires.
         * 
         * @return the color, in English, small caps, usual color names
         */
        public String getTiresColor() {
            return tiresColor;
        }
        
        /**
         * Returns the color of the tires. [snakeyaml]
         * 
         * @param tiresColor the color, in English, small caps, usual color names
         */
        public void setTiresColor(String tiresColor) {
            this.tiresColor = tiresColor;
        }
        
        /**
         * Returns whether the car has an engraving pattern.
         * 
         * @return {@code true} for pattern, {@code false} else
         */
        public boolean isPattern() {
            return pattern;
        }
        
        /**
         * Defines whether the car has an engraving pattern. [snakeyaml]
         * 
         * @param pattern {@code true} for pattern, {@code false} else
         */
        public void setPattern(boolean pattern) {
            this.pattern = pattern;
        }
        
        /**
         * Returns the engraving text of the car.
         * 
         * @return the engravingText, may be empty, shall not be <b>null</b>
         */
        public String getEngravingText() {
            return engravingText;
        }
        
        /**
         * Changes the engraving text of the car. [snakeyaml]
         * 
         * @param engravingText the engravingText, may be empty, shall not be <b>null</b>
         */
        public void setEngravingText(String engravingText) {
            this.engravingText = engravingText;
        }
        
        /**
         * Returns the manufacturer logo.
         * 
         * @return the manufacturerLogo, either empty, a local file name in {@code resources} subject of 
         *     classpath resource loading or a URL
         */
        public String getManufacturerLogo() {
            return manufacturerLogo;
        }

        /**
         * Defines the manufacturer logo. [snakeyaml]
         * 
         * @param manufacturerLogo the manufacturerLogo, either empty, a local file name in {@code resources} subject of
         *     classpath resource loading or a URL
         */
        public void setManufacturerLogo(String manufacturerLogo) {
            this.manufacturerLogo = manufacturerLogo;
        }
        
        /**
         * Returns the product image.
         * 
         * @return the product image, either empty, a local file name in {@code resources} subject of
         *     classpath resource loading or a URL
         */
        public String getProductImage() {
            return productImage;
        }
        
        /**
         * Defines the product image. [snakeyaml]
         * 
         * @param productImage the product image, either empty, a local file name in {@code resources} subject of
         *     classpath resource loading or a URL
         */
        public void setProductImage(String productImage) {
            this.productImage = productImage;
        }

        /**
         * Returns the product image.
         * 
         * @return diagnosis the diagnosis, may be empty
         */
        public String getDiagnosis() {
            return diagnosis;
        }
        
        /**
         * Defines the product diagnosis. [snakeyaml]
         * 
         * @param diagnosis the diagnosis, may be empty
         */
        public void setDiagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
        }
    
    
    }
    
    
    /**
     * Reads a instance from {@code in}. Unknown properties are ignored.
     *
     * @param in the stream to read from (ignored if <b>null</b>)
     * @return the car instance
     * @throws IOException if the data cannot be read, the configuration class cannot be instantiated
     */
    public static CarsYaml readFromYaml(InputStream in) throws IOException {
        return AbstractSetup.readFromYaml(CarsYaml.class, in);
    }
    
    /**
     * Reads from resource {@code cars.yml} or {@code src/main/resources/cars.yml}.
     * 
     * @return the car instance
     * @throws IOException if the data cannot be read, the configuration class cannot be instantiated
     */
    public static CarsYaml readFromYaml() throws IOException {
        InputStream in = null;
        File f = new File("src/main/resources/cars.yml"); // development takes precedence vs. test scope
        if (f.exists()) {
            in = new FileInputStream(f);
        } else {
            in = ResourceLoader.getResourceAsStream("cars.yml");
        }
        return AbstractSetup.readFromYaml(CarsYaml.class, in); // closes in, copes with null
    }

    /**
     * Returns the cars.
     * 
     * @return the cars read from YAML
     */
    public List<Car> getCars() {
        return cars;
    }

    /**
     * Changes the cars. [snakeyaml]
     * 
     * @param cars the cars to set
     */
    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

}
