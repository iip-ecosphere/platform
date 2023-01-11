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

package test.de.iip_ecosphere.platform.transport.spring.binder.hivemqv5;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.spring.binder.hivemqv5.HivemqV5Client;
import de.iip_ecosphere.platform.transport.spring.binder.hivemqv5.HivemqV5Configuration;

/**
 * Runs {@link HivemqV5MessageBinderTest} with TLS.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(initializers = HivemqV5MessageBinderTest.Initializer.class)
@RunWith(SpringRunner.class)
public class HivemqV5MessageBinderTlsTest extends HivemqV5MessageBinderTest {

    /**
     * Sets the configuration directory to <code>secCfg</code>.
     */
    @BeforeClass
    public static void init() {
        ServerAddress addr = resetAddr(Schema.SSL);
        setSecCfg(new File("./src/test/secCfg"));
        HivemqV5Client client = HivemqV5Client.getLastInstance();
        HivemqV5MessageBinderTest.init();
        if (null != client) { // called again, reset client in binder
            HivemqV5Configuration cfg = client.getConfiguration();
            cfg.setPort(addr.getPort());
            cfg.setKeystoreKey(getKeystoreKey());
            client.createClient(cfg);
        }
    }
    
}
