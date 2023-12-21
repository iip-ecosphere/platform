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

package test.de.iip_ecosphere.platform.support.aas;

import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.IdentifierType;

import org.junit.Assert;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;

/**
 * Tests {@link IdentifierType}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IdentifierTypeTest {

    /**
     * Tests the prefix functions.
     */
    @Test
    public void testPrefixFunctions() {
        Assert.assertTrue(urn("abc").startsWith(URN_PREFIX));
        Assert.assertTrue(iri("abc").startsWith(IRI_PREFIX));
        Assert.assertTrue(irdi("abc").startsWith(IRDI_PREFIX));
        Assert.assertTrue(urnText("abc").startsWith(URN_TEXT_PREFIX));
    }

}
