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

package de.iip_ecosphere.platform.support.aas.basyx2.apps.aasRegistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import org.eclipse.digitaltwin.basyx.aasregistry.service.events.RegistryEventLogSink;
import org.eclipse.digitaltwin.basyx.aasregistry.service.storage.AasRegistryStorage;
import org.eclipse.digitaltwin.basyx.aasregistry.service.storage.memory.InMemoryAasRegistryStorage;

/**
 * Spring application for starting an in-memory AAS registry.
 * 
 * Somehow feature-based composition does not work in here. More investigation needed.
 * 
 * @author Monika Staciwa, SSE
 */
@SpringBootApplication
@Configuration
@ComponentScan(
    basePackages = { "org.eclipse.digitaltwin.basyx", "de.iip_ecosphere.platform.support.aas.basyx2.apps.security" }, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AasRegistryTypeFilter.class))
@Component
public class AasRegistrySpringApp {

    /**
     * Starts the application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AasRegistrySpringApp.class, args);
    }

    /**
     * Returns the AAS registry storage.
     * 
     * @return the storage instance
     */
    @Bean
    public static AasRegistryStorage getAasRegistry() {     
        return new InMemoryAasRegistryStorage();
    }

    /**
     * Returns the AAS registry event sink.
     * 
     * @return the event sink instance
     */
    @Bean 
    public org.eclipse.digitaltwin.basyx.aasregistry.service.events.RegistryEventSink registryEventSink() {
        return new RegistryEventLogSink(); // implements RegistryEventSink
    }
    
    // checkstyle: stop exception type check
    
    /**
     * Defines the security filter chain.
     * @param http the security instance
     * @return the filterchain
     * @throws Exception if something fails
     */
    @Profile("test")
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { // preliminary
        //https://www.baeldung.com/spring-security-deactivate
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }    

    // checkstyle: resume exception type check

}
