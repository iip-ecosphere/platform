/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.DefaultQualifiedElement;
import de.iip_ecosphere.platform.transport.serialization.QualifiedElement;
import de.iip_ecosphere.platform.transport.serialization.QualifiedElementFactory;

import org.junit.Assert;

/**
 * Tests {@link QualifiedElementFactory}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class QualifiedElementFactoryTest {

    /**
     * Tests {@link DefaultQualifiedElement}.
     */
    @Test
    public void testDefaultElement() {
        QualifiedElement<String> elt = new DefaultQualifiedElement<>();
        elt.setValue("TEST");
        Assert.assertEquals("TEST", elt.getValue());
        elt.setQualifier("iri:0123-123-456");
        Assert.assertEquals("iri:0123-123-456", elt.getQualifier());
    }

    /**
     * An own element type for testing.
     * 
     * @param <T> the element value type
     * @author Holger Eichelberger, SSE
     */
    private static class MyElement<T> extends DefaultQualifiedElement<T> {
    }

    /**
     * Tests {@link QualifiedElementFactory}.
     */
    @Test
    public void testElementFactory() {
        QualifiedElementFactory.reset();

        QualifiedElement<String> sElt = QualifiedElementFactory.createElement(String.class);
        Assert.assertNotNull(sElt);
        Assert.assertTrue(sElt instanceof DefaultQualifiedElement);
        
        QualifiedElementFactory.registerElementSupplier(Integer.class, () -> new MyElement<>());
        QualifiedElement<Integer> iElt = QualifiedElementFactory.createElement(Integer.class);
        Assert.assertNotNull(iElt);
        Assert.assertTrue(iElt instanceof MyElement);

        QualifiedElementFactory.reset();
    }

}
