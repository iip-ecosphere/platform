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
        
        /**
         * Returns the value of a request parameter or, if supported, the value of a path variable.
         * 
         * @param name the name of the parameter/variable, see {@link RestServer#toPathVariable(String)}
         * @return the value, <b>null</b> if there is no such parameter/variable
         */
        public String getParam(String name);

        /**
         * Returns the content type.
         * 
         * @return the content type
         */
        public String getContentType();

        /**
         * Returns the full query string.
         * 
         * @return the query string
         */
        public String getQueryString();

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

        /**
         * Sets the content/response type. Default is "text/html".
         * 
         * @param type the type
         */
        public void setType(String type);

        /**
         * Sets the content/response type to "application/json".
         * 
         * @see #setType(String)
         */
        public default void setApplicationJsonType() {
            setType("application/json");
        }

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
     * Represents a route filter.
     * 
     * @author Holger Eichelberger, SSE
     */
    @FunctionalInterface
    public interface Filter {
        
        /**
         * Invoked when a request is made on this route's corresponding path e.g. '/hello'
         *
         * @param request  The request object providing information about the HTTP request
         * @param response The response object providing functionality for modifying the response
         * @throws IOException if an I/O issue occurs
         */
        public void handle(Request request, Response response) throws IOException;
        
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

        /**
         * Defines a filter on all paths.
         * 
         * @param filter the filter
         */
        public void defineBefore(Filter filter);

        /**
         * Defines a filter on a given path.
         * 
         * @param path the path
         * @param filter the filter
         */
        public void defineBefore(String path, Filter filter);
        
        /**
         * Halts processing a request.
         * 
         * @param status the status
         * @param body the body
         */
        public void halt(int status, String body);

        /**
         * Returns whether path variables are supported.
         * 
         * @return {@code true} for path variables, {@code false} else
         */
        public boolean supportsPathVariables();
            
        /**
         * Turns a name to a path variable.
         * 
         * @param name the name
         * @return the path variable for {@code name}, may be {@code name} if no path variables are supported
         * @see #toPathVariable(String)
         */
        public String toPathVariable(String name);

        /**
         * Set the connection to be secure, using the specified keystore and
         * truststore. This has to be called before any route mapping is done. 
         *
         * @param keystoreFile       The keystore file location as string
         * @param keystorePassword   the password for the keystore
         * @param certAlias          the default certificate Alias
         */
        public void secure(String keystoreFile, String keystorePassword, String certAlias);
        
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
