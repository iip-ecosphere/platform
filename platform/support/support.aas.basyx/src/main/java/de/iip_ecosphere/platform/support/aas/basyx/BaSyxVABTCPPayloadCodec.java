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

package de.iip_ecosphere.platform.support.aas.basyx;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.eclipse.basyx.vab.protocol.basyx.CoderTools;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder.PayloadCodec;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder.PayloadConsumer;

/**
 * A BaSyx TCP payload codec in the style of TCP VAB, but on generic payload. We introduced
 * this codec because TCP VAB closes its connection after each data frame.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxVABTCPPayloadCodec implements PayloadCodec {

    private Charset charset = Charset.forName("UTF-8");
    
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
        int resultFrameSize = 4 + (info == null ? 0 : info.length()) + payload.length;
        byte[] frameLength = new byte[4];
        byte[] infoLength = new byte[4];
        CoderTools.setInt32(frameLength, 0, resultFrameSize);
        CoderTools.setInt32(infoLength, 0, null == info ? -1 : info.length());

        // Place response frame in buffer
        ByteBuffer buffer = ByteBuffer.allocate(resultFrameSize + 4);
        buffer.put(frameLength);
        buffer.put(infoLength);
        if (info != null) {
            buffer.put(info.getBytes(charset));
        }
        buffer.put(payload);
        buffer.flip();

        return buffer.array();
    }

    @Override
    public int getDataBytesLength() {
        return 4;
    }

    @Override
    public int decodeDataLength(byte[] data) {
        return CoderTools.getInt32(data, 0);
    }

    @Override
    public void decode(byte[] data, PayloadConsumer consumer) {
        int infoLength =  CoderTools.getInt32(data, 0);
        String info;
        if (infoLength < 0) {
            info = null;
            infoLength = 0;
        } else {
            info = new String(data, 4, infoLength, charset);
        }
        byte[] payload = new byte[data.length - 4 - infoLength];
        System.arraycopy(data, 4 + infoLength, payload, 0, payload.length);
        consumer.decoded(info, payload);
    }

}
