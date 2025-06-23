
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

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

/**
 * An element that may receive RBAC rules.
 * 
 * @param <T> the actual element type
 * @author Holger Eichelberger, SSE
 */
public interface RbacReceiver<T> {

    /**
     * Creates an RBAC rule for the receiver and adds the role to {@code auth}.
     * 
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @param role the role to create the rule for (call ignored if <b>null</b>)
     * @param actions the permitted actions (call ignored if <b>null</b>, as array)
     * @return <b>this</b> for chaining
     */
    public T rbac(AuthenticationDescriptor auth, Role role, RbacAction... actions); 
    
    /**
     * Creates RBAC rules for the receiver and adds the roles to {@code auth}.
     * 
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @param roles the roles to create the rules for (call ignored if <b>null</b>)
     * @param actions the permitted actions (call ignored if <b>null</b>, as array)
     * @return <b>this</b> for chaining
     */
    public T rbac(AuthenticationDescriptor auth, Role[] roles, RbacAction... actions);

    /**
     * Creates RBAC rules for the receiver on all authenticated/anonymous roles and all known actions.
     * 
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @return <b>this</b> for chaining
     * @see #rbac(AuthenticationDescriptor, Role[], RbacAction...)
     */
    public default T rbacAll(AuthenticationDescriptor auth) {
        return rbac(auth, Role.all(), RbacAction.all());
    }

    /**
     * Creates RBAC rules for the receiver  on all authenticated roles and all known actions.
     * 
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @return <b>this</b> for chaining
     */
    public default T rbacAllAuthenticated(AuthenticationDescriptor auth) {
        return rbac(auth, Role.allAuthenticated(), RbacAction.all());
    }

    /**
     * Creates RBAC rules for the receiver  on all authenticated roles and all known actions.
     * 
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @return <b>this</b> for chaining
     */
    public default T rbacPlatform(AuthenticationDescriptor auth) {
        //rbac(auth, Role.of(DefaultRole.PLATFORM, DefaultRole.ADMIN), RbacAction.all());
        //rbac(auth, Role.allExcept(true, DefaultRole.PLATFORM, DefaultRole.ADMIN), RbacAction.READ);
        return rbac(auth, Role.allAuthenticated(), RbacAction.all()); // preliminary placeholder
    }

    /**
     * Creates RBAC rules for the receiver  on all authenticated roles and all known actions.
     * 
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @return <b>this</b> for chaining
     */
    public default T rbacDevice(AuthenticationDescriptor auth) {
        //rbac(auth, Role.of(DefaultRole.PLATFORM, DefaultRole.ADMIN, DefaultRole.DEVICE), RbacAction.all());
        //rbac(auth, Role.allExcept(true, DefaultRole.PLATFORM, DefaultRole.ADMIN, DefaultRole.DEVICE), 
        //   RbacAction.READ);
        return rbac(auth, Role.allAuthenticated(), RbacAction.all()); // preliminary placeholder
    }
    
    /**
     * Creates default RBAC rules based on the rules of the parent builder, i.e., the context this operation
     * is created within. The implementation decides whether/how this operation shall be implemented or whether
     * the respective rules are implicit.
     * 
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @return <b>this</b> for chaining
     */
    public T rbac(AuthenticationDescriptor auth);

}
