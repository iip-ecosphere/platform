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

import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.identities.IdentityToken;

/**
 * A descriptor that defines how platform authentication shall be applied to AAS. If no such
 * descriptor is present or no identity token is present, authentication is disabled.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface AuthenticationDescriptor {
    
    /**
     * Returns the client authentication token.
     * 
     * @return the client authentication token
     */
    public IdentityToken getClientToken();
    
    /**
     * Returns whether authentication is enabled on client.
     * 
     * @param desc the descriptor
     * @return {@code true} if authentication is enabled, {@code false} else
     */
    public static boolean isEnabledOnClient(AuthenticationDescriptor desc) {
        return null != desc && desc.getClientToken() != null;
    }

    /**
     * Returns whether authentication is enabled on server.
     * 
     * @param desc the descriptor
     * @return {@code true} if authentication is enabled, {@code false} else
     */
    public static boolean isEnabledOnServer(AuthenticationDescriptor desc) {
        return null != desc && (desc.getServerUsers() != null || desc.getOAuth2Setup() != null);
    }

    /**
     * Marks a role for AAS access. Register implementing classes with {@link RbacRoles}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Role {
        
        /**
         * Returns the name of the role.
         * 
         * @return the name
         */
        public String name();
        
        /**
         * Returns whether this role represents anonymous users.
         * 
         * @return {@code true} for anonymous, {@code false} else
         */
        public boolean anonymous();

        /**
         * Returns all authenticated, non-anonymous roles.
         * 
         * @return the authenticated, non-anonymous roles
         */
        public static Role[] allAuthenticated() {
            return RbacRoles.allAuthenticated();
        }

        /**
         * Returns all anonymous roles.
         * 
         * @return the anonymous roles
         */
        public static Role[] allAnonymous() {
            return RbacRoles.allAnonymous();
        }

        /**
         * Returns all roles.
         * 
         * @return the roles
         */
        public static Role[] all() {
            return RbacRoles.all();
        }

    }

    /**
     * Defines the default roles.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum DefaultRole implements Role {
        
        /**
         * User via UI.
         */
        USER(false),

        /**
         * Platform services (headless).
         */
        PLATFORM(false),
        
        /**
         * Device services, e.g., ECS-Runtime (headless).
         */
        DEVICE(false),
        
        /**
         * Administrators (headless, UI).
         */
        ADMIN(false),
        
        /**
         * Anonymous.
         */
        NONE(true);
        
        private boolean anonymous;

        /**
         * Creates a role instance.
         * 
         * @param anonymous {@code true} for anonymous, {@code false} else
         */
        private DefaultRole(boolean anonymous) {
            this.anonymous = anonymous;
        }

        @Override
        public boolean anonymous() {
            return anonymous;
        }

    }
    
    /**
     * Represents an identity token with attached role.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class IdentityTokenWithRole extends IdentityToken {
        
        private Role role;

        /**
         * Creates a token from a given token and a known role.
         * 
         * @param token the token
         * @param role the role
         */
        protected IdentityTokenWithRole(IdentityToken token, Role role) {
            super(token);
            this.role = role;
        }

        /**
         * Returns the role.
         * 
         * @return the role
         */
        public Role getRole() {
            return role;
        }
        
        @Override
        public String toString() {
            return super.toString() + " as " + role;
        }
        
    }
    
    /**
     * Returns the explicit server users and their tokens. This may be used for basic authentication and testing.
     * 
     * @return the users, may be <b>null</b> for none, indicating that none or a different authentication 
     *     mechanism shall be used
     */
    public List<IdentityTokenWithRole> getServerUsers();
    
    /**
     * Denotes the component type a RBAC rule shall be applied to.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum RbacAasComponent {

        AAS,
        SUBMODEL,
        SUBMODEL_ELEMENT
    }
    
    /**
     * Represents allowed RBAC actions.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum RbacAction {
        
        READ,
        CREATE,
        UPDATE,
        EXECUTE,
        DELETE;
        
        /**
         * Returns all actions.
         * 
         * @return all actions
         */
        public static RbacAction[] all() {
            return values();
        }
        
    }
    
    /**
     * Represents an abstracted RBAC rule.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class RbacRule {
        
        public static final String PATH_SEPARATOR = ".";
        
        private RbacAasComponent component;
        private List<RbacAction> actions;
        private Role role;
        private String element;
        private String path;

        /**
         * Creates a rule instance.
         * 
         * @param component the AAS component this rule shall be applied to
         * @param role the user role the rule applies to
         * @param element the element within {@code component}
         * @param path the optional path as qualification with element, separated by {@link #PATH_SEPARATOR}
         * @param actions the permitted actions
         */
        public RbacRule(RbacAasComponent component, Role role, String element, String path, RbacAction... actions) {
            this.component = component;
            this.role = role;
            this.element = element;
            this.path = path;
            this.actions = Collections.unmodifiableList(CollectionUtils.toList(actions));
        }
        
        /**
         * Returns the kind of AAS component.
         * 
         * @return the component
         */
        public RbacAasComponent getComponent() {
            return component;
        }

        /**
         * Returns the permitted actions.
         * 
         * @return the actions, may be empty, may be unmodifiable
         */
        public List<RbacAction> getActions() {
            return actions;
        }

        /**
         * Returns the user role this rule applies to.
         * 
         * @return the role
         */
        public Role getRole() {
            return role;
        }

        /**
         * Returns the element within {@link #getComponent()} this rule applies to.
         * 
         * @return the element, may be ignored if empty or <b>null</b>
         */
        public String getElement() {
            return element;
        }

        /**
         * Returns the optional path as qualification of {@link #getElement()} this rule applies to.
         * 
         * @return the path, may be empty or <b>null</b>
         */
        public String getPath() {
            return path;
        }
        
        @Override
        public String toString() {
            return new StringBuilder("RbacRule{")
                .append("component='").append(component)
                .append("', role='").append(role)
                .append("', actions='").append(actions)
                .append("', element='").append(element)
                .append("', path='").append(path)
                .append("'}").toString();
        }

    }
    
    /**
     * Enables RBAC although there may no rules be specified through {@link #addAccessRule(RbacRule)}.
     */
    public void enableRbac();

    /**
     * Adds an access rule. This method is intended that platform sub-components specify the access rules when 
     * creating AAS.
     * 
     * @param rule the rule, may be <b>null</b>, ignored then
     */
    public void addAccessRule(RbacRule rule);
    
    /**
     * Returns the rules added by {@link #addAccessRule(RbacRule)}.
     * 
     * @return the rules, may be <b>null</b> indicating that no RBAC shall be applied
     */
    public List<RbacRule> getAccessRules();

    /**
     * Returns whether anonymous access is required.
     * 
     * @return {@code true} for anonymous access, {@code false} else
     */
    public default boolean requiresAnonymousAccess() {
        boolean result = false;
        List<RbacRule> rules = getAccessRules();
        if (null != rules) {
            result = rules
                .stream()
                .anyMatch(u -> DefaultRole.NONE == u.getRole());        
        }
        return result;
    }
    
    /**
     * Adds an RBAC rule to {@code desc}.
     * 
     * @param desc the descriptor, may be <b>null</b> then the rule is ignored
     * @param rule the rule
     */
    public static void addAccessRule(AuthenticationDescriptor desc, RbacRule rule) {
        if (null != desc && null != rule) {
            desc.addAccessRule(rule);
        }
    }

    /**
     * Helper function to create an AAS RBAC rule.
     * 
     * @param <T> the caller type
     * @param caller the caller
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @param role the role
     * @param idShort the AAS idShort
     * @param actions the permitted actions
     * @return the caller
     */
    public static <T> T aasRbac(T caller, AuthenticationDescriptor auth, Role role, String idShort, 
        RbacAction... actions) {
        if (null != auth) {
            auth.addAccessRule(new RbacRule(RbacAasComponent.AAS, role, idShort, null, actions));
        }
        return caller;
    }

    /**
     * Helper function to create a submodel RBAC rule.
     * 
     * @param <T> the caller type
     * @param caller the caller
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @param role the role
     * @param idShort the submodel idShort
     * @param actions the permitted actions
     * @return the caller
     */
    public static <T> T submodelRbac(T caller, AuthenticationDescriptor auth, Role role, String idShort, 
        RbacAction... actions) {
        if (null != auth) {
            auth.addAccessRule(new RbacRule(RbacAasComponent.SUBMODEL, role, idShort, "*", actions));
        }
        return caller;
    }

    /**
     * Helper function to create a submodel element RBAC rule.
     * 
     * @param <T> the caller type
     * @param caller the caller
     * @param auth the authentication descriptor, may be <b>null</b>, ignored then
     * @param role the role
     * @param path the path to the submodel element, separated by {@link RbacRule#PATH_SEPARATOR} starting with the 
     *     hosting submodel
     * @param actions the permitted actions
     * @return the caller
     */
    public static <T> T elementRbac(T caller, AuthenticationDescriptor auth, Role role, String path, 
        RbacAction... actions) {
        if (null != auth && path != null && path.length() > 0) {
            int pos = path.indexOf(RbacRule.PATH_SEPARATOR);
            if (pos > 0) {
                String smId = path.substring(0, pos);
                String pth = path.substring(pos + 1);
                auth.addAccessRule(new RbacRule(RbacAasComponent.SUBMODEL_ELEMENT, role, smId, pth, actions));
            } 
        }
        return caller;
    }

    /**
     * Returns whether RBAC is enabled.
     * 
     * @param desc the descriptor
     * @return {@code true} if RBAC is enabled, {@code false} else
     */
    public static boolean definesRbac(AuthenticationDescriptor desc) {
        return isEnabledOnServer(desc) && desc.getAccessRules() != null;
    }

    /**
     * Represents an OAuth2 JWT setup.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface OAuth2Setup {

        /**
         * Returns the issuer URI.
         * 
         * @return the issuer URI
         */
        public String getIssuerUri();

        /**
         * Returns the JWK set URI.
         * 
         * @return the JWK set URI
         */
        public String getJwkSetUri();

        /**
         * Returns the required audience.
         * 
         * @return the audience
         */
        public String getRequiredAud();

    }

    /**
     * A default implementation of {@link OAuth2Setup}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class DefaultOAuth2Setup implements OAuth2Setup {

        private String issuerUri;
        private String jwkSetUri;
        private String requiredAud;
        
        /**
         * Creates a setup instance.
         * 
         * @param issuerUri the issue URI
         * @param jwkSetUri the JWK set URI
         * @param requiredAud the required audience
         */
        public DefaultOAuth2Setup(String issuerUri, String jwkSetUri, String requiredAud) {
            this.issuerUri = issuerUri;
            this.jwkSetUri = jwkSetUri;
            this.requiredAud = requiredAud;
        }
        
        @Override
        public String getIssuerUri() {
            return issuerUri;
        }

        @Override
        public String getJwkSetUri() {
            return jwkSetUri;
        }

        @Override
        public String getRequiredAud() {
            return requiredAud;
        }
        
    }

    /**
     * Returns the OAuth2 setup.
     * 
     * @return the setup, may be <b>null</b> for none
     */
    public OAuth2Setup getOAuth2Setup();
    
    /**
     * Consumes an HTTP header name/value pair.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface HeaderValueConsumer {

        /**
         * Consumes an HTTP header name/value.
         * 
         * @param name the name
         * @param value the value
         */
        public void consume(String name, String value);
        
    }

    /**
     * Turns authentication to an HTTP header string. Authentication data is taken from {@code aDesc} if any.
     * 
     * @param aDesc the authentication descriptor
     * @param prependHeaderFieldName if the header field name shall be prepended/included in the result
     * @return the header string, may be <b>null</b> for none
     */
    public static String authenticate(AuthenticationDescriptor aDesc, boolean prependHeaderFieldName) {
        StringBuffer buf = new StringBuffer();
        authenticate((n, v) -> {
            if (prependHeaderFieldName) {
                buf.append(n);
                buf.append(":");
            }
            buf.append(v);
        }, aDesc);
        return buf.isEmpty() ? null : buf.toString(); 
    }

    /**
     * Adds authentication to an HTTP header via the given {@code consumer}. Authentication data is taken from 
     * {@code aDesc} if any.
     * 
     * @param consumer the header value consumer
     * @param aDesc the authentication descriptor, may be <b>null</b> for none
     */
    public static void authenticate(HeaderValueConsumer consumer, AuthenticationDescriptor aDesc) {
        if (aDesc != null) {
            IdentityToken clientToken = aDesc.getClientToken();
            
            if (clientToken != null) {
                switch (clientToken.getType()) {
                case USERNAME:
                    // TODO consider clientToken.getTokenEncryptionAlgorithm()
                    String credentials = clientToken.getUserName() + ":" + clientToken.getTokenDataAsString();
                    String headerValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
                    consumer.consume("Authorization", headerValue);
                    break;
                    // TODO bearer...
                case ANONYMOUS:
                    break;
                default:
                    LoggerFactory.getLogger(AuthenticationDescriptor.class).error("Authentication token type {} not "
                        + "supported for setting up HTTP authentication. Staying unauthenticated.", 
                        clientToken.getType());
                    break;
                }
            }
        }    
    }
    
}
