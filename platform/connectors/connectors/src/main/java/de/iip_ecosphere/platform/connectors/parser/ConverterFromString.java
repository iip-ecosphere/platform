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

package de.iip_ecosphere.platform.connectors.parser;

import java.io.IOException;
import java.util.List;

import de.iip_ecosphere.platform.connectors.parser.InputParser.InputConverter;
import de.iip_ecosphere.platform.transport.serialization.QualifiedElement;

/**
 * A basic String to primitive types converter.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConverterFromString implements InputConverter<String> {

    @Override
    public int toInteger(String data) throws IOException {
        try {
            return Integer.parseInt(data);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public byte toByte(String data) throws IOException {
        try {
            return Byte.parseByte(data);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public long toLong(String data) throws IOException {
        try {
            return Long.parseLong(data);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String toString(String data) throws IOException {
        return data;
    }

    @Override
    public double toDouble(String data) throws IOException {
        try {
            return Double.parseDouble(data);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public short toShort(String data) throws IOException {
        try {
            return Short.parseShort(data);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public float toFloat(String data) throws IOException {
        try {
            return Float.parseFloat(data);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean toBoolean(String data) throws IOException {
        return Boolean.valueOf(data);
    }

    @Override
    public int[] toIntegerArray(String data) throws IOException {
        throw new IOException("currently not implemented");
    }

    @Override
    public String[] toStringArray(String data) throws IOException {
        throw new IOException("currently not implemented");
    }

    @Override
    public byte[] toByteArray(String data) throws IOException {
        throw new IOException("currently not implemented");
    }

    @Override
    public double[] toDoubleArray(String data) throws IOException {
        throw new IOException("currently not implemented");
    }

    @Override
    public Object toObject(String data) throws IOException {
        throw new IOException("currently not implemented");
    }

    @Override
    public <E> List<E> toList(String data, Class<E> eltCls) throws IOException {
        throw new IOException("currently not implemented");
    }

    @Override
    public <E> List<QualifiedElement<E>> toElementList(String data, Class<E> eltCls) throws IOException {
        throw new IOException("currently not implemented");
    }

}
