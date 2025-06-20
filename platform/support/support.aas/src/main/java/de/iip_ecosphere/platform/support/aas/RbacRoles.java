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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.DefaultRole;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

/**
 * Manages roles.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RbacRoles {

    private static List<Role> roles = new ArrayList<>();
    
    static {
        registerRole(DefaultRole.class);
    }

    /**
     * Processes fields of a (role) class.
     * 
     * @param cls the class to process
     * @param operation the operation to apply to the enum constants of {@code cls}
     */
    private static void processFields(Class<? extends Role> cls, Consumer<Role> operation) {
        for (Field f : cls.getDeclaredFields()) {
            if (f.isEnumConstant()) {
                try {
                    operation.accept((Role) f.get(null));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    LoggerFactory.getLogger(RbacRoles.class).warn("Cannot process enum constant {}: {}", 
                        f.getName(), e.getMessage());
                }
            }
        }
    }
    
    /**
     * Registers a role class.
     * 
     * @param cls the role class
     */
    public static void registerRole(Class<? extends Role> cls) {
        processFields(cls, r -> {
            if (!roles.contains(r)) {
                roles.add(r);
            }
        });
    }

    /**
     * Unregisters a role class.
     * 
     * @param cls the role class
     */
    public static void unregisterRole(Class<? extends Role> cls) {
        processFields(cls, r -> roles.remove(r));
    }
    
    /**
     * Returns all roles.
     * 
     * @return all roles
     */
    public static Role[] all() {
        return roles.stream().toArray(Role[]::new);
    }

    /**
     * Returns all authenticated, non-anonymous roles.
     * 
     * @return the authenticated, non-anonymous roles
     */
    public static Role[] allAuthenticated() {
        return roles.stream().filter(r -> !r.anonymous()).toArray(Role[]::new);
    }

    /**
     * Returns all anonymous roles.
     * 
     * @return the anonymous roles
     */
    public static Role[] allAnonymous() {
        return roles.stream().filter(r -> r.anonymous()).toArray(Role[]::new);
    }
    
    /**
     * Returns whether {@code roles} contains {@code role}.
     * 
     * @param roles the roles to screen
     * @param role the role to look for
     * @return {@code true} if contained, {@code false} else
     */
    public static boolean contains(Role[] roles, Role role) {
        return Stream.of(roles).anyMatch(r -> r == role);
    }
    
    /**
     * Creates RBAC rules for {@code target} under creation and adds the roles to {@code auth}.
     * 
     * @param target the target object
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @param roles the roles to create the rules for
     * @param actions the permitted actions
     * @return {@code target}
     */
    public static <T extends RbacReceiver<T>> T rbac(T target, AuthenticationDescriptor auth, Role[] roles, 
        RbacAction... actions) {
        for (Role r: roles) {            
            target.rbac(auth, r, actions);
        }
        return target;
    }
    

}
