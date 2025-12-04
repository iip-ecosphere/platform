/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport.spring.binder.generic;

import java.io.File;

import org.junit.BeforeClass;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.transport.spring.binder.generic.GenericClient;
import de.iip_ecosphere.platform.transport.spring.binder.generic.GenericConfiguration;

/**
 * Runs {@link GenericMessageBinderTest} with TLS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class GenericMessageBinderTlsTest extends GenericMessageBinderTest {

    /**
     * Sets the configuration directory to <code>secCfg</code>.
     */
    @BeforeClass
    public static void init() {
        ServerAddress addr = resetAddr();
        setSecCfg(new File("./src/test/secCfg"));
        GenericClient client = GenericClient.getLastInstance();
        GenericMessageBinderTest.init();
        if (null != client) { // called again, reset client in binder
            GenericConfiguration cfg = client.getConfiguration();
            cfg.setPort(addr.getPort());
            cfg.setKeystoreKey(getKeystoreKey());
            client.createClient(cfg);
        }
    }
    
}
