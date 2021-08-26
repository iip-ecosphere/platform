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

package de.iip_ecosphere.platform.support.aas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder.PayloadCodec;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder.PayloadConsumer;

/**
 * A simple serial payload codec.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SerialPayloadCodec implements PayloadCodec {

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
        byte[] result;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeUTF(info);
            oos.writeInt(payload.length);
            oos.write(payload);
            oos.flush();
            result = bos.toByteArray();
            bos.close();
        } catch (IOException e) { // shall not fail
            result = new byte[0];
        }
        return result;
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
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bis);
            String info = ois.readUTF();
            int len = ois.readInt();
            byte[] payload = new byte[len];
            ois.readFully(payload);
            bis.close();
            consumer.decoded(info, payload);
        } catch (IOException e) { // shall not occur
        }
    }

}
