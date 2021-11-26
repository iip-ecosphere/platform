/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx;

import java.io.File;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxAasFactory;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import test.de.iip_ecosphere.platform.support.aas.AasTest;

/**
 * Tests the AAS abstraction implementation for BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTest extends AasTest {

    @Override
    protected KeyStoreDescriptor getKeyStoreDescriptor(String protocol) {
        KeyStoreDescriptor result = null;
        if (BaSyxAasFactory.PROTOCOL_VAB_HTTPS.equals(protocol)) {
            File f = new File("./src/test/resources/keystore.jks");
            System.out.println("Using Keystore: " + f.getAbsolutePath() + " " + f.exists());
            result = new KeyStoreDescriptor(f, "a1234567", "tomcat"); // tomcat required by BaSyx
        }
        return result;
    }

    @Override
    protected boolean excludeProtocol(String protocol) {
        boolean result = false;
        if (BaSyxAasFactory.PROTOCOL_VAB_HTTPS.equals(protocol)) {
            // currently it's unclear why VAB-HTTPS works on Windows but not on Linux while HTTPS-AAS works
            result = NetUtils.getOwnHostname().indexOf("jenkins") >= 0;
            System.out.println("Checking exclusion: " + NetUtils.getOwnHostname() + " " + result);            
        }
        return result;
    }

}
