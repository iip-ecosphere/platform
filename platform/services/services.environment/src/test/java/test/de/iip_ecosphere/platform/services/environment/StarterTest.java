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

package test.de.iip_ecosphere.platform.services.environment;

import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import org.junit.Assert;

/**
 * Tests the {@link Starter}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StarterTest {
    
    /**
     * Tests the starter.
     */
    @Test
    public void testStarter() {
        int port = NetUtils.getEphemeralPort();
        Starter.parse("file", "--myParam", 
            Starter.PARAM_PREFIX + Starter.PARAM_IIP_PROTOCOL + Starter.PARAM_VALUE_SEP + AasFactory.DEFAULT_PROTOCOL, 
            Starter.PARAM_PREFIX + Starter.PARAM_IIP_PORT + Starter.PARAM_VALUE_SEP + port,
            "--endParam");
        Assert.assertNotNull(Starter.getProtocolBuilder());
        Starter.start();
        Starter.shutdown();
    }

}
