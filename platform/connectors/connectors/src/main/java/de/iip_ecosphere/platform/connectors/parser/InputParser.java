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
import java.time.LocalDateTime;
import java.util.Date;

import de.iip_ecosphere.platform.connectors.formatter.FormatCache;
import de.iip_ecosphere.platform.support.function.IOConsumer;
import de.iip_ecosphere.platform.transport.serialization.IipEnum;

/**
 * Interfaces for generic named/indexed input parsers. Custom implementations must have a constructor with a single 
 * String argument, the character encoding name. If adequate, a parser may interpret a given field, e.g., if a JSON 
 * payload is given as string but the (field) access indicates that there shall be an object inside, the parser 
 * implementation may "deserialize" the string and look for the field.
 * 
 * Implementing classes shall use their specific rather than generic return types for {@link #parse(byte[])} and 
 * {@link #getConverter()} to reduce dependencies on {@code <T>}. Moreover, methods of {@link ParseResult} shall
 * be directly used in combination with {@link InputConverter} to avoid exposing {@code <T>} unless explicitly 
 * necessary.
 * 
 * This interface is used to generate connector code against.
 * <b>Warning:</b> This interface is not stable and may change during performance optimization.
 * 
 * @param <T> the type of data produced by parsing
 * @author Holger Eichelberger, SSE
 */
public interface InputParser<T> {
    
    /**
     * Separator for hierarchical names.
     */
    public static char SEPARATOR = '.';
    
    /**
     * Result of parsing data.
     * 
     * @param <T> the type of data produced by parsing
     * @author Holger Eichelberger, SSE
     */
    public interface ParseResult<T> {
        
        /**
         * Returns the number of parsed data fields.
         * 
         * @return the number of data fields (non-negative)
         */
        public int getDataCount();

        /**
         * Returns the value of the data field for the given field {@code name} or with 
         * via the given {@code index}. Primary index goes via name and if not given/mapped, index-based 
         * access shall be used as fallback. Names may be hierarchical. May be overridden if direct access to names 
         * is provided by the parsed structure, e.g., in JSON. Thus, no index-access is provided in the first place
         * by this interface.
         * 
         * @param name the name of the data field, may contain hierarchical names separated by 
         *     {@link InputParser#SEPARATOR}, may be based on the scope set by {@link #stepInto(String, int)}
         * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
         *     {@link #getDataCount()}
         * @return the data value
         * @throws IOException if the mapped index or the given 
         *     {@code index}&lt;0 || index &gt;= {@link #getDataCount()}
         */
        public T getData(String name, int... indexes) throws IOException;

        /**
         * Returns the value of the data field for the given field {@code name} or with 
         * via the given {@code index}. Primary index goes via name and if not given/mapped, index-based 
         * access shall be used as fallback. Names may be hierarchical. May be overridden if direct access to names 
         * is provided by the parsed structure, e.g., in JSON. Thus, no index-access is provided in the first place
         * by this interface. This method shall not throw any exception.
         * 
         * @param ifPresent consumer called if a data value was found. The value is passed to {@code ifPresent}
         *     for further processing
         * @param name the name of the data field, may contain hierarchical names separated by 
         *     {@link InputParser#SEPARATOR}, may be based on the scope set by {@link #stepInto(String, int)}
         * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
         *     {@link #getDataCount()}
         * @throws IOException if an 
         */
        public void getData(IOConsumer<T> ifPresent, String name, int... indexes) throws IOException;

        /**
         * Returns the value of the data field for the given field {@code name} or with 
         * via the given {@code index}. Primary index goes via name and if not given/mapped, index-based 
         * access shall be used as fallback. No hierarchical name interpretation happens here (local). In combination
         * with {@link #stepInto(String, int)}, this method shall be faster than {@link #getData(String, int...)}.
         * 
         * @param name the name of the data field
         * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
         *     {@link #getDataCount()}
         * @return the data value
         * @throws IOException if the mapped index or the given 
         *     {@code index}&lt;0 || index &gt;= {@link #getDataCount()}
         */
        public T getLocalData(String name, int... indexes) throws IOException;

        /**
         * Returns the value of the data field for the given field {@code name} or with 
         * via the given {@code index}. Primary index goes via name and if not given/mapped, index-based 
         * access shall be used as fallback. No hierarchical name interpretation happens here (local). In combination
         * with {@link #stepInto(String, int)}, this method shall be faster than 
         * {@link #getData(IOConsumer, String, int...)}.
         * 
         * @param ifPresent consumer called if a data value was found. The value is passed to {@code ifPresent}
         *     for further processing
         * @param name the name of the data field
         * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
         *     {@link #getDataCount()}
         * @throws IOException if an exception occurred in {@code ifPresent} 
         */
        public void getLocalData(IOConsumer<T> ifPresent, String name, int... indexes) throws IOException;

        /**
         * Sets the hierarchical substructure denoted by {@code name} as current scope for further resolution.
         * When overriding, declare the actual type as result type.
         * 
         * @param name non-hierarchical name of contained substructure
         * @param index the 0-based indexes to the field, must be less than {@link #getDataCount()}
         * @return the sub parse-result taking {@code name} as context, use {@code #stepOut()} to leave that context
         * @throws IOException if stepping into fails for some reason
         */
        public ParseResult<T> stepInto(String name, int index) throws IOException;
        
        /**
         * Steps out of the actual context set by {@link #stepInto(String, int)}.
         * When overriding, declare the actual type as result type.
         * 
         * @return the actual (parent) context, may be <b>null</b> if this step out was illegal in a non-nested context
         */
        public ParseResult<T> stepOut();

