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

package de.iip_ecosphere.platform.connectors.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.iip_ecosphere.platform.connectors.formatter.FormatCache;
import de.iip_ecosphere.platform.connectors.parser.InputParser.InputConverter;


/**
 * Input converter implementing the {@link ModelAccess} conversion conventions. An
 * input converter for {@link ModelAccess} shall allow for more homogeneous generated
 * code compared to {@link InputParser}-based code. Moreover, this shall allow for
 * encapsulating future changes to input/output conventions for {@link ModelAccess}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ModelInputConverter implements InputConverter<Object> {

    public static final ModelInputConverter INSTANCE = new ModelInputConverter();
    
    /**
     * Prevents external creation.
     */
    protected ModelInputConverter() {
    }
    
    @Override
    public int toInteger(Object data) throws IOException {
        return (int) data;
    }

    @Override
    public long toLong(Object data) throws IOException {
        return (long) data;
    }

    @Override
    public String toString(Object data) throws IOException {
        return null != data ? data.toString() : null;
    }

    @Override
    public double toDouble(Object data) throws IOException {
        return (double) data;
    }

    @Override
    public float toFloat(Object data) throws IOException {
        return (float) data;
    }

    @Override
    public boolean toBoolean(Object data) throws IOException {
        return (boolean) data;
    }

    @Override
    public int[] toIntegerArray(Object data) throws IOException {
        return (int[]) data; // unsure
    }

    @Override
    public double[] toDoubleArray(Object data) throws IOException {
        return (double[]) data; // unsure
    }

    @Override
    public Date toDate(Object data, String format) throws IOException {
        if (data instanceof String) {
            SimpleDateFormat f = FormatCache.getDateFormatter(format);
            try {
                return f.parse((String) data);
            } catch (ParseException e) {
                throw new IOException(e);
            }
        } else {
            return (Date) data;
        }
    }

    @Override
    public Object toObject(Object data) throws IOException {
        return data;
    }

}
