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

package test.de.iip_ecosphere.platform.transport.spring.binder.hivemqv3;

import java.io.File;

import org.junit.BeforeClass;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.spring.binder.hivemqv3.HivemqV3Client;
import de.iip_ecosphere.platform.transport.spring.binder.hivemqv3.HivemqV3Configuration;

/**
 * Runs {@link AmqpMessageBinderTest} with TLS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HivemqV3MessageBinderTlsTest extends HivemqV3MessageBinderTest {

    /**
     * Sets the configuration directory to <code>secCfg</code>.
     */
    @BeforeClass
    public static void init() {
        ServerAddress addr = resetAddr(Schema.SSL);
        setSecCfg(new File("./src/test/secCfg"));
        HivemqV3Client client = HivemqV3Client.getLastInstance();
        HivemqV3MessageBinderTest.init();
        if (null != client) { // called again, reset client in binder
            HivemqV3Configuration cfg = client.getConfiguration();
            cfg.setPort(addr.getPort());
            cfg.setKeystore(getKeystore());
            cfg.setKeyPassword(getKeystorePassword());
            client.createClient(cfg);
        }
    }
    
}
