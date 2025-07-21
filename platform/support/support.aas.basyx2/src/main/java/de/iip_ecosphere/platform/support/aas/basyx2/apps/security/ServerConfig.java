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

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor.IdentityTokenWithRole;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Performs a web server configuration for tomcat.
 * 
 * @author Holger Eichelberger, SSE
 */
@Configuration
@EnableWebSecurity
public class ServerConfig {
    
    @Autowired(required = false)
    private AuthenticationDescriptor authDesc;
    
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

    /**
     * Defines the security filter chain.
     * 
     * @param httpSecurity the security object
     * @return the modified/defined filter chain
     * @throws Exception if defining fails
     */
    @Bean
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
            });
            /*.formLogin(f -> f.loginPage("/login")
                .permitAll()
            ).logout(l -> l.permitAll())*/
            //if (null != authDesc.getOAuth2Setup()) {
                // TODO authDesc.getOAuth2Setup()
            //}
            httpSecurity.httpBasic(Customizer.withDefaults());  // TODO conditional
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
    }

    // checkstyle: stop exception type check

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
