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

package de.iip_ecosphere.platform.support.aas;

import java.nio.charset.Charset;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder.PayloadCodec;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder.PayloadConsumer;
import de.iip_ecosphere.platform.support.json.JsonUtils;

/**
 * A simple JSON payload codec.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonPayloadCodec implements PayloadCodec {

    private Charset charset = Charset.forName("UTF-8");
    
    /**
     * Payload class for serialization/deserialization.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Payload {
        
        private String info;
        private byte[] payload;
        
        /**
         * Returns the information (tag).
         * 
         * @return the info
         */
        public String getInfo() {
            return info;
        }
        
        /**
         * Defines the information (tag).
         * 
         * @param info the info to set
         */
        public void setInfo(String info) {
            this.info = info;
        }
        
        /**
         * Returns the payload.
         * 
         * @return the payload
         */
        public byte[] getPayload() {
            return payload;
        }
        
        /**
         * Defines the payload.
         * 
         * @param payload the payload to set
         */
        public void setPayload(byte[] payload) {
            this.payload = payload;
        }
    }
    
    @Override
    public Charset getCharset() {
        return charset;
    }

    @Override
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    @Override
    public Schema intendedSchema() {
        return Schema.TCP;
    }

    @Override
    public byte[] encode(String info, byte[] payload) {
        Payload pl = new Payload();
        pl.setInfo(info);
        pl.setPayload(payload);
        return JsonUtils.toJson(pl).getBytes();
    }

    @Override
    public int getDataBytesLength() {
        return 0;
    }

    @Override
    public int decodeDataLength(byte[] data) {
        return 0;
    }

    @Override
    public void decode(byte[] data, PayloadConsumer consumer) {
        Payload pl = JsonUtils.fromJson(new String(data), Payload.class);
        consumer.decoded(pl.getInfo(), pl.getPayload());
    }

}
