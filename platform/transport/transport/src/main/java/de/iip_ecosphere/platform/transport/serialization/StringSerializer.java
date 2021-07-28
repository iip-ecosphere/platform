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
 * A default serializer for <i>String</i> through Base64 encoding.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StringSerializer implements Serializer<String> {

    @Override
    public String from(byte[] data) throws IOException {
        return new String(data);
    }

    @Override
    public byte[] to(String source) throws IOException {
        return source.getBytes();
    }

    @Override
    public String clone(String origin) throws IOException {
        return new String(origin);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

}
