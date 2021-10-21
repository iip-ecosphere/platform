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

package test.de.iip_ecosphere.platform.examples.vdw;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.examples.vdw.App;

/**
 * Tests the connector parts/plugins for the VDW OPC UA server.
 * 
 * Plan: Parts of class shall be generated from the configuration model when the connector is used in an application.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OpcUaConnectorTest {
    
    /**
     * Tests the connector parts/plugins for the VDW OPC UA server.
     */
    @Test
    public void testConnector() throws IOException {
        App.main();
    }
    
}
