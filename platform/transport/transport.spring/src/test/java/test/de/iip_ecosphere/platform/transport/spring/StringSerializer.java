/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport.spring;

import java.io.IOException;

import de.iip_ecosphere.platform.transport.serialization.Serializer;

public class StringSerializer implements Serializer<String> {

    @Override
    public byte[] to(String value) throws IOException {
        TestCounters.increaseTo();
        return value.getBytes();
    }

    @Override
    public String from(byte[] data) throws IOException {
        TestCounters.increaseFrom();
        return new String(data);
    }

    @Override
    public String clone(String origin) throws IOException {
        TestCounters.increaseCopy();
        return new String(origin);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

}
