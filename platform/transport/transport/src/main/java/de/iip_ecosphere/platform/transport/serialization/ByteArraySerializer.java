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

package de.iip_ecosphere.platform.transport.serialization;

import java.io.IOException;

/**
 * A default serializer for <i>byte[]</i>.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ByteArraySerializer implements Serializer<byte[]> {

    @Override
    public byte[] from(byte[] data) throws IOException {
        return data;
    }

    @Override
    public byte[] to(byte[] source) throws IOException {
        return source;
    }

    @Override
    public byte[] clone(byte[] origin) throws IOException {
        byte[] result = new byte[origin.length];
        System.arraycopy(origin, 0, result, 0, result.length);
        return result;
    }

    @Override
    public Class<byte[]> getType() {
        return byte[].class;
    }

}
