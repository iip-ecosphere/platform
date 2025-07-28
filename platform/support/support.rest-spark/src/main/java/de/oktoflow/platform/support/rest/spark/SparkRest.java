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

package de.oktoflow.platform.support.rest.spark;

import de.iip_ecosphere.platform.support.Server;
import spark.Spark;

/**
 * Implements the Rest interface by Spark.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SparkRest extends de.iip_ecosphere.platform.support.rest.Rest {
    
    /**
     * Wraps a spark request.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class SparkRequest implements Request {

        private spark.Request request;
        
        /**
         * Creates a interface request instance by wrapping the corresponding spark instance.
         * 
         * @param request the spark instance
         */
        private SparkRequest(spark.Request request) {
            this.request = request;
        }

        @Override
        public String getBody() {
            return request.body();
        }

    }

    /**
     * Wraps a spark response.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class SparkResponse implements Response {
        
        private spark.Response response;
        
        /**
         * Creates a interface response instance by wrapping the corresponding spark instance.
         * 
         * @param response the spark instance
         */
        private SparkResponse(spark.Response response) {
            this.response = response;
        }

        @Override
        public void setBody(String body) {
            response.body(body);
        }

        @Override
        public String getBody() {
            return response.body();
        }

        @Override
        public void setStatus(int status) {
            response.status(status);
        }
        
    }

    /**
     * Wraps the spark rest server.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class SparkRestServer implements RestServer {

        /**
         * Creates an instance.
         * 
         * @param port the port to use
         */
        private SparkRestServer(int port) {
            Spark.port(port);
        }

        /**
         * Wraps an interface route to a spark route.
         * 
         * @param route the interface route
         * @return the spark route
         */
        private spark.Route createRoute(Route route) {
            return (req, res) -> route.handle(new SparkRequest(req), new SparkResponse(res));
        }

        @Override
        public void definePost(String path, Route route) {
            Spark.post(path, createRoute(route));
        }

        @Override
        public void defineGet(String path, Route route) {
            Spark.get(path, createRoute(route));
        }

        @Override
        public void definePut(String path, Route route) {
            Spark.put(path, createRoute(route));
        }

        @Override
        public void defineDelete(String path, Route route) {
            Spark.delete(path, createRoute(route));
        }

        @Override
        public Server start() {
            Spark.awaitInitialization();
            return this;
        }

        @Override
        public void stop(boolean dispose) {
            Spark.stop();
            Spark.awaitStop();
        }
        
    }
    
    @Override
    public RestServer createServer(int port) {
        return new SparkRestServer(port);
    }

}
