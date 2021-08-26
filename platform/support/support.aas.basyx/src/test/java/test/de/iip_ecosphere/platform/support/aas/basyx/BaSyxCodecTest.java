/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx;

import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder.PayloadCodec;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder.PayloadConsumer;

import java.nio.charset.Charset;

import org.junit.Assert;

/**
 * Tests the available protocol codecs.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxCodecTest {

    /**
     * Tests the codecs.
     */
    @Test
    public void testCodecs() {
        AasFactory factory = AasFactory.getInstance();
        for (String protocol : factory.getProtocols()) {
            if (!AasFactory.LOCAL_PROTOCOL.equals(protocol)) { // only VAB
                ProtocolServerBuilder builder = factory.createProtocolServerBuilder(protocol, 8080); // port not rel.
                PayloadCodec codec = builder.createPayloadCodec();
                if (null != codec) {
                    Assert.assertNotNull(codec.intendedSchema());
                    Charset cs = codec.getCharset();
                    Assert.assertNotNull(cs);
                    codec.setCharset(cs);
                    Assert.assertEquals(cs, codec.getCharset()); // well, called...
                    
                    assertEncodeDecode(codec, "Info", "Hallo");
                    assertEncodeDecode(codec, "Info", "");
                    assertEncodeDecode(codec, "", "Hallo");
                    assertEncodeDecode(codec, "", "");
                    assertEncodeDecode(codec, null, "Hallo");
                    assertEncodeDecode(codec, null, "");
                }
            }
        }
    }
    
    /**
     * A payload consumer for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class TestPayloadConsumer implements PayloadConsumer {
        
        private String info;
        private byte[] payload;
        
        @Override
        public void decoded(String info, byte[] payload) {
            this.info = info;
            this.payload = payload;
        }
        
    }
    
    /**
     * Asserts encoding/decoding data.
     * 
     * @param codec the codec instance
     * @param info the optional information (may be <b>null</b> or empty)
     * @param data the data for the payload
     */
    private void assertEncodeDecode(PayloadCodec codec, String info, String data) {
        byte[] payload = data.getBytes(); // charset is irrelevant
        byte[] tmp = codec.encode(info, payload);
        Assert.assertNotNull(tmp);
        int len = codec.getDataBytesLength();
        Assert.assertTrue(len > 0);
        byte[] lenData = new byte[len];
        
        // simulate stream reading
        System.arraycopy(tmp, 0, lenData, 0, len);
        byte[] otherData = new byte[tmp.length - len];
        System.arraycopy(tmp, 4, otherData, 0, otherData.length);
        TestPayloadConsumer consumer = new TestPayloadConsumer();
        codec.decode(otherData, consumer);
        if (null == info) {
            Assert.assertNull(consumer.info);
        } else {
            Assert.assertEquals(info, consumer.info);
        }
        Assert.assertArrayEquals(payload, consumer.payload);
    }
    
}
