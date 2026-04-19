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

package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.IdentityTokenWithRole;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Performs a web server configuration for tomcat.
 * 
 * @author Holger Eichelberger, SSE
 */
@Configuration
@EnableWebSecurity
public class ServerConfig implements WebMvcConfigurer {
    
    private static ThreadLocal<Object> principal = new ThreadLocal<>();
    @Autowired(required = false)
    private AuthenticationDescriptor authDesc;

    /**
     * Implements the authentication interceptor.
     */
    private HandlerInterceptor authInterceptor = new HandlerInterceptor() {
        
        private Map<String, IdentityTokenWithRole> users;
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
            if (null == users) {
                users = new HashMap<>();
                List<IdentityTokenWithRole> sUsers = authDesc.getServerUsers();
                if (null != sUsers) {
                    sUsers.forEach(u -> users.put(u.getUserName(), u));
                }
            }
            
            String authHeader = request.getHeader("Authorization");

            principal.set(null);
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                // Extract the Base64 part
                String base64Credentials = authHeader.substring("Basic ".length());
                byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(decodedBytes);
                
                // credentials format is "username:password"
                final String[] values = credentials.split(":", 2);
                if (values.length == 2) {
                    String username = values[0];
                    String password = values[1];
                    
                    IdentityTokenWithRole token = users.get(username);
                    if (null != token) {
                        if (token.getTokenDataAsString().equals(password)) {
                            principal.set(token.getRole().name());
                            return true;                
                        }
                    }
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.addHeader("WWW-Authenticate", "Basic realm=\"DefaultRealm\"");
                return false;
            }

            if (authDesc.requiresAnonymousAccess()) {
                principal.set(AuthenticationDescriptor.DefaultRole.NONE.name());
                return true;
            }
            // If we reach here, authentication failed
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("WWW-Authenticate", "Basic realm=\"DefaultRealm\"");
            return false;
        }

    };

    /**
     * Returns the security principal determined by the authentication interceptor.
     * 
     * @return the principal, may be <b>null</b>
     */
    public static Object getPrincipal() {
        return principal.get();
    }
    
    /**
     * Creates the servlet web server factory.
     * 
     * @param sslConnectorCustomizer the connector customizer
     * @return the factory instance
     */
    @Bean
    public ServletWebServerFactory servletContainer(SSLConnectorCustomizer sslConnectorCustomizer) {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addConnectorCustomizers(sslConnectorCustomizer);
        return tomcat;
    }

    // checkstyle: stop exception type check

    /*
     * Defines the security filter chain.
     * 
     * @param httpSecurity the security object
     * @return the modified/defined filter chain
     * @throws Exception if defining fails
     */
/*    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.securityMatcher("/**");        
        if (null != authDesc) {
            httpSecurity.authorizeHttpRequests(req -> {
                req.requestMatchers("/error").permitAll();
                if (authDesc.requiresAnonymousAccess()) {
                    req.anyRequest().permitAll();
                } else {
                    req.anyRequest().authenticated();
                }
            });*/
            /*.formLogin(f -> f.loginPage("/login")
                .permitAll()
            ).logout(l -> l.permitAll())*/
            //if (null != authDesc.getOAuth2Setup()) {
                // TODO authDesc.getOAuth2Setup()
            //}
/*            httpSecurity.httpBasic(Customizer.withDefaults());  // TODO conditional
            if (authDesc.requiresAnonymousAccess()) {
                AuthenticationDescriptor.Role role = AuthenticationDescriptor.DefaultRole.NONE;
                httpSecurity.anonymous(c -> c.key(role.name()).authorities(role.name()).principal(role.name()));
            }
            httpSecurity.csrf(c -> c.disable()); // TODO
            return httpSecurity.build();
        } else {
            httpSecurity.authorizeHttpRequests(req -> 
                req.anyRequest().permitAll())
                    .httpBasic(Customizer.withDefaults())
                    .csrf(c -> c.ignoringRequestMatchers(r -> true));
            return httpSecurity.build();
        }
    }*/

    // checkstyle: stop exception type check
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (authDesc != null) {
            // Apply this interceptor to all paths, or specific ones like "/api/**"
            registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/favicon.ico");
        }
    }
    
    /**
     * Provides the user details service.
     * 
     * @return the user details service
     */
    @Bean
    public UserDetailsService userDetailsService() {
        List<UserDetails> users = new ArrayList<>();
        if (null != authDesc && authDesc.getServerUsers() != null) {
            List<IdentityTokenWithRole> sUsers = authDesc.getServerUsers();
            if (null != sUsers) {
                for (IdentityTokenWithRole t : sUsers) {
                    switch (t.getType()) {
                    case USERNAME:
                        String pwPrefix = "";
                        String tea = t.getTokenEncryptionAlgorithm();
                        if (IdentityToken.ENC_PLAIN_UTF_8.equalsIgnoreCase(tea)) {
                            pwPrefix = "{noop}";
                        } else if (IdentityToken.ENC_BCRYPT.equalsIgnoreCase(tea)) {
                            pwPrefix = "{bcrypt}";
                        } else if (IdentityToken.ENC_SHA256.equalsIgnoreCase(tea)) {
                            pwPrefix = "{sha256}";
                        }
                        users.add(
                            User.withUsername(t.getUserName())
                                .password(pwPrefix + t.getTokenDataAsString())
                                //.roles(t.getRole().name())
                                .authorities(new SimpleGrantedAuthority(t.getRole().name()))
                                .build()
                        );
                        break;
                    default:
                        LoggerFactory.getLogger(getClass()).warn("Token type {} for user {} cannot be processed. "
                            + "Skipping.", t.getType(), t.getUserName());
                        break;
                    }
                }
            }
        }
        return new InMemoryUserDetailsManager(users);
    }   

    /**
     * Provides the web security customizer, in particular helpful for debugging.
     * 
     * @return the customizer
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.debug(false);
    }    
    
}
