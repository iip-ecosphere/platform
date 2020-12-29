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
 * Represents a reusable server address.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServerAddress {

    public static final String LOCALHOST = "localhost";
    private Schema schema;
    private String host;
    private int port;

    /**
     * Creates a new localhost server address instance on an ephemerial port.
     * 
     * @param schema the schema
     */
    public ServerAddress(Schema schema) {
        this(schema, LOCALHOST, NetUtils.getEphemeralPort());
    }

    /**
     * Creates a new localhost server address instance.
     * 
     * @param schema the schema
     * @param port the port number (ignored if negative)
     */
    public ServerAddress(Schema schema, int port) {
        this(schema, LOCALHOST, port);
    }

    /**
     * Creates a new server address instance.
     * 
     * @param schema the schema
     * @param host the hostname (turned to "localhost" if <b>null</b> or empty)
     * @param port the port number
     */
    public ServerAddress(Schema schema, String host, int port) {
        this.schema = schema;
        this.host = host;
        if (null == this.host || this.host.length() == 0) {
            this.host = LOCALHOST;
        }
        this.port = port;
    }

    /**
     * Returns the schema.
     * 
     * @return the schema
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Returns the host name.
     * 
     * @return the host name
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port number.
     * 
     * @return the port number
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Returns the URI representation of this server address. Intended to be overridden by subclasses.
     * 
     * @return the URI representation
     */
    public String toUri() {
        return toServerUri();
    }
    
    /**
     * Returns the URI representation of the server address apart from overridden extensions in {@link #toUri()}.
     * 
     * @return the URI representation of the server address
     */
    public final String toServerUri() {
        return schema.toUri() + host + ":" + port;
    }
    
}
