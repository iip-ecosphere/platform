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

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Caches formatter instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FormatCache {
    
    public static final String ISO8601_FORMAT = "ISO8601";
    private static final Map<String, SimpleDateFormat> DATE_FORMATTER = new HashMap<>();
    private static final DateTimeFormatter ISO8601_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();
    
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
        String result;
        if (ISO8601_FORMAT.equals(format)) {
            LocalDate d = LocalDate.fromDateFields(data);
            result = ISO8601_FORMATTER.print(d);
        } else {
            SimpleDateFormat sdf = getDateFormatter(format);
            result = sdf.format(data);
        }
        return result;
    }

    /**
     * Parses a date from the given {@code data} for the specified {@code format}.
     * 
     * @param data the data
     * @param format the format may be from {@link SimpleDateFormat} or {@link #ISO8601_FORMAT}
     * @return the parsed date
     * @throws IOException if parsing is not possible or the format is unknown
     */
    public static Date parse(Object data, String format) throws IOException {
        Date result = null;
        if (data instanceof Date) {
            result = (Date) data;
        } else {
            String tmp = null == data ? "" : data.toString();
            if (ISO8601_FORMAT.equals(format)) {
                result = ISO8601_FORMATTER.parseLocalDate(tmp).toDate();
            } else {
                SimpleDateFormat f = FormatCache.getDateFormatter(format);
                try {
                    return f.parse((String) data);
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
