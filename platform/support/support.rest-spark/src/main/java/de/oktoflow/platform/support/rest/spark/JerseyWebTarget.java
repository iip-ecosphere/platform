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

import java.io.IOException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import de.iip_ecosphere.platform.support.rest.RestTarget;

/**
 * Wraps a Glassfish/Jersey web target.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JerseyWebTarget implements RestTarget {
    
    private WebTarget webTarget;
    
    /**
     * Creates a wrapping instance.
     * 
     * @param uri the uri to build a web target for
     */
    JerseyWebTarget(String uri) {
        webTarget = ClientBuilder.newClient().target(uri);
    }
    
    @Override
    public Request createRequest() {
        return new JerseyRequest(webTarget);
    }

    /**
     * Wraps a Glassfish/Jersey request.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class JerseyRequest implements Request {
        
        private WebTarget webTarget;

        /**
         * Creates a wrapping instance.
         * 
         * @param webTarget the instance to be wrapped
         */
        private JerseyRequest(WebTarget webTarget) {
            this.webTarget = webTarget;
        }
        
        @Override
        public Request addPath(String path) {
            this.webTarget = this.webTarget.path(path);
            return this;
        }
        
        @Override
        public Request addQueryParam(String name, String... values) {
            this.webTarget = this.webTarget.queryParam("tag", (Object[]) values);
            return this;
        }

        @Override
        public Invocation requestJson() {
            return new JerseyInvocationBuilder(webTarget, MediaType.APPLICATION_JSON);
        }
        
    }
    
    /**
     * Wraps a Glassfish/Jersey invocation builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class JerseyInvocationBuilder implements Invocation {
        
        private javax.ws.rs.client.Invocation.Builder invocationBuilder;
        private String mediaType;
        
        /**
         * Creates a wrapping instance.
         * 
         * @param webTarget the request target
         * @param mediaType the media type, see {@link MediaType}
         */
        private JerseyInvocationBuilder(WebTarget webTarget, String mediaType) {
            this.mediaType = mediaType;
            this.invocationBuilder = webTarget.request(mediaType);
        }

        @Override
        public <T> T get(Class<T> cls) throws IOException {
            try {
                return invocationBuilder.get().readEntity(cls);
            } catch (ProcessingException | IllegalStateException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void put(String body) throws IOException {
            try {
                Entity<?> entity;
                switch(mediaType) {
                case MediaType.APPLICATION_JSON:
                    entity = Entity.json(body);
                    break;
                default:
                    entity = Entity.text(body); // fallback
                    break;
                }
                invocationBuilder.put(entity);
            } catch (ProcessingException | IllegalStateException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void delete() throws IOException {
            try {
                invocationBuilder.delete(); // ignore response for now
            } catch (ProcessingException | IllegalStateException e) {
                throw new IOException(e);
            }
        }
        
    }

}
