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

import java.io.IOException;

/**
 * Represents an HTTP message.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface HttpRequest<R extends HttpRequest<R>> {

    /**
     * Associates the entity with this request.
     * 
     * @param entity the entity to send
     * @return <b>this</b> for chaining
     * @throws IOException if the entity cannot be encoded
     */
    public R setEntity(String entity) throws IOException;
    
    /**
     * Overwrites the first header with the same name. The new header will be appended to the end of the list, if no 
     * header with the given name can be found.
     * 
     * @param name the name of the header
     * @return <b>this</b> for chaining
     * @param value the value of the header
     */
    public R setHeader(String name, String value);

}
