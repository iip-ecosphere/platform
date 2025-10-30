/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.json;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Abstracts Jacksons JsonGenerator for more fine-grained, incremental creation of JSON.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface JsonGenerator extends Closeable {

    /**
     * Method for outputting given value as JSON number.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     *
     * @param number value to write
     * @throws IOException when the number cannot be written
     */
    public void writeNumber(short number) throws IOException;

    /**
     * Method for outputting given value as JSON number.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     *
     * @param number value to write
     * @throws IOException when the number cannot be written
     */
    public void writeNumber(int number) throws IOException;

    /**
     * Method for outputting given value as JSON number.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     *
     * @param number value to write
     * @throws IOException when the number cannot be written
     */
    public void writeNumber(long number) throws IOException;

    /**
     * Method for outputting given value as JSON number.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     *
     * @param number value to write
     * @throws IOException when the number cannot be written
     */
    public void writeNumber(BigInteger number) throws IOException;

    /**
     * Method for outputting indicate JSON numeric value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     *
     * @param number value to write
     * @throws IOException when the number cannot be written
     */
    public void writeNumber(double number) throws IOException;

    /**
     * Method for outputting indicate JSON numeric value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     *
     * @param number value to write
     * @throws IOException when the number cannot be written
     */
    public void writeNumber(float number) throws IOException;

    /**
     * Method for outputting indicate JSON numeric value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     *
     * @param number value to write
     * @throws IOException when the number cannot be written
     */
    public void writeNumber(BigDecimal number) throws IOException;

    /**
     * Method for outputting a String value. Depending on context
     * this means either array element, (object) field value or
     * a stand alone String; but in all cases, String will be
     * surrounded in double quotes, and contents will be properly
     * escaped as required by JSON specification.
     * 
     * @param text value to write
     * @throws IOException when the text cannot be written
     */
    public void writeString(String text) throws IOException;
    
    /**
     * Method for outputting literal JSON boolean value (one of
     * Strings 'true' and 'false').
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     *
     * @param value the value to write
     * @throws IOException when the value cannot be written
     */
    public void writeBoolean(boolean value) throws IOException;

    /**
     * Method for outputting literal JSON null value.
     * Can be called in any context where a value is expected
     * (Array value, Object field value, root-level value).
     * 
     * @throws IOException when the value cannot be written
     */
    public void writeNull() throws IOException;

    /**
     * Value write method that can be called to write a single array.
     *
     * @param array Array that contains values to write
     * @param offset Offset of the first element to write, within array
     * @param length Number of elements in array to write, from `offset` to `offset + len - 1`
     * @throws IOException when the array cannot be written
     */
    public void writeArray(int[] array, int offset, int length) throws IOException;

    /**
     * Value write method that can be called to write a single array.
     *
     * @since 2.8
     *
     * @param array Array that contains values to write
     * @param offset Offset of the first element to write, within array
     * @param length Number of elements in array to write, from `offset` to `offset + len - 1`
     * @throws IOException when the array cannot be written
     */
    public void writeArray(long[] array, int offset, int length) throws IOException;

    /**
     * Value write method that can be called to write a single array.
     *
     * @param array Array that contains values to write
     * @param offset Offset of the first element to write, within array
     * @param length Number of elements in array to write, from `offset` to `offset + len - 1`
     * @throws IOException when the array cannot be written
     */
    public void writeArray(double[] array, int offset, int length) throws IOException;

    /**
     * Value write method that can be called to write a single array.
     *
     * @param array Array that contains values to write
     * @param offset Offset of the first element to write, within array
     * @param length Number of elements in array to write, from `offset` to `offset + len - 1`
     * @throws IOException when the array cannot be written
     */
    public void writeArray(String[] array, int offset, int length) throws IOException;

    /**
     * Method for writing starting marker of a Array value.
     * Array values can be written in any context where values
     * are allowed: meaning everywhere except for when
     * a field name is expected.
     * @throws IOException when the array cannot be written
     */
    public void writeStartArray() throws IOException;
    
    /**
     * Method for writing closing marker of a JSON Array value.
     * Marker can be written if the innermost structured type
     * is Array.
     * @throws IOException when the array cannot be written
     */
    public void writeEndArray() throws IOException;

    /**
     * Method for writing starting marker of an Object value.
     * Object values can be written in any context where values
     * are allowed: meaning everywhere except for when
     * a field name is expected.
     * @throws IOException when the object cannot be written
     */
    public void writeStartObject() throws IOException;

    /**
     * Method for writing closing marker of an Object value.
     * Marker can be written if the innermost structured type
     * is Object, and the last written event was either a
     * complete value, or a start object.
     * @throws IOException when the object cannot be written
     */
    public void writeEndObject() throws IOException;

    /**
     * Method for writing a field name. Field names can only be written in Object context , when field name is expected
     * (field names alternate with values).
     * @throws IOException when the name cannot be written
     */
    public void writeFieldName(String name) throws IOException;
    
    /**
     * Method for writing given Java object (POJO) as Json.
     * @throws IOException when the object cannot be written
     */
    public abstract void writeObject(Object pojo) throws IOException;

}
