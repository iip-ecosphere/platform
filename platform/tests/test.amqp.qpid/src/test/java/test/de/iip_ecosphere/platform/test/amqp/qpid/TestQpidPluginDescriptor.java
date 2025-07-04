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

package test.de.iip_ecosphere.platform.test.amqp.qpid;

import test.de.iip_ecosphere.platform.transport.TestServerPluginDescriptor;

/**
 * Plugin descriptor for QPID.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestQpidPluginDescriptor extends TestServerPluginDescriptor {

    public static final String PLUGIN_ID = "test-qpid";

    /**
     * Creates the plugin descriptor instance.
     */
    public TestQpidPluginDescriptor() {
        super(PLUGIN_ID, (id, address, instDir) -> new JvmTestServer(id, 
            "test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer", // avoid class loading
            address, instDir, line -> line.contains("Qpid Broker Ready")));
    }

}
