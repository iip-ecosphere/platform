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

package test.de.iip_ecosphere.platform.support.http;

import de.iip_ecosphere.platform.support.http.HttpClient;
import de.iip_ecosphere.platform.support.http.HttpPost;

/**
 * Implements an empty HTTP interface for simple testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestHttp extends de.iip_ecosphere.platform.support.http.Http {

    @Override
    public HttpPost createPost(String uri) {
        return null;
    }

    @Override
    public HttpClient createClient() {
        return null;
    }

    @Override
    public HttpClient createPooledClient() {
        return null;
    }

}
