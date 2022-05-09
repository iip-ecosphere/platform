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

package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import de.iip_ecosphere.platform.transport.status.Alert;
import de.iip_ecosphere.platform.transport.status.AlertSerializer;

/**
 * Tests {@link Alert} and {@link AlertSerializer}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AlertTest {
    
    /**
     * Basic tests for {@link Alert} and {@link AlertSerializer}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testAlerts() throws IOException {
        Alert e = new Alert();
        e.setUid("uid");
        e.setCorrelationId("corId");
        e.setTimestamp(1234);
        e.setFirstTimestamp(1236);
        e.setLastTimestamp(1239);
        e.setClearTimestamp(1240);
        e.setAlertname("alName");
        e.setSource("alSource");
        e.setInstance("alInst");
        e.setInfo("info");
        e.setTags("t1,t2");
        e.setSeverity("high");
        e.setPriority("high");
        e.setEventType("1234");
        e.setProbableCause("7");
        e.setCurrentValue("25");
        e.setUrl("http://a.b/c");
        e.setDescription("desc");
        e.setStatus("status");
        e.setRuleExpression("v < 25");
        e.setRuleTimeLimit("");

        AlertSerializer ser = new AlertSerializer();
        Alert e1 = ser.clone(e);
        assertAlert(e, e1);
        
        Assert.assertNotNull(SerializerRegistry.getSerializer(Alert.class));
        
        e1 = ser.from(ser.to(e));
        assertAlert(e, e1);
    }

    /**
     * Asserts that {@code expected} is the same as {@code actual}.
     * 
     * @param expected the expected alert
     * @param actual the actual alert
     */
    private void assertAlert(Alert expected, Alert actual) {
        Assert.assertEquals(expected.getUid(), actual.getUid());
        Assert.assertEquals(expected.getCorrelationId(), actual.getCorrelationId());
        Assert.assertEquals(expected.getTimestamp(), actual.getTimestamp());
        Assert.assertEquals(expected.getFirstTimestamp(), actual.getFirstTimestamp());
        Assert.assertEquals(expected.getLastTimestamp(), actual.getLastTimestamp());
        Assert.assertEquals(expected.getClearTimestamp(), actual.getClearTimestamp());
        Assert.assertEquals(expected.getAlertname(), actual.getAlertname());
        Assert.assertEquals(expected.getSource(), actual.getSource());
        Assert.assertEquals(expected.getInstance(), actual.getInstance());
        Assert.assertEquals(expected.getInfo(), actual.getInfo());
        Assert.assertEquals(expected.getTags(), actual.getTags());
        Assert.assertEquals(expected.getSeverity(), actual.getSeverity());
        Assert.assertEquals(expected.getPriority(), actual.getPriority());
        Assert.assertEquals(expected.getEventType(), actual.getEventType());
        Assert.assertEquals(expected.getProbableCause(), actual.getProbableCause());
        Assert.assertEquals(expected.getCurrentValue(), actual.getCurrentValue());
        Assert.assertEquals(expected.getUrl(), actual.getUrl());
        Assert.assertEquals(expected.getDescription(), actual.getDescription());
        Assert.assertEquals(expected.getStatus(), actual.getStatus());
        Assert.assertEquals(expected.getRuleExpression(), actual.getRuleExpression());
        Assert.assertEquals(expected.getRuleTimeLimit(), actual.getRuleTimeLimit());
    }

}
