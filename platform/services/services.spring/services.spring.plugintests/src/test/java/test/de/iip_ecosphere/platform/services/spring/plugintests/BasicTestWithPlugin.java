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

package test.de.iip_ecosphere.platform.services.spring.plugintests;

import test.de.iip_ecosphere.platform.transport.TestWithQpid;

/**
 * Basic test with required plugins.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicTestWithPlugin extends TestWithQpid {
    
    static {
        enableLocalPlugins(false); // we need them relocated and appended/mixed
        setInstallDir("target/plugins");
        addPluginLocation("services", "services.spring", "support.log-slf4j-simple");
        addPluginLocation("support", "support.log-slf4j-simple");
        addPluginLocation("support", "support.processInfo-oshi");
        addPluginLocation("support", "support.websocket-websocket");
        addPluginLocation("support", "support.yaml-snakeyaml");
        addPluginLocation("support", "support.commons-apache");
        addPluginLocation("support", "support.bytecode-bytebuddy");
        addPluginLocation("support", "support.json-jackson");
        addPluginLocation("transport", "transport.amqp");
        loadPlugins();
    }

}
