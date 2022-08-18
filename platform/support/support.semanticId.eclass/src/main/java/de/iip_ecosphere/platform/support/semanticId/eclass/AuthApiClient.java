/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.semanticId.eclass;

import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.semanticId.eclass.handler.ApiClient;
import de.iip_ecosphere.platform.support.semanticId.eclass.handler.Pair;
import de.iip_ecosphere.platform.support.semanticId.eclass.handler.auth.Authentication;
import de.iip_ecosphere.platform.support.semanticId.eclass.handler.auth.HttpBasicAuth;

/**
 * Implements a REST client that can work with {@link IdentityToken}.
 * 
 * @author Holger Eichelberger, SSE
 *
 */
public class AuthApiClient extends ApiClient {

    private Authentication auth;

    /**
     * Sets the identity token.
     * 
     * @param token the token
     */
    public void setIdentityToken(IdentityToken token) {
        switch (token.getType()) {
        case USERNAME:
            HttpBasicAuth basicAuth = new HttpBasicAuth();
            basicAuth.setUsername(token.getUserName());
            basicAuth.setPassword(token.getTokenDataAsString());
            auth = basicAuth;
            break;
        default:
            LoggerFactory.getLogger(AuthApiClient.class).error("Cannot handle: {}. Disabling authentication", 
                token.getType());
            break;
        }
    }

    @Override
    public void updateParamsForAuth(String[] authNames, List<Pair> queryParams, Map<String, String> headerParams) {
        if (null != auth) {
            auth.applyToParams(queryParams, headerParams);
        }
    }

}    
