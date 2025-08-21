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
 * Represents a HTTP response.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface HttpResponse extends Closeable {
    
    /**
     * Returns the response entity as string.
     * 
     * @return the response entity as string
     * @throws IOException if accessing the response fails
     */
    public String getEntityAsString() throws IOException;
    
    /**
     * Returns the HTTP status code for this response.
     * 
     * @return the status code
     */
    public int getStatusCode();

    /**
     * Returns the HTTP reason phase for the {@link #getStatusCode()}.
     * 
     * @return the reason phrase
     */
    public String getReasonPhrase();


}
