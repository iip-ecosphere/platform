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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.digitaltwin.basyx.aasrepository.feature.authorization.AasTargetInformation;
import org.eclipse.digitaltwin.basyx.aasrepository.feature.authorization.rbac.AasTargetPermissionVerifier;
import org.eclipse.digitaltwin.basyx.authorization.rbac.Action;
import org.eclipse.digitaltwin.basyx.authorization.rbac.RbacPermissionResolver;
import org.eclipse.digitaltwin.basyx.authorization.rbac.RbacRuleKeyGenerator;
import org.eclipse.digitaltwin.basyx.authorization.rbac.RoleProvider;
import org.eclipse.digitaltwin.basyx.authorization.rbac.SimpleRbacPermissionResolver;
import org.eclipse.digitaltwin.basyx.authorization.rbac.TargetInformation;
import org.eclipse.digitaltwin.basyx.authorization.rbac.TargetPermissionVerifier;
import org.eclipse.digitaltwin.basyx.authorization.rules.rbac.backend.inmemory.InMemoryAuthorizationRbacStorage;
import org.eclipse.digitaltwin.basyx.submodelrepository.feature.authorization.SubmodelTargetInformation;
import org.eclipse.digitaltwin.basyx.submodelrepository.feature.authorization.rbac.SubmodelTargetPermissionVerifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.google.common.base.Supplier;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAasComponent;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacRule;

