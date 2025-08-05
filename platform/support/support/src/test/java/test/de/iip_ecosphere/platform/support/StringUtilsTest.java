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

import org.junit.Test;

import de.iip_ecosphere.platform.support.StringUtils;
import org.junit.Assert;

/**
 * Tests {@link StringUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StringUtilsTest {
    
    /**
     * Tests {@link StringUtils#escapeJava(String)} and {@link StringUtils#unescapeJava(String)}.
     */
    @Test
    public void testJava() {
        Assert.assertNull(StringUtils.escapeJava(null));
        Assert.assertNull(StringUtils.unescapeJava(null));

        Assert.assertEquals("\n", StringUtils.unescapeJava(StringUtils.escapeJava("\n")));
    }

    /**
     * Tests {@link StringUtils#escapeJson(String)} and {@link StringUtils#unescapeJson(String)}.
     */
    @Test
    public void testJson() {
        Assert.assertNull(StringUtils.escapeJson(null));
        Assert.assertNull(StringUtils.unescapeJson(null));

        Assert.assertEquals("\t", StringUtils.unescapeJava(StringUtils.escapeJava("\t")));
    }

}
