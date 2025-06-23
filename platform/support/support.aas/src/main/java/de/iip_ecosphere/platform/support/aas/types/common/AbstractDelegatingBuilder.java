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

package de.iip_ecosphere.platform.support.aas.types.common;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;
import de.iip_ecosphere.platform.support.aas.RbacReceiver;

/**
 * An abstract delegating builder introducting implicit RBAC settings so that RBAC can be applied to generated 
 * submodel templates/implementations. Assumes that at least the top-level builder calls 
 * {@link #setAuthenticationDescriptor(AuthenticationDescriptor) authentication descriptor} in it's RBAC methods.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractDelegatingBuilder {
    
    private AbstractDelegatingBuilder parent;
    private AuthenticationDescriptor authDesc;
    private Role[] nextRoles;
    private RbacAction[] nextActions;

    /**
     * Creates a delegating builder without parent.
     */
    protected AbstractDelegatingBuilder() {
    }

    /**
     * Creates a delegating builder with parent.
     * 
     * @param parent the parent
     */
    protected AbstractDelegatingBuilder(AbstractDelegatingBuilder parent) {
        this.parent = parent;
    }

    /**
     * Explicitly sets the authentication descriptor.
     * 
     * @param authDesc the authentication descriptor, may be <b>null</b> for none
     * @return
     */
    public AbstractDelegatingBuilder setAuthenticationDescriptor(AuthenticationDescriptor authDesc) {
        this.authDesc = authDesc;
        return this;
    }

    /**
     * Returns the authentication descriptor, the own or a parent one.
     * 
     * @return the authentication descriptor, may be <b>null</b> for none
     */
    public AuthenticationDescriptor getAuthenticationDescriptor() {
        AuthenticationDescriptor result = authDesc;
        if (null == result && parent != null) {
            result = parent.getAuthenticationDescriptor();
        }
        return result;
    }

    /**
     * Defines the settings for next RBAC rule creations.
     * 
     * @param role the role (call ignored if <b>null</b>)
     * @param actions the actions (call ignored if <b>null</b>, as array)
     * @return <b>this</b> for chaining
     */
    public AbstractDelegatingBuilder nextRbac(Role role, RbacAction... actions) {
        return nextRbac(new Role[] {role}, actions);
    }

    /**
     * Defines the settings for next RBAC rule creations.
     * 
     * @param roles the roles (call ignored if <b>null</b>)
     * @param actions the actions (call ignored if <b>null</b>, as array)
     * @return <b>this</b> for chaining
     */
    public AbstractDelegatingBuilder nextRbac(Role[] roles, RbacAction... actions) {
        this.nextRoles = roles;
        this.nextActions = actions;
        return this;
    }
    
    /**
     * Applies the last RBAC settings to {@code element}, only if there is an 
     * {@link #setAuthenticationDescriptor(AuthenticationDescriptor) authentication descriptor} and specific RBAC 
     * settings.
     * 
     * @param <T> the element type
     * @param element the element
     * @return {@code element}
     * @see #nextRbac(Role, RbacAction...)
     * @see #nextRbac(Role[], RbacAction...)
     */
    protected <T> T rbac(RbacReceiver<T> element) {
        return element.rbac(getAuthenticationDescriptor(), nextRoles, nextActions);
    }

}
