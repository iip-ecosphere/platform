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

package de.iip_ecosphere.platform.support.rest;

import java.io.IOException;

/**
 * Represents a Web target for RESTful API access. Abstracted from Glassfish/Jersey.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface RestTarget {

    /**
     * Represents a request.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Request {

        /**
         * Updates the request by appending path to the actual/original URI. When constructing the final path, a '/' 
         * separator will be inserted between the existing path and the supplied path if necessary. Existing '/' 
         * characters are preserved thus a single value can represent multiple URI path segments. 
         * 
         * @param path the path, may contain URI template parameters
         * @return <b>this</b> for chaining, an updated request instance
         * @throws NullPointerException if path is <b>null</b>
         */
        public Request addPath(String path);
        
        /**
         * Updates the request instance by configuring a query parameter on the current URI.
         * If multiple values are supplied the parameter will be added once per value. In case a single
         * {@code null} value is entered, all parameters with that name are removed (if present) from
         * the collection of query parameters inherited from the current target.
         *
         * @param name   the query parameter name, may contain URI template parameters
         * @param values the query parameter value(s), each object will be converted
         *               to a {@code String} using its {@code toString()} method. Stringified
         *               values may contain URI template parameters
         * @return a new target instance
         * @throws NullPointerException if the parameter name is <b>null</b> or if there are multiple
         *         values present and any of those values is <b>null</b>
         */
        public Request addQueryParam(String name, String... values);

        /**
         * Requests a JSON invocation.
         * 
         * @return the invocation instance.
         */
        public Invocation requestJson();

    }
    
    /**
     * Represents a request invocation.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Invocation {

        /**
         * Invoke HTTP GET method for the current request synchronously, read the message entity input stream as an 
         * instance of specified Java type.
         * 
         * @param <T> entity instance Java type
         * @param cls the type of entity
         * @return the invocation request in the specified type
         * @throws IOException if invoking/turning the result into the specified type fails
         */
        public <T> T get(Class<T> cls) throws IOException;

        /**
         * Invoke HTTP GET method for the current request synchronously, read the message entity input stream as an 
         * instance of String.
         * 
         * @return the invocation request in the specified type
         * @throws IOException if invoking/turning the result into a String fails
         * @see #get(Class)
         */
        public default String getAsString() throws IOException {
            return get(String.class);
        }
        
        /**
         * Invoke HTTP PUT method for the current request synchronously, taking the creation of the invocation 
         * (e.g. JSON) into account for encoding/creating the body entity.
         * 
         * @param body the body
         * @throws IOException if invoking/turning the result into a String fails
         */
        public void put(String body) throws IOException;
        
        /**
         * Invoke HTTP DELETE method for the current request synchronously.
         * 
         * @throws IOException if invoking delete fails
         */
        public void delete() throws IOException;

    }

    /**
     * Creates a request.
     * 
     * @return the request
     */
    public Request createRequest();

}
