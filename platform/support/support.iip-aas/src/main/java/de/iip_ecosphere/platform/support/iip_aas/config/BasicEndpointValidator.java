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

import de.iip_ecosphere.platform.support.Schema;

/**
 * A basic endpoint holder validator just passing through all input data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BasicEndpointValidator implements EndpointValidator {

    @Override
    public String validateHost(String host, EndpointHolder holder) {
        return host;
    }

    @Override
    public int validatePort(int port, EndpointHolder holder) {
        return port;
    }

    @Override
    public Schema validateSchema(Schema schema, EndpointHolder holder) {
        return schema;
    }

    @Override
    public String validatePath(String path, EndpointHolder holder) {
        return path;
    }

}
