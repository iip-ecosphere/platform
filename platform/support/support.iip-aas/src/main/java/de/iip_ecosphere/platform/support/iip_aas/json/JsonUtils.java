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

}
