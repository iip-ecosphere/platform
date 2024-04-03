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

package test.de.iip_ecosphere.platform.support.aas;

import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.types.common.DefaultElement;
import de.iip_ecosphere.platform.support.aas.types.common.Element;
import de.iip_ecosphere.platform.support.aas.types.common.ElementFactory;

import org.junit.Assert;

/**
 * Tests {@link ElementFactory}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ElementFactoryTest {

    /**
     * Tests {@link DefaultElement}.
     */
    @Test
    public void testDefaultElement() {
        Element<String> elt = new DefaultElement<>();
        elt.setValue("TEST");
        Assert.assertEquals("TEST", elt.getValue());
        elt.setSemanticId("iri:0123-123-456");
        Assert.assertEquals("iri:0123-123-456", elt.getSemanticId());
    }

    /**
     * An own element type for testing.
     * 
     * @param <T> the element value type
     * @author Holger Eichelberger, SSE
     */
    private static class MyElement<T> extends DefaultElement<T> {
    }

    /**
     * Tests {@link ElementFactory}.
     */
    @Test
    public void testElementFactory() {
        ElementFactory.reset();

        Element<String> sElt = ElementFactory.createElement(String.class);
        Assert.assertNotNull(sElt);
        Assert.assertTrue(sElt instanceof DefaultElement);
        
        ElementFactory.registerElementSupplier(Integer.class, () -> new MyElement<>());
        Element<Integer> iElt = ElementFactory.createElement(Integer.class);
        Assert.assertNotNull(iElt);
        Assert.assertTrue(iElt instanceof MyElement);

        ElementFactory.reset();
    }

}
