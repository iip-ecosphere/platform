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

package de.iip_ecosphere.platform.examples.hm22.carAas;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.iip_ecosphere.platform.examples.hm22.carAas.CarsYaml.Car;

/**
 * Reads/prints a {@link CarsYaml}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CarsYamlPrinter {
    
    /**
     * Reads/prints a car yaml.
     * 
     * @param args the command line arguments, first is file name
     */
    public static void main(String[] args) {
        String fileName;
        if (args.length == 0) {
            fileName = "src/main/resources/cars.yml";
            System.out.println("No file name given, using " + fileName);
        } else {
            fileName = args[0];
        }
        try {
            FileInputStream fis = new FileInputStream(fileName);
            CarsYaml cars = CarsYaml.readFromYaml(fis);
            for (Car c : cars.getCars()) {
                System.out.println(ReflectionToStringBuilder.toString(c, ToStringStyle.MULTI_LINE_STYLE));
            }
            fis.close();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
    
}
