/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.spring;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Some utility functions. [public for testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public class Utils {

    /**
     * Formats {@code number} to <a href="https://en.wikipedia.org/wiki/Mebibyte">Mebibytes</a> (g/m only) without 
     * fractions and bytes as base unit. For input outside the m/g range, the input is returned. Please be aware of 
     * number overflow for larger g values. [public for testing]
     * 
     * @param number the number to be formatted
     * @return the formatted number
     * @see #formatToMeBi(long, int)
     */
    public static String formatToMeBi(long number) {
        return formatToMeBi(number, 0);
    }

    /**
     * Formats {@code number} to <a href="https://en.wikipedia.org/wiki/Mebibyte">Mebibytes</a> (g/m only) without 
     * fractions. For input outside the m/g range (including t in terms of g), the input is 
     * returned. [public for testing]
     * 
     * @param number the number to be formatted
     * @param baseUnit 0 = bytes, 1 = kByte, 2 = mByte, 3 = gByte, ...
     * @return the formatted number
     */
    public static String formatToMeBi(long number, int baseUnit) {
        String result;
        int unit = baseUnit; // k=1, m=2, g=3
        long rem = number;
        while (rem >= 1024) {
            rem /= 1024;
            unit++;
        }
        if (2 == unit) {
            result = rem + "m";
        } else if (3 == unit) {
            result = rem + "g";
        } else if (4 == unit) {
            result = 1024 * rem + "g";
        } else {
            result = String.valueOf(number);
        }
        return result;
    }

    /**
     * Checks, formats and adds a long megabyte value as <a href="https://en.wikipedia.org/wiki/Mebibyte">Mebibytes</a> 
     * property.
     * 
     * @param properties the properties to be modified as a side effect
     * @param key the key in {@code Properties} to modify
     * @param value the number as value in megabytes to be added
     * @param deflt the formatted default if {@code number} is not positive, may be <b>null</b> for no property at all
     * 
     * @see #formatToMeBi(long)
     * @see #addProperty(Map, String, Number, String, Predicate, Function)
     */
    public static void addPropertyIfPositiveToMeBi(Map<String, String> properties, String key, Number value, 
        String deflt) {
        addProperty(properties, key, value, deflt, i -> i.longValue() > 0, i -> formatToMeBi(i.longValue(), 2));
    }

    /**
     * Checks, formats and adds an integer deployer property.
     * 
     * @param properties the properties to be modified as a side effect
     * @param key the key in {@code Properties} to modify
     * @param value the number as value to be added
     * @param deflt the formatted default if {@code number} is not positive, may be <b>null</b> for no property at all
     * @see #addProperty(Map, String, Number, String, Predicate, Function)
     */
    public static void addPropertyIfPositiveToInt(Map<String, String> properties, String key, Number value, 
        String deflt) {
        addProperty(properties, key, value, deflt, i -> i.intValue() > 0, i -> String.valueOf(i.intValue()));
    }
    
    // checkstyle: stop parameter number check

    /**
     * Checks, formats and adds a deployer property.
     * 
     * @param properties the properties to be modified as a side effect
     * @param key the key in {@code Properties} to modify
     * @param value the value to be added
     * @param deflt the formatted default if {@code number} is not acceptable, may be <b>null</b> to add nothing as 
     *   default value
     * @param cond checks {@code value}, if {@code true} format via {@code formatter} and add as {@code key} to 
     *   {@code properties}, else (if {@code deflt} is not <b>null</b>, add {@code deflt} as {@code key} to 
     *   {@code properties}
     * @param formatter the formatter for {@code number}
     */
    public static void addProperty(Map<String, String> properties, String key, Number value, String deflt, 
        Predicate<Number> cond, Function<Number, String> formatter) {
        String result;
        if (null != value && null != cond && cond.test(value)) {
            result = formatter.apply(value);
        } else {
            result = deflt;
        }
        if (null != result) {
            properties.put(key, result);
        }
    }

    // checkstyle: resume parameter number check

}
