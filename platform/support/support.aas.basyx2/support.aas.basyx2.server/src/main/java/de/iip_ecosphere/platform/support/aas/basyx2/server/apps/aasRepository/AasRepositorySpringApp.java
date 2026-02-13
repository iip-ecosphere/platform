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

package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.aasRepository;

import org.eclipse.digitaltwin.basyx.aasrepository.AasRepository;
import org.eclipse.digitaltwin.basyx.aasrepository.backend.CrudAasRepositoryFactory;
import org.eclipse.digitaltwin.basyx.aasrepository.feature.authorization.AuthorizedAasRepository;
import org.eclipse.digitaltwin.basyx.aasservice.AasServiceFactory;
import org.eclipse.digitaltwin.basyx.aasservice.backend.AasBackend;
import org.eclipse.digitaltwin.basyx.aasservice.backend.CrudAasServiceFactory;
import org.eclipse.digitaltwin.basyx.core.filerepository.FileRepository;
import org.eclipse.digitaltwin.basyx.core.filerepository.InMemoryFileRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.BaSyxNames;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.security.RbacUtils;

import org.eclipse.digitaltwin.basyx.aasservice.backend.InMemoryAasBackend;

/**
 * Spring application running an AAS repository.
 * 
 * Somehow feature-based composition does not work in here. More investigation needed.
 * 
 * @author Monika Staciwa, SSE
 */
@SpringBootApplication
@Configuration
@ComponentScan(
    basePackages = { BaSyxNames.PACKAGE_BASYX, BaSyxNames.PACKAGE_PLUGIN_BASYX_SERVER_SECURITY }, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AasRepositoryTypeFilter.class))
public class AasRepositorySpringApp {

    /**
     * Starts the application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AasRepositorySpringApp.class, args);
    }
    
    /**
     * Returns the AAS repository instance.
     * 
     * @param authDesc the actual authentication descriptor
     * @return the AAS repository
     */
    @Bean 
    public static AasRepository getAasRepository(@Nullable AuthenticationDescriptor authDesc) {
        FileRepository fileRepository = new InMemoryFileRepository();
        AasRepository result = CrudAasRepositoryFactory.builder().backend(new InMemoryAasBackend())
            .fileRepository(fileRepository).create();
        if (AuthenticationDescriptor.definesRbac(authDesc)) {
            result = new AuthorizedAasRepository(result, RbacUtils.createAasPermissionResolver(authDesc));
        }
        return result;
    }
    
    /**
     * Returns the AAS service factory instance.
     * 
     * @return the AAS service factory 
     */
    @Bean
    public static AasServiceFactory getAasServiceFactory() {
        return new CrudAasServiceFactory(new InMemoryAasBackend(), new InMemoryFileRepository());
    }
    
    /**
     * Returns the AAS backend instance.
     * 
     * @return the AAS backend instance
     */
    @Bean 
    public static AasBackend getAasBackend() {
        return new InMemoryAasBackend();
    }

    /**
     * Returns the file repository instance.
     * 
     * @return the file repository instance
     */
    @Bean 
    public static FileRepository getFileRepository() {
        return new InMemoryFileRepository();
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
