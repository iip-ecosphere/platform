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

package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractConfiguration;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import org.junit.Assert;

/**
 * Tests {@link TransportSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportSetupTest {
    
    /**
     * Example setup.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Setup {

        private TransportSetup transport;

        /**
         * Returns the transport setup.
         * 
         * @return the transport setup
         */
        public TransportSetup getTransport() {
            return transport;
        }

        /**
         * Defines the transport setup. [snakeyaml]
         * 
         * @param transport the transport setup
         */
        public void setTransport(TransportSetup transport) {
            this.transport = transport;
        }
        
    }
    
    /**
     * Tests the transport setup in a test setup class.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testSetup() throws IOException {
        Setup setup = AbstractConfiguration.readFromYaml(Setup.class, 
            TransportSetupTest.class.getResourceAsStream("/test.yml"));
        Assert.assertNotNull(setup);
        
        TransportSetup transport = setup.getTransport();
        Assert.assertNotNull(transport);
        Assert.assertEquals("me.de", transport.getHost());
        Assert.assertEquals(1234, transport.getPort());
        Assert.assertEquals("pass", transport.getPassword());
        Assert.assertEquals("user", transport.getUser());
        
        TransportParameter param = transport.createParameter();
        Assert.assertNotNull(param);
        Assert.assertEquals("me.de", param.getHost());
        Assert.assertEquals(1234, param.getPort());
        Assert.assertEquals("pass", param.getPassword());
        Assert.assertEquals("user", param.getUser());
    }

}
