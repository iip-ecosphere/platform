/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import de.iip_ecosphere.platform.support.Schema;

/**
 * An endpoint validator based on {@link RuntimeSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RuntimeSetupEndpointValidator extends BasicEndpointValidator {
    
    private URI uri;

    /**
     * Creates a runtime setup endpoint validator based on the given URI.
     * 
     * @param uri the URI overriding the data
     */
    private RuntimeSetupEndpointValidator(URI uri) {
        this.uri = uri;
    }

    /**
     * Creates an endpoint validator based on {@link RuntimeSetup}. If there is no runtime setup or the accessed URI 
     * is invalid, a {@link BasicEndpointValidator} is returned.
     * 
     * @param accessor the accessor defining the URI to be used
     * @return the endpoint validator
     */
    public static EndpointValidator create(Function<RuntimeSetup, String> accessor) {
        EndpointValidator result;
        RuntimeSetup setup = RuntimeSetup.load(() -> null, false);
        if (null == setup) {
            result = new BasicEndpointValidator();
        } else {
            try {
                result = new RuntimeSetupEndpointValidator(new URI(accessor.apply(setup)));
            } catch (URISyntaxException e) {
                result = new BasicEndpointValidator();
            }
        }
        return result;
    }

    @Override
    public String validateHost(String host, EndpointHolder holder) {
        return holder.isEphmemeral() ? uri.getHost() : host;
    }

    @Override
    public int validatePort(int port, EndpointHolder holder) {
        return holder.isEphmemeral() ? uri.getPort() : port;
    }

    @Override
    public Schema validateSchema(Schema schema, EndpointHolder holder) {
        Schema result = schema;
        if (holder.isEphmemeral()) {
            try {
                result = Schema.valueOf(uri.getScheme().toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        return result;
    }

    @Override
    public String validatePath(String path, EndpointHolder holder) {
        String result = path;
        if (holder.isEphmemeral()) {
            result = uri.getPath();
            while (result.startsWith("/")) {
                result = result.substring(1);
            }
        }
        return result;
    }
}
