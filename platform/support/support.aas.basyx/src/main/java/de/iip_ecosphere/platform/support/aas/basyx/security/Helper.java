package de.iip_ecosphere.platform.support.aas.basyx.security;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.servlet.Filter;

import org.eclipse.basyx.aas.aggregator.api.IAASAggregator;
import org.eclipse.basyx.extensions.aas.aggregator.authorization.internal.AuthorizedAASAggregator;
import org.eclipse.basyx.extensions.aas.aggregator.authorization.internal.SimpleRbacAASAggregatorAuthorizer;
import org.eclipse.basyx.extensions.shared.authorization.internal.AuthenticationContextProvider;
import org.eclipse.basyx.extensions.shared.authorization.internal.BaSyxObjectTargetInformation;
import org.eclipse.basyx.extensions.shared.authorization.internal.PredefinedSetRbacRuleChecker;
import org.eclipse.basyx.extensions.shared.authorization.internal.RbacRule;
import org.eclipse.basyx.extensions.shared.authorization.internal.RbacRuleSet;
import org.eclipse.basyx.extensions.shared.authorization.internal.TargetInformation;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.JwtBearerTokenAuthenticationConfiguration;
import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.OAuth2Setup;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAction;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacAasComponent;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.aas.SetupSpec.ComponentSetup;

/**
 * Server/security helper functions.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Helper {

    private static final Map<RbacAction, String> ACTION_MAPPING = new HashMap<>();

    static { // preliminary
//        ACTION_MAPPING.put(RbacAction.ALL, RbacAction.ALL.toString());
        ACTION_MAPPING.put(RbacAction.CREATE, RbacAction.CREATE.toString());
        ACTION_MAPPING.put(RbacAction.DELETE, RbacAction.DELETE.toString());
        ACTION_MAPPING.put(RbacAction.EXECUTE, RbacAction.EXECUTE.toString());
        ACTION_MAPPING.put(RbacAction.READ, RbacAction.READ.toString());
        ACTION_MAPPING.put(RbacAction.UPDATE, RbacAction.UPDATE.toString());
    }

    /**
     * Returns the docBasePath from {@code context} using reflection.
     * 
     * @param context the BaSyx context
     * @param logger the logger to use
     * @return the doc base path or <b>null</b> if there is none
     */
    public static String getDocBasePath(BaSyxContext context, Logger logger) {
        String docBasePath = null;
        try {
            Field fld = context.getClass().getDeclaredField("docBasePath");
            fld.setAccessible(true);
            docBasePath = (String) fld.get(context);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Cannot find/access field docBasePath in BaSyxContext: " + e.getMessage());
        }
        return docBasePath;
    }
    
    /**
     * Configures security based on the {@code component} setup in {@code spec}. If filter chains are created,
     * they are passed on to {@code filterChainConsumer}.
     * 
     * @param context the BaSyx context
     * @param spec the setup specification
     * @param component the component being configured
     * @param filterChainConsumer the filter chain consumer
     * @return the BaSyX JWT Bearer Token Authentication configuration, if not already processed (may then be empty)
     */
    public static Optional<JwtBearerTokenAuthenticationConfiguration> configureSecurity(BaSyxContext context, 
        SetupSpec spec, AasComponent component, Consumer<FilterChainProxy> filterChainConsumer) {
        ComponentSetup setup = spec.getSetup(component);
        Optional<JwtBearerTokenAuthenticationConfiguration> jwtCfg = Optional.empty();
        AuthenticationDescriptor auth = setup.getAuthentication();
        if (AuthenticationDescriptor.isEnabledOnServer(auth) && (AasComponent.AAS_REPOSITORY == component)) {
            if (auth.getServerUsers() != null) {
                AuthenticationManager authMgr = new AuthenticationDescriptorBasedAuthenticationManager(auth);
                final List<Filter> sortedListOfFilters = new ArrayList<>();
                sortedListOfFilters.add(new BasicAuthenticationFilter(authMgr));
                String uriExceptionRegEx = null;
                ComponentSetup regSetup = spec.getSetup(AasComponent.AAS_REGISTRY);
                // all-in-one setup on the same server but with different endpoint
                if (regSetup.getEndpoint().toServerUri().equals(setup.getEndpoint().toServerUri())) {
                    uriExceptionRegEx = regSetup.getEndpoint().getEndpoint();
                    if (!uriExceptionRegEx.startsWith("/")) {
                        uriExceptionRegEx = "/" + uriExceptionRegEx;
                    }
                    if (!uriExceptionRegEx.endsWith("/")) {
                        uriExceptionRegEx = uriExceptionRegEx + "/";
                    }
                    uriExceptionRegEx += ".*";
                }
                sortedListOfFilters.add(
                    new FailNoAuthorizationFilter(uriExceptionRegEx, auth.requiresAnonymousAccess()));
                final FilterChainProxy filterChainProxy = new FilterChainProxy(
                    new DefaultSecurityFilterChain(AnyRequestMatcher.INSTANCE, sortedListOfFilters));
                filterChainConsumer.accept(filterChainProxy);
            } else if (auth.getOAuth2Setup() != null) {
                OAuth2Setup oauth2Setup = auth.getOAuth2Setup();
                jwtCfg = Optional.of(JwtBearerTokenAuthenticationConfiguration.of(oauth2Setup.getIssuerUri(), 
                    oauth2Setup.getJwkSetUri(), oauth2Setup.getRequiredAud()));
            }
        } else {
            jwtCfg = context.getJwtBearerTokenAuthenticationConfiguration();
        }
        return jwtCfg;
    }
    
    /**
     * Adds RBAC authorization to {@code aggregator}.
     * 
     * @param aggregator the aggregator
     * @param spec the setup specification
     * @param component the component to use from {@code spec}
     * @return the authorizing aggregator or {@code aggregator}.
     */
    public static IAASAggregator addAuthorization(IAASAggregator aggregator, SetupSpec spec, AasComponent component) {
        IAASAggregator result = aggregator;
        ComponentSetup setup = spec.getSetup(component);
        if (null != setup) {
            AuthenticationDescriptor auth = setup.getAuthentication();
            if (AuthenticationDescriptor.isEnabledOnServer(auth) && auth.getAccessRules() != null) {
                RbacRuleSet rbacSet = new RbacRuleSet();
                for (de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.RbacRule r 
                    : auth.getAccessRules()) {
                    for (RbacAction action : r.getActions()) {
                        String path = null;
                        if (r.getPath() != null) {
                            path = r.getPath().replaceAll(de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor
                                .RbacRule.PATH_SEPARATOR, ".");
                        }
                        String aasId = null;
                        String smId = null;
                        if (r.getComponent() == RbacAasComponent.AAS) {
                            aasId = r.getElement();
                        } else if (r.getComponent() == RbacAasComponent.SUBMODEL 
                            || r.getComponent() == RbacAasComponent.SUBMODEL_ELEMENT) {
                            smId = r.getElement();
                        }
                        TargetInformation tInfo = new BaSyxObjectTargetInformation(aasId, smId, path);
                        rbacSet.addRule(new RbacRule(r.getRole().name(), ACTION_MAPPING.get(action), tInfo));
                    }
                }
                result = new AuthorizedAASAggregator<>(result, 
                    new SimpleRbacAASAggregatorAuthorizer<>(new PredefinedSetRbacRuleChecker(rbacSet), 
                        AuthenticationDescriptorBasedAuthenticationManager.AUTHENTICATOR), 
                    new AuthenticationContextProvider());
            }
        }
        return result;
    }

}
