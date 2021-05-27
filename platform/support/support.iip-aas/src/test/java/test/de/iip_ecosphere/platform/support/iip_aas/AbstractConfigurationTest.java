/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractConfiguration;
import org.junit.Assert;

/**
 * Tests {@link AbstractConfiguration}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractConfigurationTest {
    
    /**
     * Test configuration.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Cfg extends AbstractConfiguration {
        
        private int property;
        
        /**
         * No-arg constructor.
         */
        public Cfg() {
        }

        /**
         * Returns the test property.
         * 
         * @return the property value
         */
        public int getProperty() {
            return property;
        }

        /**
         * Changes the test property.
         * 
         * @param property the new property value
         */
        public void setProperty(int property) {
            this.property = property;
        }
        
    }
    
    /**
     * Invalid configuration class.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Cfg2 extends AbstractConfiguration {

        /**
         * No public constructor.
         * 
         * @param whatever ignored
         */
        public Cfg2(String whatever) {
        }
        
    }
    
    /**
     * Tests the configuration (reading).
     */
    @Test
    public void testConfiguration() throws IOException {
        try {
            Cfg.readFromYaml(Cfg.class, "a");
            Assert.fail("No exception, file does not exist");
        } catch (IOException e) {
            // ok
        }
        try {
            Cfg2.readFromYaml(Cfg2.class, "test.yaml");
            Assert.fail("No exception, class cannot be instantiated");
        } catch (IOException e) {
            // ok
        }

        // readable, without leading /
        Cfg cfg = Cfg.readFromYaml(Cfg.class, "test.yaml");
        Assert.assertNotNull(cfg);
        Assert.assertEquals(42, cfg.getProperty());

        // readable, with leading /
        cfg = Cfg.readFromYaml(Cfg.class, "/test.yaml");
        Assert.assertNotNull(cfg);
        Assert.assertEquals(42, cfg.getProperty());
    }

}
