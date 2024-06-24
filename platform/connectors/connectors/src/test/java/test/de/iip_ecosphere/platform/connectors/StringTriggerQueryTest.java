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

package test.de.iip_ecosphere.platform.connectors;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.events.StringTriggerQuery;
import org.junit.Assert;

/**
 * Tests {@link StringTriggerQuery}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StringTriggerQueryTest {

    /**
     * Tests {@link StringTriggerQuery}.
     */
    @Test
    public void testStringTriggerQuery() {
        final String query = "from x";
        StringTriggerQuery q = new StringTriggerQuery(query);
        Assert.assertEquals(0, q.delay());
        Assert.assertEquals(query, q.getQuery());

        q = new StringTriggerQuery(query, 100);
        Assert.assertEquals(100, q.delay());
        Assert.assertEquals(query, q.getQuery());
    }

}
