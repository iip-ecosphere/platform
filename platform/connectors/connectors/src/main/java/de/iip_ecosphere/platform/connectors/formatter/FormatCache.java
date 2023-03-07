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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Caches formatter instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FormatCache {
    
    /**
     * Pre-defined known format for ISO8601.
     */
    public static final String ISO8601_FORMAT = "ISO8601";
    private static final Map<String, SimpleDateFormat> DATE_FORMATTER = new HashMap<>();
    private static final Map<Class<?>, DateConverter<?>> CONVERTERS = new HashMap<>();
    
    /**
     * Plugin to extend the date time conversion.
     * 
     * @param <T> the type 
     * @author Holger Eichelberger, SSE
     */
    public interface DateConverter<T> {
        
        /**
         * Turns data into a date.
         * 
         * @param data the data
         * @return the date instance
         */
        public Date toDate(T data);
        
        /**
         * The type being handled by this converter.
         * 
         * @return the type
         */
        public Class<T> getDataType(); 
        
    }
    
    /**
     * An abstract basic date converter.
     * 
     * @param <T> the type 
     * @author Holger Eichelberger, SSE
     */
    public abstract static class AbstractDateConverter<T> implements DateConverter<T> {
        
        private Class<T> cls;
        
        /**
         * Creates an abstract converter instance.
         * 
         * @param cls the class to create the instance for
         */
        protected AbstractDateConverter(Class<T> cls) {
            this.cls = cls;
        }
        
        @Override
        public Class<T> getDataType() {
            return cls;
        }
        
    }
    
    /**
     * Registers an additional converter.
     * 
     * @param converter the converter
     */
    public static void registerConverter(DateConverter<?> converter) {
        if (null != converter) {
            CONVERTERS.put(converter.getDataType(), converter);
        }
    }

    /**
     * Registers a default format for {@link SimpleDateFormat}.
     * 
     * @param name the symbolic name of the format
     * @param pattern the pattern format to apply
     * @throws IllegalArgumentException if {@code pattern} is illegal
     */
    public static void registerFormat(String name, String pattern) {
        if (null != name && null != pattern) {
            DATE_FORMATTER.put(name, new SimpleDateFormat(pattern));
        }
    }
    
    static {
        registerConverter(new AbstractDateConverter<Date>(Date.class) {

            @Override
            public Date toDate(Date data) {
                return data;
            }
            
        });
        registerConverter(new AbstractDateConverter<DateTime>(DateTime.class) {

            @Override
            public Date toDate(DateTime data) {
                return data.toDate();
            }
            
        });
        registerConverter(new AbstractDateConverter<LocalDateTime>(LocalDateTime.class) {

            @Override
            public Date toDate(LocalDateTime data) {
                return FormatCache.toDate(data);
            }
            
        }); 
        registerFormat(ISO8601_FORMAT, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }
    
    /**
     * Formats a date object to a given format.
     * 
     * @param data the data/date
     * @param format the format, may be from {@link SimpleDateFormat} or {@link #ISO8601_FORMAT}
     * @return if no format can be constructed
     * @throws IOException if formatting is not possible or the format is unknown
     */
    public static String format(Date data, String format) throws IOException {
        ISODateTimeFormat.dateTimeNoMillis();
        SimpleDateFormat sdf = getDateFormatter(format);
        return sdf.format(data);
    }

    /**
     * Uses one of the registered data converters to convert {@code data}.
     * 
     * @param <T> the type of {@code data}
     * @param cls the type of {@code data}
     * @param data the data (must not be <b>null</b>)
     * @return the converted instance, may be <b>null</b> if there is no conversion
     */
    @SuppressWarnings("unchecked")
    private static <T> Date convertToDate(Class<T> cls, Object data) {
        Date result = null;
        DateConverter<T> conv = (DateConverter<T>) CONVERTERS.get(cls);
        if (null != conv) {
            result = conv.toDate((T) data);
        }
        return result;
    }

    /**
     * Parses a date from the given {@code data} for the specified {@code format}.
     * 
     * @param data the data (may be <b>null</b>, the the result will be <b>null</b>)
     * @param format the format may be from {@link SimpleDateFormat} or {@link #ISO8601_FORMAT}
     * @return the parsed date
     * @throws IOException if parsing is not possible or the format is unknown
     */
    public static Date parse(Object data, String format) throws IOException {
        Date result = null;
        if (null != data) {
            result = convertToDate(data.getClass(), data);
            if (null == result) {
                String tmp = null == data ? "" : data.toString();
                SimpleDateFormat f = FormatCache.getDateFormatter(format);
                try {
                    return f.parse(tmp);
                } catch (ParseException e) {
                    throw new IOException(e);
                }
            }
        }
        return result;
    }
    
    /**
     * Returns a (cached) simple date formatter.
     * 
     * @param format the format for the requested formatter for a string for {@link SimpleDateFormat}
     * @return the formatter instance
     * @throws IOException if {@code format} is not valid
     */
    public static SimpleDateFormat getDateFormatter(String format) throws IOException {
        SimpleDateFormat result = DATE_FORMATTER.get(format);
        if (null == result) {
            try {
                result = new SimpleDateFormat(format);
                DATE_FORMATTER.put(format, result);
            } catch (IllegalArgumentException e) {
                throw new IOException(e);
            }
        }
        return result;
    }

    /**
     * Converts a {@link Date} value to {@link LocalDateTime} using instant and the 
     * system default time zone.
     * 
     * @param date the date to convert (may be <b>null</b>)
     * @return the converted date (may be <b>null</b>)
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        LocalDateTime result;
        if (null == date) {
            result = null;
        } else {
            result = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        }
        return result;
    }
    
    /**
     * Converts a {@link LocalDateTime} value to {@link Date} using instant and the 
     * system default time zone.
     * 
     * @param date the date to convert (may be <b>null</b>)
     * @return the converted date (may be <b>null</b>)
     */
    public static Date toDate(LocalDateTime date) {
        java.util.Date result;
        if (null == date) {
            result = null;
        } else {
            result = java.util.Date
                .from(date.atZone(ZoneId.systemDefault())
                .toInstant());
        }
        return result;
    }

}
