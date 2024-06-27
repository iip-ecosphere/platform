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

import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery;
import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery.TimeKind;
import org.junit.Assert;

/**
 * Tests {@link SimpleTimeseriesQuery}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimpleTimeseriesQueryTest {

    /**
     * Tests {@link SimpleTimeseriesQuery}.
     */
    @Test
    public void testStringTriggerQuery() {
        SimpleTimeseriesQuery q = new SimpleTimeseriesQuery(0, TimeKind.ABSOLUTE);
        Assert.assertEquals(0, q.delay());
        Assert.assertEquals(0, q.getStart());
        Assert.assertEquals(TimeKind.ABSOLUTE, q.getStartKind());
        Assert.assertEquals(TimeKind.UNSPECIFIED, q.getEndKind());

        q = new SimpleTimeseriesQuery(0, TimeKind.ABSOLUTE, 100, TimeKind.RELATIVE_MINUTES);
        Assert.assertEquals(0, q.delay());
        Assert.assertEquals(0, q.getStart());
        Assert.assertEquals(TimeKind.ABSOLUTE, q.getStartKind());
        Assert.assertEquals(100, q.getEnd());
        Assert.assertEquals(TimeKind.RELATIVE_MINUTES, q.getEndKind());

        q = new SimpleTimeseriesQuery(-50, TimeKind.RELATIVE_SECONDS, 70, TimeKind.RELATIVE_WEEKS, 500);
        Assert.assertEquals(500, q.delay());
        Assert.assertEquals(-50, q.getStart());
        Assert.assertEquals(TimeKind.RELATIVE_SECONDS, q.getStartKind());
        Assert.assertEquals(70, q.getEnd());
        Assert.assertEquals(TimeKind.RELATIVE_WEEKS, q.getEndKind());
    }

}
