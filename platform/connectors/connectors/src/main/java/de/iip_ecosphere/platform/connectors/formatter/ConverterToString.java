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

package de.iip_ecosphere.platform.connectors.formatter;

import java.io.IOException;
import java.util.Date;

import de.iip_ecosphere.platform.connectors.formatter.OutputFormatter.OutputConverter;

/**
 * Converts primitive data to string format.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConverterToString implements OutputConverter<String> {
    
    @Override
    public String fromInteger(int data) throws IOException {
        return Integer.toString(data);
    }

    @Override
    public String fromByte(byte data) throws IOException {
        return Byte.toString(data);
    }

    @Override
    public String fromLong(long data) throws IOException {
        return Long.toString(data);
    }

    @Override
    public String fromShort(short data) throws IOException {
        return Short.toString(data);
    }

    @Override
    public String fromString(String data) throws IOException {
        return data;
    }

    @Override
    public String fromDouble(double data) throws IOException {
        return Double.toString(data);
    }

    @Override
    public String fromFloat(float data) throws IOException {
        return Float.toString(data);
    }

    @Override
    public String fromBoolean(boolean data) throws IOException {
        return Boolean.toString(data);
    }

    @Override
    public String fromIntegerArray(int[] data) throws IOException {
        throw new IOException("currently not supported");
    }

    @Override
    public String fromDoubleArray(double[] data) throws IOException {
        throw new IOException("currently not supported");
    }
    
    @Override
    public String fromStringArray(String[] data) throws IOException {
        throw new IOException("currently not supported");
    }

    @Override
    public String fromByteArray(byte[] data) throws IOException {
        throw new IOException("currently not supported");
    }

    @Override
    public String fromObject(Object data) throws IOException {
        throw new IOException("currently not supported");
    }

    @Override
    public String fromDate(Date data, String format) throws IOException {
        return FormatCache.format(data, format);
    }

}
