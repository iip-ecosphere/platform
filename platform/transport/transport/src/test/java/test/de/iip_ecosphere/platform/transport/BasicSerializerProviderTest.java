/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.BasicSerializerProvider;
import de.iip_ecosphere.platform.transport.serialization.StringSerializer;

/**
 * Tests {@link BasicSerializerProvider}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicSerializerProviderTest {

    /**
     * Tests {@link BasicSerializerProvider}.
     */
    @Test
    public void test() {
        BasicSerializerProvider prov = new BasicSerializerProvider();
        Assert.assertNull(prov.getSerializer(String.class));
        prov.registerSerializer(new StringSerializer());
        Assert.assertNotNull(prov.getSerializer(String.class));
    }

}