        /**
         * Returns the name of the field. This operation may not be efficient on all input parsers, in particular
         * if no index positions are recorded. However, for generically parsing back some structures, this operation is
         * required.
         * 
         * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
         *     {@link #getDataCount()}
         * @return the name of the field or empty if not known
         */
        public default String getFieldName(int... indexes) {
            try {
                return getFieldName(null, indexes);
            } catch (IOException e) {
                // shall not occur as there is no value consumer
                return "";
            }
        }

        /**
         * Returns the name of the field. This operation may not be efficient on all input parsers, in particular
         * if no index positions are recorded. However, for generically parsing back some structures, this operation is
         * required.
         * 
         * @param valueCons a value consumer to handle the value of the field (if found) in the same step, may be 
         *     <b>null</b> for none
         * @param indexes the path of (nested) 0-based indexes to the field, the sum must be less than 
         *     {@link #getDataCount()}
         * @return the name of the field or empty if not known
         * @throws IOException if applying {@code valueCons} leads to an exception
         */
        public String getFieldName(IOConsumer<T> valueCons, int... indexes) throws IOException;

    }
    
    /**
     * Converts parsed data to primitive types. Implementations must be stateless.
     * 
     * @param <T> the type of data produced by parsing
     * @author Holger Eichelberger, SSE
     */
    public interface InputConverter<T> {

        /**
         * Converts parsed data returned by {@link ParseResult} to byte.
         * 
         * @param data the obtained data
         * @return the converted integer
         * @throws IOException if conversion fails
         */
        public byte toByte(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to integer.
         * 
         * @param data the obtained data
         * @return the converted integer
         * @throws IOException if conversion fails
         */
        public int toInteger(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to long.
         * 
         * @param data the obtained data
         * @return the converted long
         * @throws IOException if conversion fails
         */
        public long toLong(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to short.
         * 
         * @param data the obtained data
         * @return the converted long
         * @throws IOException if conversion fails
         */
        public short toShort(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to String.
         * 
         * @param data the obtained data
         * @return the converted String
         * @throws IOException if conversion fails
         */
        public String toString(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to double.
         * 
         * @param data the obtained data
         * @return the converted double
         * @throws IOException if conversion fails
         */
        public double toDouble(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to float.
         * 
         * @param data the obtained data
         * @return the converted float
         * @throws IOException if conversion fails
         */
        public float toFloat(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to Boolean.
         * 
         * @param data the obtained data
         * @return the converted Boolean
         * @throws IOException if conversion fails
         */
        public boolean toBoolean(T data) throws IOException;
        
        /**
         * Converts parsed data returned by {@link ParseResult} to an integer array.
         * 
         * @param data the obtained data
         * @return the converted integer array
         * @throws IOException if conversion fails
         */
        public int[] toIntegerArray(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to a double array.
         * 
         * @param data the obtained data
         * @return the converted double array
         * @throws IOException if conversion fails
         */
        public double[] toDoubleArray(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to a byte array.
         * 
         * @param data the obtained data
         * @return the converted double array
         * @throws IOException if conversion fails
         */
        public byte[] toByteArray(T data) throws IOException;

        /**
         * Converts parsed data returned by {@link ParseResult} to a date representation.
         * 
         * @param data the obtained data
         * @param format the target date format (see {@link FormatCache})
         * @return the converted date
         * @throws IOException if conversion fails
         */
        public default Date toDate(T data, String format) throws IOException {
            return FormatCache.parse(data.toString(), format);
        }
        
        /**
         * Converts parsed data returned by {@link ParseResult} to a date representation.
         * 
         * @param data the obtained data
         * @param format the target date format (see {@link FormatCache})
         * @return the converted date
         * @throws IOException if conversion fails
         */
        public default LocalDateTime toLocalDateTime(T data, String format) throws IOException {
            return FormatCache.toLocalDateTime(toDate(data, format));
        }

        /**
         * Converts parsed data returned by {@link ParseResult} to an instance of {@code enumType}. Primarily, a 
         * conversion to {@link #toInteger(Object)} and {@link IipEnum#getModelOrdinal()} is performed. If this fails, 
         * a name-based enum conversion is applied.
         *  
         * @param <E> the enum type
         * @param data the obtained data
         * @param enumType enumeration target type
         * @return the converted enum instance
         * @throws IOException if the conversion fails
         */
        public default <E extends Enum<E> & IipEnum> E toEnum(T data, Class<E> enumType) throws IOException {
            E result = null;
            try {
                result = IipEnum.valueByModelOrdinal(enumType, toInteger(data)); // result may be null
            } catch (IOException e) {
                // ok, result stays null
            }
            if (null == result) {
                try {
                    result = Enum.valueOf(enumType, toString(data));
                } catch (IllegalArgumentException e) {
                    throw new IOException("Cannot convert to enum " + enumType.getName() + ": " + e.getMessage());
                } catch (NullPointerException e) { // can be thrown offically
                    throw new IOException("Cannot convert to enum " + enumType.getName());
                }
            }
            return result;
        }
        
        /**
         * Converts parsed data returned by {@link ParseResult} to an object. [fallback dummy]
         * 
         * @param data the obtained data
         * @return the converted double array
         * @throws IOException if conversion fails
         */
        public Object toObject(T data) throws IOException;
        
    }
    
    /**
     * Parses a chunk of data received from a source.
     * 
     * @param data the data
     * @return parsing result
     * @throws IOException if parsing fails for some reasons
     */
    public ParseResult<T> parse(byte[] data) throws IOException;

    /**
     * Returns a type converter for parsed data.
     * 
     * @return the type converter
     */
    public InputConverter<T> getConverter();
    
}
