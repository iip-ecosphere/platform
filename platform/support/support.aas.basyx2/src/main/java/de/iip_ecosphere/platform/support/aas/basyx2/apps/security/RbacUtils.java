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

package de.iip_ecosphere.platform.support.aas.basyx2.apps.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.digitaltwin.basyx.aasrepository.feature.authorization.AasTargetInformation;
import org.eclipse.digitaltwin.basyx.aasrepository.feature.authorization.rbac.AasTargetPermissionVerifier;
import org.eclipse.digitaltwin.basyx.authorization.rbac.Action;
import org.eclipse.digitaltwin.basyx.authorization.rbac.RbacPermissionResolver;
import org.eclipse.digitaltwin.basyx.authorization.rbac.RoleProvider;
import org.eclipse.digitaltwin.basyx.authorization.rbac.SimpleRbacPermissionResolver;
import org.eclipse.digitaltwin.basyx.authorization.rbac.TargetInformation;
import org.eclipse.digitaltwin.basyx.authorization.rbac.TargetPermissionVerifier;
import org.eclipse.digitaltwin.basyx.authorization.rules.rbac.backend.inmemory.InMemoryAuthorizationRbacStorage;
import org.eclipse.digitaltwin.basyx.submodelrepository.feature.authorization.SubmodelTargetInformation;
import org.eclipse.digitaltwin.basyx.submodelrepository.feature.authorization.rbac.SubmodelTargetPermissionVerifier;

import com.google.common.base.Supplier;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAasComponent;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacRule;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.Role;

/**
 * RBAC utility (translation) functions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RbacUtils {

    private static final Map<RbacAction, Action> ACTION_MAPPING = new HashMap<>();

    static {
        ACTION_MAPPING.put(RbacAction.ALL, Action.ALL);
        ACTION_MAPPING.put(RbacAction.CREATE, Action.CREATE);
        ACTION_MAPPING.put(RbacAction.DELETE, Action.DELETE);
        ACTION_MAPPING.put(RbacAction.EXECUTE, Action.EXECUTE);
        ACTION_MAPPING.put(RbacAction.READ, Action.READ);
        ACTION_MAPPING.put(RbacAction.UPDATE, Action.UPDATE);
    }

    /**
     * Creates a permission resolver from an authentication descriptor.
     * 
     * @param <T> the type of target information
     * @param authDesc the authentication descriptor (must comply with {@link AuthenticationDescriptor#enableRbac()}).
     * @param component the AAS component to create the resolver for (as filter)
     * @param infoCreator the target information object creator
     * @param verifierCreator the target permission verifier creator
     * @return the created permission resolver
     */
    public static <T extends TargetInformation> RbacPermissionResolver<T> createPermissionResolver(
        AuthenticationDescriptor authDesc, RbacAasComponent component, Function<RbacRule, T> infoCreator, 
        Supplier<TargetPermissionVerifier<T>> verifierCreator) {
        RoleProvider roleProvider = new RoleProvider() {

            @Override
            public List<String> getRoles() {
                List<String> result = new ArrayList<>();
                for (Role r : AuthenticationDescriptor.DefaultRole.values()) {
                    result.add(r.name());
                }
                return result;
            }
            
        };
        InMemoryAuthorizationRbacStorage rbacStorage = new InMemoryAuthorizationRbacStorage(new HashMap<>());
        authDesc.getAccessRules().stream().filter(r -> r.getComponent() == component).forEach(r -> {
            List<Action> actions = r.getActions().stream()
                .map(a -> ACTION_MAPPING.get(a))
                .filter(a -> a != null)
                .collect(Collectors.toList());
            rbacStorage.addRule(new org.eclipse.digitaltwin.basyx.authorization.rbac.RbacRule(r.getRole().name(), 
                actions, infoCreator.apply(r)));
        });
        return new SimpleRbacPermissionResolver<>(rbacStorage, roleProvider, verifierCreator.get());
    }
    
    /**
     * Creates a submodel permission resolver from an authentication descriptor.
     * 
     * @param authDesc the authentication descriptor (must comply with {@link AuthenticationDescriptor#enableRbac()}).
     * @return the created permission resolver
     */
    public static RbacPermissionResolver<SubmodelTargetInformation> createSubmodelPermissionResolver(
        AuthenticationDescriptor authDesc) {
        return createPermissionResolver(authDesc, RbacAasComponent.SUBMODEL, 
            r -> new SubmodelTargetInformation(CollectionUtils.toList(r.getElement()), toPaths(r)), 
            () -> new SubmodelTargetPermissionVerifier());
    }
    
    /**
     * Creates an AAS permission resolver from an authentication descriptor.
     * 
     * @param authDesc the authentication descriptor (must comply with {@link AuthenticationDescriptor#enableRbac()}).
     * @return the created permission resolver
     */
    public static RbacPermissionResolver<AasTargetInformation> createAasPermissionResolver(
        AuthenticationDescriptor authDesc) {
        return createPermissionResolver(authDesc, RbacAasComponent.AAS, 
            r -> new AasTargetInformation(CollectionUtils.toList(r.getElement())), 
            () -> new AasTargetPermissionVerifier());
    }
    
    /**
     * Turns a generic rule path to a BaSyx rule path.
     * 
     * @param rule the rule, paths may be <b>null</b>
     * @return the paths
     */
    private static final List<String> toPaths(RbacRule rule) {
        List<String> result = null;
        String path = rule.getPath();
        if (null != path) {
            path = path.replaceAll(RbacRule.PATH_SEPARATOR, ".");
            result = CollectionUtils.toList(path);
        }
        return result;
    }
    
}
