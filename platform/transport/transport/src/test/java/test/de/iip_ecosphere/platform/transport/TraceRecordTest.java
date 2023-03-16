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
import java.util.HashMap;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import de.iip_ecosphere.platform.transport.status.TraceRecordSerializer;
import org.junit.Assert;

/**
 * Tests {@link TraceRecord} and {@link TraceRecordSerializer}.
 *  
 * @author Holger Eichelberger, SSE
 */
public class TraceRecordTest {

    /**
     * Tests trace record access and serialization.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testTraceRecord() throws IOException {
        StatusMessage payload = new StatusMessage(ActionTypes.ADDED, "1234", "abba");
        TraceRecord record = new TraceRecord("src", "act", payload);
        TraceRecordSerializer ser = new TraceRecordSerializer();
        byte[] s = ser.to(record);
        TraceRecord tr = ser.from(s);
        Assert.assertNotNull(tr);
        Assert.assertEquals(record.getAction(), tr.getAction());
        Assert.assertEquals(record.getSource(), tr.getSource());
        Assert.assertEquals(record.getTimestamp(), tr.getTimestamp());
        Assert.assertNotNull(record.getPayload());
        Assert.assertTrue(record.getPayload() instanceof StatusMessage);
        StatusMessage p = (StatusMessage) record.getPayload();
        Assert.assertEquals(payload.getAction(), p.getAction());
        Assert.assertEquals(payload.getId(), p.getId());
        Assert.assertArrayEquals(payload.getAliasIds(), p.getAliasIds());
    }

    /**
     * We just need a type for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class Payload {
        
        private int field;
        private InnerPayload inner;
                
        /**
         * Returns the field value.
         * 
         * @return the field value
         */
        public int getField() {
            return field;
        }
        
        /**
         * Changes the field.
         * 
         * @param field the field
         */
        public void setField(int field) {
            this.field = field;
        }
        
        /**
         * Returns the inner value.
         * 
         * @return the inner value
         */
        public InnerPayload getInner() {
            return inner;
        }
        
        /**
         * Defines the inner value.
         * 
         * @param inner the inner value
         */
        public void setInner(InnerPayload inner) {
            this.inner = inner;
        }
        
    }
    
    /**
     * Another test type.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class InnerPayload {
    }
    
    /**
     * Tests basic payload filtering.
     * 
     * @throws IOException shall not occur
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testTraceRecordFiltering() throws IOException {
        Payload pl = new Payload();
        pl.setField(25);
        pl.setInner(new InnerPayload());
        TraceRecord record = new TraceRecord("src", "act", pl);
        TraceRecordSerializer ser = new TraceRecordSerializer();
        
        TraceRecord.ignoreClass(InnerPayload.class);
        TraceRecord record2 = ser.from(ser.to(record));
        HashMap<Object, Object> payload = (HashMap<Object, Object>) record2.getPayload(); // object unknown
        Assert.assertNotNull(payload.get("field"));
        Assert.assertEquals(25, payload.get("field"));
        Assert.assertNull(payload.get("inner"));
        TraceRecordSerializer.clearIgnores();
        
        TraceRecord.ignoreField(Payload.class, "field");
        record2 = ser.from(ser.to(record));
        payload = (HashMap<Object, Object>) record2.getPayload(); // object unknown
        Assert.assertNull(payload.get("field"));
        Assert.assertNotNull(payload.get("inner"));
        TraceRecordSerializer.clearIgnores();
    }

}
