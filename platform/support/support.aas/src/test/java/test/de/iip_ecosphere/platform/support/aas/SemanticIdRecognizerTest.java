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

package test.de.iip_ecosphere.platform.support.aas;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.SemanticIdRecognizer;

/**
 * Tests {@link SemanticIdRecognizer}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SemanticIdRecognizerTest {
    
    /**
     * Tests the recognizer.
     */
    @Test
    public void testRecognizer() {
        Assert.assertFalse(SemanticIdRecognizer.isSemanticId("abc"));
        Assert.assertNull(SemanticIdRecognizer.getIdentifierPrefix("abc"));
        Assert.assertNull(SemanticIdRecognizer.getSemanticIdFrom("abc", false));
        
        final String irdi = "0173-1#02-AAM737#002";
        Assert.assertTrue(SemanticIdRecognizer.isSemanticId(irdi));
        Assert.assertEquals(IdentifierType.IRDI_PREFIX, SemanticIdRecognizer.getIdentifierPrefix(irdi));
        Assert.assertEquals(irdi, SemanticIdRecognizer.getSemanticIdFrom(irdi, false));
        Assert.assertEquals(irdi, SemanticIdRecognizer.getSemanticIdFrom("0173-1#02-AAM735#002/" + irdi, false, true));

        final String iri = "https://eclass.eu/iri/1234";
        Assert.assertTrue(SemanticIdRecognizer.isSemanticId(iri));
        Assert.assertEquals(IdentifierType.IRI_PREFIX, SemanticIdRecognizer.getIdentifierPrefix(iri));
        Assert.assertEquals(iri, SemanticIdRecognizer.getSemanticIdFrom(iri, false));

        final String iri2 = "https://admin-shell.io/submodel/v2";
        Assert.assertTrue(SemanticIdRecognizer.isSemanticId(iri2));
        Assert.assertEquals(IdentifierType.IRI_PREFIX, SemanticIdRecognizer.getIdentifierPrefix(iri2));
        Assert.assertEquals(IdentifierType.IRI_PREFIX + iri2, SemanticIdRecognizer.getSemanticIdFrom(iri2, true));
    }

}
