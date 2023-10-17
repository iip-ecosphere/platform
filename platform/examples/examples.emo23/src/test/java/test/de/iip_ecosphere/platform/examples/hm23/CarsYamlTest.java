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

package test.de.iip_ecosphere.platform.examples.hm23;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.examples.hm23.carAas.CarsAasServer;
import de.iip_ecosphere.platform.examples.hm23.carAas.CarsYaml;
import de.iip_ecosphere.platform.examples.hm23.carAas.CarsYaml.Car;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Tests {@link CarsYaml}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CarsYamlTest {
    
    /**
     * Tests the cars yaml.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testYaml() throws IOException {
        FileInputStream fis = new FileInputStream("src/test/resources/cars.yml");
        CarsYaml cars = CarsYaml.readFromYaml(fis);
        Assert.assertNotNull(cars);
        Assert.assertEquals(2, cars.getCars().size());
        System.out.println("TestYaml:");
        for (Car c : cars.getCars()) {
            System.out.println(ReflectionToStringBuilder.toString(c));
        }
        fis.close();
    }
    
    /**
     * Tests a server instance.
     */
    @Test
    public void serverTest() {
        Server server = new CarsAasServer(new String[] {"--port=10002"});
        server.start();
        TimeUtils.sleep(2000);
        server.stop(true);
    }

}
