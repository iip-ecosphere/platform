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

import java.util.List;

import de.iip_ecosphere.platform.support.identities.IdentityToken;

/**
 * A descriptor that defines how platform authentication shall be applied to AAS through a given descriptor.
 * May be partially overridden.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DelegatingAuthenticationDescriptor implements AuthenticationDescriptor {
    
    private AuthenticationDescriptor desc;

    /**
     * Creates a delegating instance.
     * 
     * @param desc the instance to delegate to
     */
    public DelegatingAuthenticationDescriptor(AuthenticationDescriptor desc) {
        this.desc = desc;
    }

    @Override
    public IdentityToken getClientToken() {
        return desc.getClientToken();
    }

    @Override
    public List<IdentityTokenWithRole> getServerUsers() {
        return desc.getServerUsers();
    }
    
    @Override
    public void enableRbac() {
        enableRbac();
    }

    @Override
    public void addAccessRule(RbacRule rule) {
        desc.addAccessRule(null);
    }
    
    @Override
    public List<RbacRule> getAccessRules() {
        return desc.getAccessRules();
    }

    @Override
    public boolean requiresAnonymousAccess() {
        return desc.requiresAnonymousAccess();
    }
    
    @Override
    public OAuth2Setup getOAuth2Setup() {
        return desc.getOAuth2Setup();
    }
    
}
