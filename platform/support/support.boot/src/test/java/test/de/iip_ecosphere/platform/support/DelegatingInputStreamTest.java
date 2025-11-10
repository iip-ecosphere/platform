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

package test.de.iip_ecosphere.platform.support;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.DelegatingInputStream;

/**
 * Tests {@link DelegatingInputStream}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DelegatingInputStreamTest {

    /**
     * For testing protected.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyDelegatingInputStream extends DelegatingInputStream {
        
        /**
         * Creates an instance.
         * 
         * @param delegate the delegate to call for individual operations
         */
        public MyDelegatingInputStream(InputStream delegate) {
            super(delegate);
        }
        
        /**
         * Returns the delegate.
         * 
         * @return the delgate
         */
        public InputStream getDel() {
            return getDelegate();
        }

    }
    
    /**
     * Tests {@link DelegatingInputStream}.
     */
    @Test
    public void testStream() throws IOException {
        InputStream fis = new FileInputStream("src/test/resources/YamlFile.yml");
        MyDelegatingInputStream dis 
            = new MyDelegatingInputStream(new FileInputStream("src/test/resources/YamlFile.yml"));
        Assert.assertEquals(fis.available(), dis.available());
        Assert.assertEquals(fis.markSupported(), dis.markSupported());
        Assert.assertEquals(fis.read(), dis.read());
        Assert.assertEquals(fis.skip(2), dis.skip(2));
        byte[] fisBuf = new byte[10];
        byte[] disBuf = new byte[10];
        fis.read(fisBuf);
        dis.read(disBuf);
        Assert.assertArrayEquals(fisBuf, disBuf);
        fis.read(fisBuf, 0, 1);
        dis.read(disBuf, 0, 1);
        Assert.assertEquals(fisBuf[0], disBuf[0]);
        fis.mark(10);
        dis.mark(10);
        try {
            fis.reset();
        } catch (IOException e) { // if not supported
        }
        try {
            dis.reset();
        } catch (IOException e) { // if not supported
        }
        Assert.assertNotNull(dis.getDel());
        dis.close();
        fis.close();
    }

}
