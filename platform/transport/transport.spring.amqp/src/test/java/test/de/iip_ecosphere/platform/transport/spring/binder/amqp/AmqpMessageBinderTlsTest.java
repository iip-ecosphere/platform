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

package test.de.iip_ecosphere.platform.transport.spring.binder.amqp;

import java.io.File;

import org.junit.BeforeClass;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.spring.binder.amqp.AmqpClient;
import de.iip_ecosphere.platform.transport.spring.binder.amqp.AmqpConfiguration;

/**
 * Runs {@link AmqpMessageBinderTest} with TLS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AmqpMessageBinderTlsTest extends AmqpMessageBinderTest {

    /**
     * Sets the configuration directory to <code>secCfg</code>.
     */
    @BeforeClass
    public static void init() {
        ServerAddress addr = resetAddr();
        setSecCfg(new File("./src/test/secCfg"));
        AmqpClient client = AmqpClient.getLastInstance();
        AmqpMessageBinderTest.init();
        if (null != client) { // called again, reset client in binder
            AmqpConfiguration cfg = client.getConfiguration();
            cfg.setPort(addr.getPort());
            cfg.setKeystore(getKeystore());
            cfg.setKeystorePassword(getKeystorePassword());
            client.createClient(cfg);
        }
    }
    
}
