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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.iip_ecosphere.platform.connectors.formatter.FormatCache;
import de.iip_ecosphere.platform.connectors.parser.InputParser.InputConverter;

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
    public double toFloat(String data) throws IOException {
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
    public double[] toDoubleArray(String data) throws IOException {
        throw new IOException("currently not implemented");
    }

    @Override
    public Object toObject(String data) throws IOException {
        throw new IOException("currently not implemented");
    }

    @Override
    public Date toDate(String data, String format) throws IOException {
        SimpleDateFormat f = FormatCache.getDateFormatter(format);
        try {
            return f.parse(data);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

}
