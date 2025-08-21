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

package de.iip_ecosphere.platform.support.http;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents an HTTP client.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface HttpClient extends Closeable {
    
    /**
     * Executes the request.
     * 
     * @param request the request
     * @return the response
     * @throws IOException if the request fails
     */
    public HttpResponse execute(HttpRequest<?> request) throws IOException;

}
