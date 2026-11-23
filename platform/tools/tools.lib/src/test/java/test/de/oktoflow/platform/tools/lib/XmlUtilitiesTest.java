/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.oktoflow.platform.tools.lib;

import org.junit.Assert;
import org.junit.Test;

import de.oktoflow.platform.tools.lib.XmlUtilities;

/**
 * Tests {@link XmlUtilities}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class XmlUtilitiesTest {
    
    /**
     * Tests {@link XmlUtilities#newSecureInstance()}, {@link XmlUtilities#newInstance()} and 
     * {@link XmlUtilities#configureSecure(javax.xml.parsers.DocumentBuilderFactory)}.
     */
    @Test
    public void testNewSecureInstance() {
        Assert.assertNotNull(XmlUtilities.newSecureInstance());
    }

}
