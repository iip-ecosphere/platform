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

package de.iip_ecosphere.platform.support;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.iip_ecosphere.platform.support.commons.Commons;

/**
 * String utility functions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class StringUtils {
    
    /**
     * Escapes the characters in a {@code String} using Java String rules.
     * Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)
     *
     * @param input  String to escape values in, may be <b>null</b>
     * @return String with escaped values, <b>null</b> if <b>null</b> string input
     */
    public static String escapeJava(final String input) {
        return Commons.getInstance().escapeJava(input);
    }

    /**
     * Unescapes any Java literals found in the {@code String}.
     *
     * @param input  the {@code String} to unescape, may be <b>null</b>
     * @return a new unescaped {@code String}, <b>null</b> if <b>null</b> string input
     */
    public static String unescapeJava(final String input) {
        return Commons.getInstance().unescapeJava(input);
    }
    
    /**
     * Escapes the characters in a {@code String} using Json String rules.
     * Escapes any values it finds into their Json String form.
     * Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)
     *
     * @param input  String to escape values in, may be <b>null</b>
     * @return String with escaped values, <b>null</b> if null string input
     */
    public static String escapeJson(final String input) {
        return Commons.getInstance().escapeJson(input);
    }

    /**
     * Unescapes any Json literals found in the {@code String}.
     *
     * @param input  the {@code String} to unescape, may be <b>null</b>
     * @return A new unescaped {@code String}, <b>null</b> if null string input
     */
    public static String unescapeJson(final String input) {
        return Commons.getInstance().unescapeJson(input);
    }
    
    /**
     * Returns either the passed in CharSequence, or if the CharSequence is
     * whitespace, empty ({@code ""}) or <b>null</b>, the value of {@code defaultStr}.
     *
     * @param <T> the specific kind of CharSequence
     * @param str the CharSequence to check, may be <b>null</b>
     * @param defaultStr  the default CharSequence to return
     *  if the input is whitespace, empty ({@code ""}) or <b>null</b>
     * @return the passed in CharSequence, or the default
     */
    public static <T extends CharSequence> T defaultIfBlank(final T str, final T defaultStr) {
        return Commons.getInstance().defaultIfBlank(str, defaultStr);
    }

    /**
     * Returns either the passed in CharSequence, or if the CharSequence is
     * empty or <b>null</b>, the value of {@code defaultStr}.
     *
     * @param <T> the specific kind of CharSequence
     * @param str  the CharSequence to check, may be null
     * @param defaultStr  the default CharSequence to return
     *  if the input is empty ({@code ""}) or <b>null</b>
     * @return the passed in CharSequence, or the default
     */
    public static <T extends CharSequence> T defaultIfEmpty(final T str, final T defaultStr) {
        return Commons.getInstance().defaultIfEmpty(str, defaultStr);
    }
    
    /**
     * Gets a CharSequence length or {@code 0} if the CharSequence is
     * {@code null}.
     *
     * @param cs a CharSequence, may be <b>null</b>
     * @return CharSequence length or {@code 0} if the CharSequence is <b>null</b>.
     */
    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * Checks if a CharSequence is empty ({@code ""}), <b>null</b> or whitespace only.
     *
     * @param cs  the CharSequence to check, may be <b>null</b>
     * @return {@code true} if the CharSequence is <b>null</b>, empty or whitespace only
     */
    public static boolean isBlank(final CharSequence cs) {
        final int strLen = length(cs);
        if (strLen == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a CharSequence is not empty (""), not null and not whitespace only.
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     *  not empty and not null and not whitespace only
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }
    
    /**
     * Replaces a String with another String inside a larger String, once.
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for, may be null
     * @param replacement  the String to replace with, may be null
     * @return the text with any replacements processed,
     *  <b>null</b> if null String input
     */
    public static String replaceOnce(final String text, final String searchString, final String replacement) {
        return Commons.getInstance().replaceOnce(text, searchString, replacement);
    }

    /**
     * Checks if a CharSequence is empty ({@code ""}) or <b>null</b>.
     *
     * @param cs  the CharSequence to check, may be <b>null</b>
     * @return {@code true} if the CharSequence is empty or <b>null</b>
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    
    /**
     * Turns an object to an readable string, usually using reflection. Uses some default
     * style.
     * 
     * @param obj the object
     * @return the string representation
     */
    public static String toString(Object obj) {
        return Commons.getInstance().toString(obj);
    }
    
    /**
     * Turns an object to an readable string, usually using reflection. Uses oktoflow short style.
     * 
     * @param obj the object
     * @return the string representation
     */
    public static String toStringShortStyle(Object obj) {
        return Commons.getInstance().toStringShortStyle(obj);
    }

    /**
     * Turns an object to an readable string, usually using reflection. Uses a multi-line style.
     * 
     * @param obj the object
     * @return the string representation
     */
    public static String toStringMultiLineStyle(Object obj) {
        return Commons.getInstance().toStringMultiLineStyle(obj);
    }

    /**
     * Removes a substring only if it is at the beginning of a source string,
     * otherwise returns the source string.

     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  <b>null</b> if <b>null</b> String input
     */
    public static String removeStart(String str, String remove) {
        return Commons.getInstance().removeStart(str, remove);
    }
    
    /**
     * Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  <b>null</b> if <b>null</b> String input
     */
    public static String removeEnd(String str, String remove) {
        return Commons.getInstance().removeEnd(str, remove);
    }
    
    /**
     * Turns the tokens of the given tokenizer into a list.
     * 
     * @param tokenizer the tokenizer
     * @return the list
     */
    public static List<String> toTokenList(StringTokenizer tokenizer) {
        List<String> result = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    }

    /**
     * Turns a string list to an array.
     * 
     * @param list the list, may be <b>null</b>
     * @return the corresponding array, also <b>null</b> if {@code list} is <b>null</b>
     */
    public static String[] toArray(List<String> list) {
        return null == list ? null : list.toArray(new String[list.size()]);
    }

    /**
     * Turns the tokens of the given tokenizer into an array.
     * 
     * @param tokenizer the tokenizer
     * @return the array
     */
    public static String[] toTokenArray(StringTokenizer tokenizer) {
        return toArray(toTokenList(tokenizer));
    }

}
