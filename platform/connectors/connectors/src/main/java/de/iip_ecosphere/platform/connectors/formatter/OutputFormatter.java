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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import de.iip_ecosphere.platform.connectors.parser.InputParser;
import de.iip_ecosphere.platform.connectors.parser.InputParser.InputConverter;
import de.iip_ecosphere.platform.transport.serialization.IipEnum;

/**
 * Generic output formatter. You may add information to one chunk until {@link #chunkCompleted()} is called.
 * Custom implementations must have a constructor with a single String argument, the character encoding name.
 * 
 * Implementing classes shall use their specific rather than generic return types for 
 * {@link #getConverter()} to reduce dependencies on {@code <T>}. Moreover, {@link #add(String, Object)} shall
 * be directly used in combination with {@link InputConverter} to avoid exposing {@code <T>} unless explicitly 
 * necessary.
 * 
 * This interface is used to generate connector code against.
 * 
 * @param <T> the output format type
 * @author Holger Eichelberger, SSE
 */
public interface OutputFormatter<T> {
    
    /**
     * Separator for hierarchical names.
     */
    public static final char SEPARATOR = InputParser.SEPARATOR;
    
    /**
     * Converts primitive types to the output format. Must be stateless.
     * 
     * @param <T> the output format type
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface OutputConverter<T> {

        /**
         * Converts data from byte to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromByte(byte data) throws IOException;

        /**
         * Converts data from int to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromInteger(int data) throws IOException;

        /**
         * Converts data from long to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromLong(long data) throws IOException;

        /**
         * Converts data from String to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromString(String data) throws IOException;

        /**
         * Converts data from short to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromShort(short data) throws IOException;

        /**
         * Converts data from double to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromDouble(double data) throws IOException;

        /**
         * Converts data from float to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromFloat(float data) throws IOException;

        /**
         * Converts data from Boolean to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromBoolean(boolean data) throws IOException;
        
        /**
         * Converts data from an integer array to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromIntegerArray(int[] data) throws IOException;

        /**
         * Converts data from a double array to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromDoubleArray(double[] data) throws IOException;

        /**
         * Converts data from a byte array to the output format.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromByteArray(byte[] data) throws IOException;

        /**
         * Converts data from a date to the output format.
         * 
         * @param data the date
         * @param format the target date format (see {@link SimpleDateFormat})
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromDate(Date data, String format) throws IOException;

        /**
         * Converts data from a date to the output format.
         * 
         * @param data the date
         * @param format the target date format (see {@link SimpleDateFormat})
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public default T fromLocalDateTime(LocalDateTime data, String format) throws IOException {
            return fromDate(FormatCache.toDate(data), format);
        }

        /**
         * Converts data from an IIP enum literal to the output format using {@link IipEnum#getModelOrdinal()}.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public default T fromEnum(IipEnum data) throws IOException {
            return fromInteger(data.getModelOrdinal());
        }

        /**
         * Converts data from an IIP enum literal to the output format using {@link Enum#name()}.
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public default T fromEnumAsName(Enum<?> data) throws IOException {
            return fromString(data.name());
        }

        /**
         * Converts data from an object the output format. [fallback dummy]
         * 
         * @param data the data
         * @return the converted output format
         * @throws IOException if conversion fails
         */
        public T fromObject(Object data) throws IOException;

    }

    /**
     * Adds information to one chunk of output. Hierarchical names separated by {@link #SEPARATOR} can be used, but 
     * nested values for the same parent (object) field <b>must</b> be named in sequence.
     * 
     * @param name optional data name field (may be <b>null</b> for none)
     * @param data the data to be added
     * @throws IOException if adding the data fails for some reason
     */
    public void add(String name, T data) throws IOException;

    /**
     * Starts an array structure. Following {@link #add(String, Object)} calls will add elements to the array.
     * Must be closed with {@#link #endStructure()} 
     * 
     * @param name optional data name field holding the array (may be <b>null</b> for none)
     * @throws IOException if starting this structure fails
     */
    public void startArrayStructure(String name) throws IOException;
    
    /**
     * Starts an object structure. Following {@link #add(String, Object)} calls will add elements to the object.
     * Must be closed with {@#link #endStructure()} 
     * 
     * @param name optional data name field holding the array (may be <b>null</b> for none)
     * @throws IOException if starting this structure fails
     */
    public void startObjectStructure(String name) throws IOException;
    
    /**
     * Ends a structure started before.
     * 
     * @throws IOException if ending the actual structure fails
     */
    public void endStructure() throws IOException;
    
    /**
     * Completes a chunk of output data.
     * 
     * @return the chunk
     * @throws IOException if creating the chunk fails for some reason
     */
    public byte[] chunkCompleted() throws IOException;
    
    /**
     * Returns the output converter.
     * 
     * @return the output converter
     */
    public OutputConverter<T> getConverter();
    
}
