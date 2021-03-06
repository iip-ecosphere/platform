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

package de.iip_ecosphere.platform.support.iip_aas;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;

/**
 * Helper functions for active AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasUtils {

    /**
     * An empty URI for {@link #readUri(Object[], int, URI)}.
     */
    public static final URI EMPTY_URI;
    
    static {
        URI tmp;
        try {
            tmp = new URI("");
        } catch (URISyntaxException e) {
            tmp = null;
        }
        EMPTY_URI = tmp;
    }
    
    /**
     * Reads the first argument from {@code} args as String with default value empty.
     * 
     * @param args the array to take the value from 
     * @return the value
     */
    public static String readString(Object[] args) {
        return readString(args, 0);
    }

    /**
     * Reads the {@code index} argument from {@code} args as String with default value empty.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @return the value
     */
    public static String readString(Object[] args, int index) {
        return readString(args, index, "");
    }

    /**
     * Reads the {@code index} argument from {@code} args as String.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null ...
     * @return the value
     */
    public static String readString(Object[] args, int index, String dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        return null == param ? dflt : param.toString();
    }
    
    /**
     * Reads the {@code index} argument from {@code} args as int.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null, the value is no int...
     * @return the value
     */
    public static int readInt(Object[] args, int index, int dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        int result = dflt;
        if (null != param) {
            try {
                result = Integer.parseInt(param.toString());
            } catch (NumberFormatException e) {
                // handled by result = deflt
            }
        }
        return result;
    }

    /**
     * Reads the {@code index} argument from {@code} args as URI.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null
     * @return the value
     * @throws URISyntaxException if the value cannot be turned into an URI...
     */
    public static URI readUriEx(Object[] args, int index, URI dflt) throws URISyntaxException {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        URI result = dflt;
        if (null != param) {
            result = new URI(param.toString());
        } 
        return result;
    }

    /**
     * Reads the {@code index} argument from {@code} args as URI.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null, the value cannot be turned 
     *   into an URI...
     * @return the value
     */
    public static URI readUri(Object[] args, int index, URI dflt) {
        URI result;
        try {
            result = readUriEx(args, index, dflt);
        } catch (URISyntaxException e) {
            result = dflt;
        }
        return result;
    }
    
    /**
     * Modifies a given {@code id} so that it fits the needs of the implementation. Uses 
     * {@link AasFactory#fixId(String)}.
     * 
     * @param id the id
     * @return the fixed id
     */
    public static String fixId(String id) {
        return AasFactory.getInstance().fixId(id);
    }
    
    /**
     * Reads the {@code index} argument from {@code} args as map of strings.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null ...
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> readMap(Object[] args, int index, Map<String, String> dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        Map<String, String> result = dflt;
        if (null != param) {
            result = JsonUtils.fromJson(result, Map.class);
        }
        return result;
    }
    
    /**
     * Writes a map to JSON.
     * 
     * @param map the map
     * @return the JSON representation
     */
    public static String writeMap(Map<String, String> map) {
        return JsonUtils.toJson(map);
    }

}
