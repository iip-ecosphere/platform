/*******************************************************************************
 * Copyright (C) 2021 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx.basyx;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.HealthCheckValve;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.eclipse.basyx.vab.protocol.http.server.BaSyxContext;
import org.eclipse.basyx.vab.protocol.http.server.BasysHTTPServlet;
import org.eclipse.basyx.vab.protocol.http.server.JwtBearerTokenAuthenticationConfiguration;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.aas.basyx.security.Helper;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Starter Class for Apache Tomcat HTTP server that adds the provided servlets
 * and respective mappings on startup.
 * 
 * Taken over from BaSyx code to enable authentication adjustments prevented by
 * private methods there.
 * 
 * @author pschorn, espen, haque, danish
 * 
 */
@SuppressWarnings("deprecation")
public class BaSyxHTTPServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaSyxHTTPServer.class);

    private final Tomcat tomcat;

    static {
        // Enable coding of forward slash in tomcat
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");

        // Throw exception on startup error, unless user explicitly disables it
        if (System.getProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE") == null) {
            System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
        }
    }

    /**
     * Constructor.
     * 
     * Create new Tomcat instance and add the provided servlet mappings
     * 
     * @param context Basyx context with of url mappings to HTTPServlet
     * @param spec the setup specification
     * @param component the component to create the server for
     */
    public BaSyxHTTPServer(BaSyxContext context, SetupSpec spec, AasComponent component) {
        // Instantiate and setup Tomcat server
        tomcat = new Tomcat();

        // Set random name to prevent lifecycle expections during shutdown of multiple
        // instances
        tomcat.getEngine().setName(UUID.randomUUID().toString());

        if (context.isSecuredConnectionEnabled()) {
            Connector httpsConnector = tomcat.getConnector();
            configureSslConnector(context, httpsConnector);
        } else {
            tomcat.setPort(context.getPort());
        }

        tomcat.setHostname(context.getHostname());
        tomcat.getHost().setAppBase(".");

        configureHealthEndpoint();

        // Create servlet context
        // - Base path for resource files

        File docBase = new File(Helper.getDocBasePath(context, LOGGER)); // System.getProperty("java.io.tmpdir"));
        // - Create context for servlets
        final Context rootCtx = tomcat.addContext(context.getContextPath(), docBase.getAbsolutePath());

        Optional<JwtBearerTokenAuthenticationConfiguration> jwtCfg = Helper.configureSecurity(context, spec, component, 
            fcp -> {
                fcp.setFirewall(createHttpFirewall());
                addFilterChainProxyFilterToContext(rootCtx, fcp);
            });
        
        jwtCfg.ifPresent(jwtBearerTokenAuthenticationConfiguration -> addSecurityFiltersToContext(rootCtx,
                        jwtBearerTokenAuthenticationConfiguration));

        Iterator<Entry<String, HttpServlet>> servletIterator = context.entrySet().iterator();

        while (servletIterator.hasNext()) {
            addNewServletAndMappingToTomcatEnvironment(context, rootCtx, servletIterator.next());
        }
    }
    
    /**
     * Configures the health endpoint.
     */
    private void configureHealthEndpoint() {
        Valve valve = new HealthCheckValve();

        tomcat.getHost().getPipeline().addValve(valve);
    }

    /**
     * Adds new servlets and mappings.
     * 
     * @param context the BaSyx context
     * @param rootCtx the root context
     * @param entry the servlet entries
     */
    private void addNewServletAndMappingToTomcatEnvironment(BaSyxContext context, final Context rootCtx,
            Entry<String, HttpServlet> entry) {
        String mapping = entry.getKey();
        HttpServlet servlet = entry.getValue();

        configureCorsOrigin(context, servlet);

        Tomcat.addServlet(rootCtx, Integer.toString(servlet.hashCode()), servlet);
        rootCtx.addServletMappingDecoded(mapping, Integer.toString(servlet.hashCode()));
    }
    
    // checkstyle: stop exception type check

    /**
     * Configures the CORS origin.
     * 
     * @param context the context
     * @param servlet the servlet
     */
    private void configureCorsOrigin(BaSyxContext context, HttpServlet servlet) {
        if (!isCorsOriginDefined(context)) {
            return;
        }

        try {
            ((BasysHTTPServlet) servlet).setCorsOrigin(context.getAccessControlAllowOrigin());
        } catch (RuntimeException e) {
            LOGGER.info("DefaultServlet cannot be cast to BasysHTTPServlet " + e);
        }
    }

    // checkstyle: resumes exception type check

    /**
     * Returns whether CORS origin is defined.
     * 
     * @param context the context to look into
     * @return {@code true} for defined, {@code false} else
     */
    private boolean isCorsOriginDefined(BaSyxContext context) {
        return context.getAccessControlAllowOrigin() != null;
    }

    /**
     * Adds security filters to context.
     * 
     * @param context the context to modify
     * @param jwtBearerTokenAuthenticationConfiguration the bearer token authentication setup
     */
    private void addSecurityFiltersToContext(final Context context,
            final JwtBearerTokenAuthenticationConfiguration jwtBearerTokenAuthenticationConfiguration) {
        final FilterChainProxy filterChainProxy = createFilterChainProxy(jwtBearerTokenAuthenticationConfiguration);
        addFilterChainProxyFilterToContext(context, filterChainProxy);
    }

    /**
     * Adds a filter chain proxy to context.
     * 
     * @param context the context to modify
     * @param filterChainProxy the proxy
     */
    private void addFilterChainProxyFilterToContext(final Context context, final FilterChainProxy filterChainProxy) {
        final FilterDef filterChainProxyFilterDefinition = createFilterChainProxyFilterDefinition(filterChainProxy);
        context.addFilterDef(filterChainProxyFilterDefinition);

        final FilterMap filterChainProxyFilterMapping = createFilterChainProxyFilterMap();
        context.addFilterMap(filterChainProxyFilterMapping);
    }

    /**
     * Creates a filter chain proxy filter map.
     * 
     * @return the map
     */
    private FilterMap createFilterChainProxyFilterMap() {
        final FilterMap filterChainProxyFilterMapping = new FilterMap();
        filterChainProxyFilterMapping.setFilterName(FilterChainProxy.class.getSimpleName());
        filterChainProxyFilterMapping.addURLPattern("/*");
        return filterChainProxyFilterMapping;
    }

    /**
     * Creates a filter chain proxy filter definition.
     * 
     * @param filterChainProxy the proxy
     * @return the filter definition
     */
    private FilterDef createFilterChainProxyFilterDefinition(final FilterChainProxy filterChainProxy) {
        final FilterDef filterChainProxyFilterDefinition = new FilterDef();
        filterChainProxyFilterDefinition.setFilterName(FilterChainProxy.class.getSimpleName());
        filterChainProxyFilterDefinition.setFilterClass(FilterChainProxy.class.getName());
        filterChainProxyFilterDefinition.setFilter(filterChainProxy);
        return filterChainProxyFilterDefinition;
    }

    /**
     * Creates a filter chain proxy for token authentication.
     * 
     * @param jwtBearerTokenAuthenticationConfiguration the authentication configuration
     * @return the filter chain proxy
     */
    private FilterChainProxy createFilterChainProxy(
            final JwtBearerTokenAuthenticationConfiguration jwtBearerTokenAuthenticationConfiguration) {
        final FilterChainProxy filterChainProxy = new FilterChainProxy(
                createSecurityFilterChain(jwtBearerTokenAuthenticationConfiguration));
        filterChainProxy.setFirewall(createHttpFirewall());
        return filterChainProxy;
    }

    /**
     * Creates the HTTP firewall.
     * 
     * @return the firewall instance.
     */
    private HttpFirewall createHttpFirewall() {
        final StrictHttpFirewall httpFirewall = new StrictHttpFirewall();

        // '/' is valid character in an Internationalized Resource Identifier (IRI)
        httpFirewall.setAllowUrlEncodedSlash(true);

        return httpFirewall;
    }

    /**
     * Creates a security filter chain for OAuth2.
     * 
     * @param jwtBearerTokenAuthenticationConfiguration the authentication configuration
     * @return the filter chain
     */
    private SecurityFilterChain createSecurityFilterChain(
            final JwtBearerTokenAuthenticationConfiguration jwtBearerTokenAuthenticationConfiguration) {
        final List<Filter> sortedListOfFilters = new ArrayList<>();

        sortedListOfFilters.add(createBearerTokenAuthenticationFilter(jwtBearerTokenAuthenticationConfiguration));
        sortedListOfFilters.add(createExceptionTranslationFilter());

        return new DefaultSecurityFilterChain(AnyRequestMatcher.INSTANCE, sortedListOfFilters);
    }

    /**
     * Creates an exception translation filter.
     * 
     * @return the filter
     */
    private ExceptionTranslationFilter createExceptionTranslationFilter() {
        final BearerTokenAuthenticationEntryPoint authenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();
        return new ExceptionTranslationFilter(authenticationEntryPoint);
    }

    /**
     * Creates a bearer token authentication filter.
     * 
     * @param jwtBearerTokenAuthenticationConfiguration the token authentication configuration
     * @return the filter
     */
    private BearerTokenAuthenticationFilter createBearerTokenAuthenticationFilter(
            final JwtBearerTokenAuthenticationConfiguration jwtBearerTokenAuthenticationConfiguration) {
        final JwtDecoder jwtDecoder = createJwtDecoder(jwtBearerTokenAuthenticationConfiguration.getIssuerUri(),
                jwtBearerTokenAuthenticationConfiguration.getJwkSetUri(),
                jwtBearerTokenAuthenticationConfiguration.getRequiredAud().orElse(null));
        final JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        final AuthenticationManager authenticationManager = new ProviderManager(jwtAuthenticationProvider);
        return new BearerTokenAuthenticationFilter(authenticationManager);
    }

    /**
     * Creates a JWT decoder.
     * 
     * @param issuerUri the issuer URI
     * @param jwkSetUri the JWK Set URI
     * @param requiredAud the required audience for the OAuth2 token validator
     * @return the decoder
     */
    private JwtDecoder createJwtDecoder(final String issuerUri, final String jwkSetUri,
            @Nullable final String requiredAud) {
        final NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.from("RS256")).build();
        nimbusJwtDecoder.setJwtValidator(createOAuth2TokenValidator(issuerUri, requiredAud));

        return nimbusJwtDecoder;
    }

    /**
     * Creates an OAuth2 token validator.
     * 
     * @param issuerUri the issuer URI
     * @param requiredAud the required audience for the OAuth2 token validator
     * @return the validator
     */
    private OAuth2TokenValidator<Jwt> createOAuth2TokenValidator(final String issuerUri,
            @Nullable final String requiredAud) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(JwtValidators.createDefaultWithIssuer(issuerUri));

        if (requiredAud != null) {
            validators.add(createJwtClaimValidatorForRequiredAudience(requiredAud));
        }

        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    /**
     * Creates a JWT claim validator for required audience.
     * 
     * @param requiredAud the required audience for the OAuth2 token validator
     * @return the claim validator
     */
    private JwtClaimValidator<Collection<String>> createJwtClaimValidatorForRequiredAudience(final String requiredAud) {
        return new JwtClaimValidator<>(JwtClaimNames.AUD, aud -> null != aud && aud.contains(requiredAud));
    }

    /**
     * SSL Configuration for SSL connector.
     * 
     * @param context
     * @param httpsConnector
     */
    private void configureSslConnector(BaSyxContext context, Connector httpsConnector) {
        httpsConnector.setPort(context.getPort());
        httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        httpsConnector.setAttribute("keystoreFile", context.getCertificatePath());
        httpsConnector.setAttribute("clientAuth", "false");
        httpsConnector.setAttribute("sslProtocol", "TLS");
        httpsConnector.setAttribute("SSLEnabled", true);
        httpsConnector.setAttribute("protocol", "HTTP/1.1");
        httpsConnector.setAttribute("keystorePass", context.getKeyPassword());

        httpsConnector.setAttribute("keyAlias", "tomcat");

        httpsConnector.setAttribute("maxThreads", "200");
        httpsConnector.setAttribute("protocol", "org.apache.coyote.http11.Http11AprProtocol");
    }

    /**
     * Starts the server in a new thread to avoid blocking the main thread
     * 
     * <p>
     * This method blocks until the server is up and running.
     * 
     * <p>
     * If an error occurs during server startup the process is aborted and the
     * method returns immediately. {@link #hasEnded()} returns <code>true</code> in
     * this case. <br>
     * This behavior can be disabled by setting the system property
     * <code>org.apache.catalina.startup.EXIT_ON_INIT_FAILURE = false</code>, for
     * instance with the <code>-D</code> command line option when launching the JVM,
     * or through {@link System#setProperty(String, String)} (before the first call
     * to {@link BaSyxHTTPServer}). In this case the startup is finished regardless
     * of any errors and subsequent calls to {@link #hasEnded()} return
     * <code>false</code>, but the server might be left in an undefined and
     * non-functional state.
     * 
     * <p>
     * TODO: Throw exception upon startup failure. This is a breaking change, so
     * wait until next major version.
     */
    public void start() {
        LOGGER.trace("Starting Tomcat.....");

        Thread serverThread = new Thread(() -> {
            try {
                stopTomcatServerIfRunningAlready();

                // Adds listener that notifies the tomcat object when the server has started
                tomcat.getServer().addLifecycleListener(new LifecycleListener() {
                    @Override
                    public void lifecycleEvent(LifecycleEvent event) {
                        if (event.getLifecycle().getState() == LifecycleState.STARTED) {
                            synchronized (tomcat) {
                                tomcat.notifyAll();
                            }
                        }
                    }
                });

                tomcat.getConnector();
                tomcat.start();

                // Keeps the server thread alive until the server is shut down
                tomcat.getServer().await();
            } catch (LifecycleException e) {
                LOGGER.error("Failed to start HTTP server.", e);

                synchronized (tomcat) {
                    tomcat.notifyAll();
                }
            }
        });
        serverThread.start();

        // Wait until tomcat is started before returning
        EnumSet<LifecycleState> returnStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.FAILED);
        synchronized (tomcat) {
            try {
                while (!returnStates.contains(tomcat.getServer().getState())) {
                    tomcat.wait();
                }
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting for tomcat to start. Stopping tomcat.", e);
                shutdown();
            }
        }
    }

    /**
     * Stops it if the tomcat server is running.
     * 
     * @throws LifecycleException if stopping tomcat fails
     */
    private void stopTomcatServerIfRunningAlready() throws LifecycleException {
        if (!isTomcatServerRunning()) {
            return;
        }

        tomcat.stop();
    }

    /**
     * Returns whether the tomcat server is running.
     * 
     * @return {@code true} for running, {@code false} else
     */
    private boolean isTomcatServerRunning() {
        return tomcat != null && tomcat.getServer().getState() == LifecycleState.STARTED;
    }

    /**
     * This Method stops and destroys the tomcat instance. This is important since
     * Tomcat would be already bound to port 8080 when new tests are run that
     * require a start of tomcat
     */
    public void shutdown() {
        LOGGER.trace("Shutting down BaSyx HTTP Server...");

        try {
            tomcat.stop();
            tomcat.destroy();
        } catch (LifecycleException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Exception in shutdown", e);
        }
    }

    /**
     * Returns a value indicating whether the server is currently running.
     * 
     * @return <code>false</code> if the server is running, <code>true</code>
     *         otherwise.
     */
    public boolean hasEnded() {
        return tomcat.getServer().getState() != LifecycleState.STARTED;
    }
}
