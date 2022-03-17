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

package de.iip_ecosphere.platform.support.iip_aas.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.iip_aas.config.ServerAddressHolder;

/**
 * Some JSON utility methods, also reading/writing of specific types.
 * 
 * @author Holger Eichelberger, SSE
 * @author Lemur Project (<a href="http://lemurproject.org/galago-license">BSD License</a>) 
 */
public class JsonUtils {

    /**
     * Reads a {@link ServerAddress} from a JSON string.
     * 
     * @param json the JSON value, usually a String
     * @return the server address or <b>null</b> if reading fails
     * @see #toJson(ServerAddress)
     */
    public static ServerAddress serverAddressFromJson(Object json) {
        ServerAddress result = null;
        if (null != json) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ServerAddressHolder tmp = objectMapper.readValue(json.toString(), ServerAddressHolder.class);
                result = new ServerAddress(tmp.getSchema(), tmp.getHost(), tmp.getPort());
            } catch (JsonProcessingException e) {
                // result = null;
            }
        }
        return result;        
    }
    
    /**
     * Turns a {@link ServerAddress} into JSON.
     * 
     * @param address the address (may be <b>null</b>)
     * @return the JSON string or an empty string in case of problems/no address
     * @see #serverAddressFromJson(Object)
     */
    public static String toJson(ServerAddress address) {
        String result = "";
        if (null != address) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ServerAddressHolder tmp = new ServerAddressHolder(address);
                result = objectMapper.writeValueAsString(tmp);
            } catch (JsonProcessingException e) {
                // handled by default value
            }
        } 
        return result;
    }

    /**
     * Turns a {@link ServerAddress} into JSON.
     * 
     * @param obj the object (may be <b>null</b>), must have getters/setters for all attributes and a no-arg constructor
     *   no-arg constructor
     * @return the JSON string or an empty string in case of problems/no address
     * @see #fromJson(Object, Class)
     */
    public static String toJson(Object obj) {
        String result = "";
        if (null != obj) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                result = objectMapper.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                // handled by default value
            }
        } 
        return result;
    }
    
    /**
     * Reads an Object from a JSON string.
     * 
     * @param <R> the object type, must have getters/setters for all attributes and a no-arg constructor
     * @param json the JSON value (usually a String)
     * @param cls the class of the type to read
     * @return the server address or <b>null</b> if reading fails
     * @see #toJson(Object)
     */
    public static <R> R fromJson(Object json, Class<R> cls) {
        R result = null;
        if (null != json) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                result = objectMapper.readValue(json.toString(), cls);
            } catch (JsonProcessingException e) {
                //result = null;
            }
        }
        return result; 
    }
    
    /**
     * Escapes an input string for JSON. Taken over from 
     * <a href="https://stackoverflow.com/questions/34706849/how-do-i-unescape-a-json-string-using-java-jackson">
     * Stackoverflow</a> and <a href="http://lemurproject.org/">Lemur Project</a>. The respective methods from <a 
     * href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringEscapeUtils.html">
     * Apache Commons Lang3</a> are too slow for our purpose.
     * 
     * @param input the input string
     * @return the escaped string
     */
    public static String escape(String input) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            int chx = (int) ch;

            // let's not put any nulls in our strings
            assert (chx != 0);

            if (ch == '\n') {
                output.append("\\n");
            } else if (ch == '\t') {
                output.append("\\t");
            } else if (ch == '\r') {
                output.append("\\r");
            } else if (ch == '\\') {
                output.append("\\\\");
            } else if (ch == '"') {
                output.append("\\\"");
            } else if (ch == '\b') {
                output.append("\\b");
            } else if (ch == '\f') {
                output.append("\\f");
            } else if (chx >= 0x10000) {
                assert false : "Java stores as u16, so it should never give us a character that's bigger than 2 bytes. "
                    + "It literally can't.";
            } else if (chx > 127) {
                output.append(String.format("\\u%04x", chx));
            } else {
                output.append(ch);
            }
        }

        return output.toString();
    }

    /**
     * Unescapes an input string from JSON. Taken over from <a href=
     * "https://stackoverflow.com/questions/34706849/how-do-i-unescape-a-json-string-using-java-jackson">
     * Stackoverflow</a> and <a href="http://lemurproject.org/">Lemur Project</a>.
     * The respective methods from <a href=
     * "https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/StringEscapeUtils.html">
     * Apache Commons Lang3</a> are too slow for our purpose.
     * 
     * @param input the input string
     * @return the unescaped string
     */
    public static String unescape(String input) {
        StringBuilder builder = new StringBuilder();

        int i = 0;
        while (i < input.length()) {
            char delimiter = input.charAt(i);
            i++; // consume letter or backslash

            if (delimiter == '\\' && i < input.length()) {

                // consume first after backslash
                char ch = input.charAt(i);
                i++;

                if (ch == '\\' || ch == '/' || ch == '"' || ch == '\'') {
                    builder.append(ch);
                } else if (ch == 'n') {
                    builder.append('\n');
                } else if (ch == 'r') {
                    builder.append('\r');
                } else if (ch == 't') {
                    builder.append('\t');
                } else if (ch == 'b') {
                    builder.append('\b');
                } else if (ch == 'f') {
                    builder.append('\f');
                } else if (ch == 'u') {
                    StringBuilder hex = new StringBuilder();

                    // expect 4 digits
                    if (i + 4 > input.length()) {
                        throw new RuntimeException("Not enough unicode digits! ");
                    }
                    for (char x : input.substring(i, i + 4).toCharArray()) {
                        if (!Character.isLetterOrDigit(x)) {
                            throw new RuntimeException("Bad character in unicode escape.");
                        }
                        hex.append(Character.toLowerCase(x));
                    }
                    i += 4; // consume those four digits.

                    int code = Integer.parseInt(hex.toString(), 16);
                    builder.append((char) code);
                } else {
                    throw new RuntimeException("Illegal escape sequence: \\" + ch);
                }
            } else { // it's not a backslash, or it's the last character.
                builder.append(delimiter);
            }
        }

        return builder.toString();
    }

}
