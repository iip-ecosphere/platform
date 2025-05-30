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

package de.iip_ecosphere.platform.support.aas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;

/**
 * Implements a default authentication descriptor based on the identity store. Server users may be supplied through
 * identity tokens with keys in the form "aas-ROLE-i" whereby aas is {@link #DEFAULT_ID}, ROLE is the 
 * {@link DefaultRole} in all lower/upper caps and is is the 1-based index of the consecutively numbered token.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IdentityStoreAuthenticationDescriptor implements AuthenticationDescriptor {

    /**
     * The default identity name for AAS authentication ({@value}).
     */
    public static final String DEFAULT_ID = "aas";
    private String clientId;
    private List<RbacRule> rbacRules;

    /**
     * Creates an identity storage authentication descriptor with {@link #DEFAULT_ID}.
     */
    public IdentityStoreAuthenticationDescriptor() {
        this(DEFAULT_ID);
    }
    
    /**
     * Creates an identity storage authentication descriptor with given id.
     * 
     * @param clientId the client id from {@link IdentityStore} to be used
     */
    public IdentityStoreAuthenticationDescriptor(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public IdentityToken getClientToken() {
        return IdentityStore.getInstance().getToken(clientId);
    }

    @Override
    public List<IdentityTokenWithRole> getServerUsers() {
        List<IdentityTokenWithRole> result = new ArrayList<>(); // enable, even with no users
        IdentityStore store = IdentityStore.getInstance();
        for (DefaultRole role : DefaultRole.values()) {
            for (IdentityToken tok : store.enumerateTokens(clientId + "-" + role.name() + "-")) {
                result.add(new IdentityTokenWithRole(tok, role));
            }
        }
        return result;
    }

    @Override
    public void addAccessRule(RbacRule rule) {
        if (null != rule) {
            enableRbac(); // ensure lazy
            rbacRules.add(rule);
        }        
    }

    @Override
    public List<RbacRule> getAccessRules() {
        return null == rbacRules ? null : Collections.unmodifiableList(rbacRules);
    }

    @Override
    public void enableRbac() {
        if (null == rbacRules) {
            rbacRules = new ArrayList<>();
        }
    }

    @Override
    public OAuth2Setup getOAuth2Setup() {
        return null; // none so far
    }

}
