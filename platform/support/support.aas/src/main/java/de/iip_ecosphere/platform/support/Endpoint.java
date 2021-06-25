/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

/**
 * Implements a reusable server endpoint. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class Endpoint extends ServerAddress {

    private String endpoint;
    
    /**
     * Creates a new localhost endpoint instance on an ephemerial port.
     * 
     * @param schema the schema
     * @param endpoint the endpoint path on the server (if it does not start with a "/", a leading "/" will be added)
     */
    public Endpoint(Schema schema, String endpoint) {
        super(schema);
        this.endpoint = checkEndpoint(endpoint);
    }

    /**
     * Creates a new localhost endpoint instance.
     * 
     * @param schema the schema
     * @param port the port number (ignored if negative)
     * @param endpoint the endpoint path on the server (if it does not start with a "/", a leading "/" will be added)
     */
    public Endpoint(Schema schema, int port, String endpoint) {
        super(schema, port);
        this.endpoint = checkEndpoint(endpoint);
    }

    /**
     * Creates a new endpoint instance.
     * 
     * @param schema the schema
     * @param host the hostname (turned to "localhost" if <b>null</b> or empty)
     * @param port the port number
     * @param endpoint the endpoint path on the server (if it does not start with a "/", a leading "/" will be added)
     */
    public Endpoint(Schema schema, String host, int port, String endpoint) {
        super(schema, host, port);
        this.endpoint = checkEndpoint(endpoint);
    }

    /**
     * Creates a new endpoint based on a given server address.
     * 
     * @param server the server address
     * @param endpoint the endpoint path on the server (if it does not start with a "/", a leading "/" will be added)
     */
    public Endpoint(ServerAddress server, String endpoint) {
        super(server.getSchema(), server.getHost(), server.getPort());
        this.endpoint = checkEndpoint(endpoint);
    }

    /**
     * Returns the endpoint.
     * 
     * @return the endpoint with leading "/"
     */
    public String getEndpoint() {
        return endpoint;
    }
    
    /**
     * Returns the URI representation of this endpoint.
     * 
     * @return the URI representation
     */
    public String toUri() {
        return super.toUri() + endpoint;
    }

    /**
     * Checks/fixes an endpoint path.
     * 
     * @param endpoint the endpoint path
     * @return the (fixed) endpoint path
     */
    public static String checkEndpoint(String endpoint) {
        if (null == endpoint) {
            endpoint = "";
        }
        if (endpoint.length() > 0 && !endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        return endpoint;
    }
    
    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof Endpoint) {
            Endpoint o = (Endpoint) other;
            result = super.equals(other) && endpoint.equals(o.endpoint); 
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ endpoint.hashCode();
    }

}
