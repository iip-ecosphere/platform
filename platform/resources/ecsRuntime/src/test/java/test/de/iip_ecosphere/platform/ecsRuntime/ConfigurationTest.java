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

package test.de.iip_ecosphere.platform.ecsRuntime;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.Configuration;

/**
 * Tests {@link Configuration}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationTest {
    
    /**
     * Tests the configuration.
     */
    @Test
    public void testConfiguration() throws IOException {
        // does not exist
        try {
            Configuration.readFromYaml(Configuration.class, "me.yml");
            Assert.fail("No exception");
        } catch (IOException e) {
            // ok
        }
        
        // for now no configuration content
        Configuration cfg = Configuration.readFromYaml();
        Assert.assertNotNull(cfg);
    }

}