/**
 * RBAC utility (translation) functions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RbacUtils {

    private static final Map<RbacAction, Action> ACTION_MAPPING = new HashMap<>();

    static {
        //ACTION_MAPPING.put(RbacAction.ALL, Action.ALL);
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
     * @param infoHandler the target information object handler
     * @param verifierCreator the target permission verifier creator
     * @param components the AAS components to create the resolver for (as filter)
     * @return the created permission resolver
     */
    public static <T extends TargetInformation> RbacPermissionResolver<T> createPermissionResolver(
        AuthenticationDescriptor authDesc, TargetInfoHandler<T> infoHandler, 
        Supplier<TargetPermissionVerifier<T>> verifierCreator, RbacAasComponent... components) {
        RoleProvider roleProvider = new RoleProvider() {

            @Override
            public List<String> getRoles() {
                List<String> result = new ArrayList<>();
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (null == principal) { // shall not happen
                    Stream.of(AuthenticationDescriptor.Role.allAnonymous()).forEach(a -> result.add(a.name()));
                } else if (principal instanceof User) {
                    User user = (User) principal;
                    if (null != user.getAuthorities()) {
                        user.getAuthorities().forEach(a -> result.add(a.getAuthority()));
                    }
                } else if (principal instanceof String) { // see SecuritySetup
                    result.add(principal.toString());
                }
                return result;
            }
            
        };
        InMemoryAuthorizationRbacStorage rbacStorage = new InMemoryAuthorizationRbacStorage(new HashMap<>());
        PreliminaryRbacRules<T> tmpRules = new PreliminaryRbacRules<>();
        authDesc.getAccessRules().stream().filter(r -> containsByRef(components, r.getComponent())).forEach(r -> {
            List<Action> actions = r.getActions().stream()
                .map(a -> ACTION_MAPPING.get(a))
                .filter(a -> a != null)
                .collect(Collectors.toList());
            tmpRules.addRule(r.getRole().name(), actions, infoHandler, r);
        });
        tmpRules.addAll(rbacStorage);
        return new SimpleRbacPermissionResolver<>(rbacStorage, roleProvider, verifierCreator.get());
    }

    /**
     * Returns whether {@code element} is contained by reference equality in {@code array}.
     * 
     * @param <T> the element type
     * @param array the array
     * @param element the element to look for
     * @return whether element is contained or not
     */
    private static <T> boolean containsByRef(T[] array, T element) {
        for (T a: array) {
            if (a == element) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * A preliminary RBAC rule that potentially needs to be joined with further, so far unknown rules.
     *
     * @param <T> the target information type
     * @author Holger Eichelberger, SSE
     */
    private static class PreliminaryRbacRule<T extends TargetInformation> {
        
        private String role;
        private Action action;
        private TargetInfoHandler<T> infoHandler;
        private T info;
        
        /**
         * Creates a rule.
         * 
         * @param role the role
         * @param action the action (singled out)
         * @param infoHandler the information object handler
         * @param orig the original RBAC rule to store/join
         */
        public PreliminaryRbacRule(String role, Action action, TargetInfoHandler<T> infoHandler, RbacRule orig) {
            this.role = role;
            this.action = action;
            this.infoHandler = infoHandler;
            this.info = infoHandler.create(orig);
        }
        
        /**
         * Return the RBAC storage key.
         * 
         * @return the key
         */
        public String getKey() {
            return RbacRuleKeyGenerator.generateKey(role, action.toString(), infoHandler.getTypeName());
        }
        
        /**
         * Join {@code rule} with this rule.
         * 
         * @param rule the rule to join
         */
        public void join(PreliminaryRbacRule<T> rule) {
            this.info = infoHandler.join(info, rule.info);
        }
        
        /**
         * Create a BaSyx RBAC rule from the collected information.
         * 
         * @return the BaSyx RBAC rule
         */
        public org.eclipse.digitaltwin.basyx.authorization.rbac.RbacRule toRbacRule() {
            return new org.eclipse.digitaltwin.basyx.authorization.rbac.RbacRule(role, List.of(action), info);
        }
        
    }

    /**
     * A preliminary set of RBAC rules that potentially need to be joined with further, so far unknown rules.
     *
     * @param <T> the target information type
     * @author Holger Eichelberger, SSE
     */
    private static class PreliminaryRbacRules<T extends TargetInformation> {
        
        private Map<String, PreliminaryRbacRule<T>> rules = new HashMap<>();
        
        /**
         * Add a rule to this rule set.
         * 
         * @param role the role
         * @param actions the actions of the rule (to be splitted)
         * @param infoHandler the information object handler
         * @param orig the original RBAC rule to store/join
         */
        private void addRule(String role, List<Action> actions, TargetInfoHandler<T> infoHandler, RbacRule orig) {
            for (Action a: actions) {
                PreliminaryRbacRule<T> rule = new PreliminaryRbacRule<>(role, a, infoHandler, orig);
                String key = rule.getKey();
                PreliminaryRbacRule<T> known = rules.get(key);
                if (null == known) {
                    rules.put(key, rule);
                } else {
                    known.join(rule);
                }
            }
        }
        
        /**
         * Adds all collected rules to {@code rbacStorage}.
         * 
         * @param rbacStorage the target storage to modify
         */
        private void addAll(InMemoryAuthorizationRbacStorage rbacStorage) {
            for (PreliminaryRbacRule<T> r : rules.values()) {
                rbacStorage.addRule(r.toRbacRule());
            }
        }
        
    }
    
    /**
     * Creates a submodel permission resolver from an authentication descriptor.
     * 
     * @param authDesc the authentication descriptor (must comply with {@link AuthenticationDescriptor#enableRbac()}).
     * @return the created permission resolver
     */
    public static RbacPermissionResolver<SubmodelTargetInformation> createSubmodelPermissionResolver(
        AuthenticationDescriptor authDesc) {
        return createPermissionResolver(authDesc, SubmodelInfoCreator.INSTANCE, 
            () -> new SubmodelTargetPermissionVerifier(), RbacAasComponent.SUBMODEL, RbacAasComponent.SUBMODEL_ELEMENT);
    }
    
    /**
     * Creates an AAS permission resolver from an authentication descriptor.
     * 
     * @param authDesc the authentication descriptor (must comply with {@link AuthenticationDescriptor#enableRbac()}).
     * @return the created permission resolver
     */
    public static RbacPermissionResolver<AasTargetInformation> createAasPermissionResolver(
        AuthenticationDescriptor authDesc) {
        return createPermissionResolver(authDesc, AasInfoHandler.INSTANCE, 
            () -> new AasTargetPermissionVerifier(), RbacAasComponent.AAS);
    }
    
}
