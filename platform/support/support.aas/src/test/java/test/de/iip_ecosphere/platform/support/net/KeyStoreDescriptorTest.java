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

package test.de.iip_ecosphere.platform.support.net;

import java.io.File;

import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link KeyStoreDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class KeyStoreDescriptorTest {

    /**
     * Tests {@link KeyStoreDescriptor}.
     */
    @Test
    public void testKeyStoreDescriptor() {
        File f = new File("keystore.jks");
        String password = "pw";
        String alias = "xyz";
        KeyStoreDescriptor desc = new KeyStoreDescriptor(f, password, alias);
        Assert.assertEquals(f, desc.getPath());
        Assert.assertEquals(password, desc.getPassword());
        Assert.assertEquals(alias, desc.getAlias());
    }
    
}
