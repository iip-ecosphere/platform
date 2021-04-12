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

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * Some JSON utility methods, also reading/writing of specific types.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JsonUtils {

    /**
     * A proxy for {@link ServerAddress} as we do not want to have setters there.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ServerAddressHolder {
        private int port;
        private String host;
        private Schema schema;

        /**
         * Creates an instance (deserialization).
         */
        protected ServerAddressHolder() {
        }

        /**
         * Creates an instance from a given instance (serialization).
         * 
         * @param addr the instance to take data from
         */
        protected ServerAddressHolder(ServerAddress addr) {
            port = addr.getPort();
            host = addr.getHost();
            schema = addr.getSchema();
        }
        
        /**
         * Returns the port value.
         * 
         * @return the port
         */
        public int getPort() {
            return port;
        }
        
        /**
         * Defines the {@link #port} value.
         * 
         * @param port the new value of {@link #port}
         */
        public void setPort(int port) {
            this.port = port;
        }
        
        /**
         * Returns the host value.
         * 
         * @return the host
         */
        public String getHost() {
            return host;
        }
        
        /**
         * Defines the {@link #host} value.
         * 
         * @param host the new value of {@link #host}
         */
        public void setHost(String host) {
            this.host = host;
        }
        
        /**
         * Returns the schema value.
         * 
         * @return the schema
         */
        public Schema getSchema() {
            return schema;
        }

        /**
         * Defines the {@link #schema} value.
         * 
         * @param schema the new value of {@link #schema}
         */
        public void setSchema(Schema schema) {
            this.schema = schema;
        }
    }

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
