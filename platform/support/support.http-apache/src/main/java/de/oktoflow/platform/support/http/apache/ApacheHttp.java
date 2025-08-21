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

package de.oktoflow.platform.support.http.apache;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import de.iip_ecosphere.platform.support.http.HttpClient;
import de.iip_ecosphere.platform.support.http.HttpPost;
import de.iip_ecosphere.platform.support.http.HttpRequest;
import de.iip_ecosphere.platform.support.http.HttpResponse;

/**
 * Implements the HTTP interface by Apache.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ApacheHttp extends de.iip_ecosphere.platform.support.http.Http {
    
    /**
     * Represents a URI-based request.
     * 
     * @param <R> the actual implementation request type
     * @author Holger Eichelberger, SSE
     */
    private abstract class ApacheHttpUriRequest<R extends HttpUriRequest, S extends HttpRequest<S>> 
        implements HttpRequest<S> {
        
        private R request;

        /**
         * Creates a wrapping instance.
         * 
         * @param request the implementing instance
         */
        protected ApacheHttpUriRequest(R request) {
            this.request = request;
        }

        @SuppressWarnings("unchecked")
        @Override
        public S setHeader(String name, String value) {
            request.setHeader(name, value);
            return (S) this;
        }

        /**
         * Returns the implementing request.
         * 
         * @return the request instance
         */
        protected R getRequest() {
            return request;
        }
        
    }
    
    /**
     * Represents a POST request.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class ApacheHttpPost extends ApacheHttpUriRequest<org.apache.http.client.methods.HttpPost, HttpPost> 
        implements HttpPost {
        
        /**
         * Creates a wrapping instance.
         * 
         * @param uri the target URI
         */
        private ApacheHttpPost(String uri) {
            super(new org.apache.http.client.methods.HttpPost(uri));
        }
        
        @Override
        public HttpPost setEntity(String entity) throws IOException {
            try {
                getRequest().setEntity(new StringEntity(entity));
            } catch (UnsupportedEncodingException e) {
                throw new IOException(e);
            }
            return this;
        }
        
    }
    
    /**
     * Represents a request response.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class ApacheHttpResponse implements HttpResponse {

        private CloseableHttpResponse response;
        
        /**
         * Creates a wrapping instance.
         * 
         * @param response the implementing instance
         */
        private ApacheHttpResponse(CloseableHttpResponse response) {
            this.response = response;
        }

        @Override
        public String getEntityAsString() throws IOException {
            return EntityUtils.toString(response.getEntity());
        }

        @Override
        public int getStatusCode() {
            return response.getStatusLine().getStatusCode();
        }
        
        @Override
        public String getReasonPhrase() {
            return response.getStatusLine().getReasonPhrase();
        }

        @Override
        public void close() throws IOException {
            response.close();
        }

    }
    
    /**
     * Represents a HTTP client.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class ApacheHttpClient implements HttpClient {

        private CloseableHttpClient client;
        
        /**
         * Creates a wrapping instance.
         * 
         * @param client the implementing instance
         */
        private ApacheHttpClient(CloseableHttpClient client) {
            this.client = client;
        }
        
        @Override
        public HttpResponse execute(HttpRequest<?> request) throws IOException {
            return new ApacheHttpResponse(client.execute(((ApacheHttpUriRequest<?, ?>) request).getRequest()));
        }

        @Override
        public void close() throws IOException {
            client.close();
        }
        
    }
    
    @Override
    public HttpPost createPost(String uri) {
        return new ApacheHttpPost(uri);
    }

    @Override
    public HttpClient createClient() {
        return new ApacheHttpClient(HttpClients.createDefault());
    }

    @Override
    public HttpClient createPooledClient() {
        HttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        return new ApacheHttpClient(HttpClients.custom().setConnectionManager(poolingConnectionManager).build());
    }

}
