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

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.plugins.PluginManager;

/**
 * Generic access to Rest (server). Requires an implementing plugin of type {@link Rest} or an active 
 * {@link RestProviderDescriptor}. Simplified interface akin to Spark.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class Rest {
    
    private static Rest instance; 

    static {
        instance = PluginManager.getPluginInstance(Rest.class, RestProviderDescriptor.class);
    }

    /**
     * Returns the Rest instance.
     * 
     * @return the instance
     */
    public static Rest getInstance() {
        return instance;
    }
    
    /**
     * Manually sets the instance. Shall not be needed, but may be required in some tests.
     * 
     * @param rest the Rest instance
     */
    public static void setInstance(Rest rest) {
        if (null != rest) {
            instance = rest;
        }
    }
    
    /**
     * Request interface.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Request {

        /**
         * Returns the request body. 
         * 
         * @return the request body sent by the client
         */
        public String getBody();

    }

    /**
     * Response interface.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Response {

        /**
         * Sets the body.
         *
         * @param body the body
         */
        public void setBody(String body);

        /**
         * Returns the body.
         *
         * @return the body
         */
        public String getBody();

        /**
         * Sets the status code for the response.
         *
         * @param status the status
         */
        public void setStatus(int status);

    }

    /**
     * Represents a route (function).
     * 
     * @author Holger Eichelberger, SSE
     */
    @FunctionalInterface
    public interface Route {
        
        /**
         * Invoked when a request is made on this route's corresponding path e.g. '/hello'
         *
         * @param request  The request object providing information about the HTTP request
         * @param response The response object providing functionality for modifying the response
         * @return The content to be set in the response
         * @throws IOException if an I/O issue occurs
         */
        public Object handle(Request request, Response response) throws IOException;
        
    }

    /**
     * Represents a rest server.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface RestServer extends Server {

        /**
         * Defines a post route.
         * 
         * @param path the path
         * @param route the route
         */
        public void definePost(String path, Route route);

        /**
         * Defines a get route.
         * 
         * @param path the path
         * @param route the route
         */
        public void defineGet(String path, Route route);

        /**
         * Defines a put route.
         * 
         * @param path the path
         * @param route the route
         */
        public void definePut(String path, Route route);

        /**
         * Defines a delete route.
         * 
         * @param path the path
         * @param route the route
         */
        public void defineDelete(String path, Route route);
        
    }

    /**
     * Creates a server instance.
     * 
     * @param port the port to use
     * @return the server instance
     */
    public abstract RestServer createServer(int port);

    /**
     * Creates a server instance.
     * 
     * @param addr the server address (port taken)
     * @return the server instance
     */
    public RestServer createServer(ServerAddress addr) {
        return createServer(addr.getPort());
    }
    
    /**
     * Creates a web/REST target to call operations on, a kind of REST client.
     * 
     * @param uri web resource URI. May contain template parameters. Must not be <b>null</b>.
     * @return the rest target instance
     * @throws IllegalArgumentException in case the supplied string is not a valid URI template
     * @throws NullPointerException in case the supplied argument is <b>null</b>.
     */
    public abstract RestTarget createTarget(String uri);

}
