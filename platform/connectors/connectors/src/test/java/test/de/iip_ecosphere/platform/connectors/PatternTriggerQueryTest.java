/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.events.PatternTriggerQuery;

/**
 * Test {@link PatternTriggerQuery}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PatternTriggerQueryTest {
    
    /**
     * Test {@link PatternTriggerQuery}.
     */
    @Test
    public void testQuery() {
        PatternTriggerQuery q = new PatternTriggerQuery("");
        Assert.assertNotNull(q.getPattern());
    }

}
