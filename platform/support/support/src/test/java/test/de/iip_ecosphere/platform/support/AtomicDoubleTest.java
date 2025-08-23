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

import de.iip_ecosphere.platform.support.AtomicDouble;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link AtomicDouble}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AtomicDoubleTest {

    /**
     * Tests {@link AtomicDouble}.
     * 
     * @throws IOException shall not occur
     * @throws ClassNotFoundException shall not occur
     */
    @Test
    public void testDouble() throws IOException, ClassNotFoundException {
        AtomicDouble d = new AtomicDouble();
        Assert.assertEquals(0, d.get(), 0.01);
        
        d = new AtomicDouble(2.0);
        Assert.assertEquals(2.0, d.get(), 0.01);
        d.addAndGet(1.0);
        Assert.assertEquals(3.0, d.get(), 0.01);
        Assert.assertEquals(3.0, d.doubleValue(), 0.01);
        Assert.assertEquals(3, d.intValue());
        d.set(5.0);
        Assert.assertEquals(5.0, d.doubleValue(), 0.01);
        Assert.assertEquals(String.valueOf(5.0), d.toString());
        Assert.assertEquals(5, d.longValue());
        Assert.assertEquals(5.0, d.floatValue(), 0.01);
        Assert.assertEquals(5.0, d.getAndSet(10.0), 0.01);
        Assert.assertEquals(10.0, d.get(), 0.01);
        Assert.assertEquals(10.0, d.getAndAdd(10.0), 0.01);
        Assert.assertEquals(20.0, d.get(), 0.01);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oOut = new ObjectOutputStream(out);
        oOut.writeObject(d);
        oOut.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream oIn = new ObjectInputStream(in);
        Object obj = oIn.readObject();
        oIn.close();
        Assert.assertTrue(obj instanceof AtomicDouble);
        AtomicDouble d2 = (AtomicDouble) obj;
        Assert.assertEquals(d.get(), d2.get(), 0.01);
        Assert.assertEquals(d, d);
        Assert.assertEquals(d.hashCode(), d.hashCode());
        
        d.compareAndSet(20.0, 21.0);
        d.lazySet(22.0);
        d.weakCompareAndSet(22.0, 23.0);
    }

}
