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

package test.de.iip_ecosphere.platform.support.rest;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.rest.RestTarget;

/**
 * Implements an empty Rest interface for simple testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestRest extends de.iip_ecosphere.platform.support.rest.Rest {

    /**
     * Just the server.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class TestServer implements RestServer {

        /**
         * Creates an instance.
         * 
         * @param port the port
         */
        private TestServer(int port) {
        }

        @Override
        public void definePost(String path, Route route) {
        }

        @Override
        public void defineGet(String path, Route route) {
        }

        @Override
        public void definePut(String path, Route route) {
        }

        @Override
        public void defineDelete(String path, Route route) {
        }

        @Override
        public Server start() {
            return this;
        }

        @Override
        public void stop(boolean dispose) {
        }
        
    }
    
    @Override
    public RestServer createServer(int port) {
        return new TestServer(port);
    }

    @Override
    public RestTarget createTarget(String uri) {
        return null;
    }

}
