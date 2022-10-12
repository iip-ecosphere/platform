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
import java.util.Date;

import de.iip_ecosphere.platform.connectors.formatter.OutputFormatter;
import de.iip_ecosphere.platform.connectors.formatter.OutputFormatter.OutputConverter;

/**
 * Output converter implementing the {@link ModelAccess} conversion conventions. An
 * output converter for {@link ModelAccess} shall allow for more homogeneous generated
 * code compared to {@link OutputFormatter}-based code. Moreover, this shall allow for
 * encapsulating future changes to input/output conventions for {@link ModelAccess}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ModelOutputConverter implements OutputConverter<Object> {

    public static final ModelOutputConverter INSTANCE = new ModelOutputConverter();
    
    /**
     * Prevents external creation.
     */
    protected ModelOutputConverter() {
    }

    @Override
    public Object fromInteger(int data) throws IOException {
        return data;
    }

    @Override
    public Object fromByte(byte data) throws IOException {
        return data;
    }

    @Override
    public Object fromLong(long data) throws IOException {
        return data;
    }

    @Override
    public Object fromShort(short data) throws IOException {
        return data;
    }

    @Override
    public Object fromString(String data) throws IOException {
        return data;
    }

    @Override
    public Object fromDouble(double data) throws IOException {
        return data;
    }

    @Override
    public Object fromFloat(float data) throws IOException {
        return data;
    }

    @Override
    public Object fromBoolean(boolean data) throws IOException {
        return data;
    }

    @Override
    public Object fromIntegerArray(int[] data) throws IOException {
        return data;
    }

    @Override
    public Object fromDoubleArray(double[] data) throws IOException {
        return data;
    }

    @Override
    public Object fromByteArray(byte[] data) throws IOException {
        return data;
    }

    @Override
    public Object fromDate(Date data, String format) throws IOException {
        return data;
    }

    @Override
    public Object fromObject(Object data) throws IOException {
        return data;
    }

}
