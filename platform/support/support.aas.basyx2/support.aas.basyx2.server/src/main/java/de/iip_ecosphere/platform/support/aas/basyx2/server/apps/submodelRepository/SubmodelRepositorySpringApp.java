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

package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.submodelRepository;

import java.io.IOException;
import java.util.List;

import org.eclipse.digitaltwin.basyx.aasrepository.AasRepositoryFactory;
import org.eclipse.digitaltwin.basyx.aasrepository.backend.CrudAasRepositoryFactory;
import org.eclipse.digitaltwin.basyx.aasservice.backend.AasBackend;
import org.eclipse.digitaltwin.basyx.aasservice.backend.CrudAasServiceFactory;
import org.eclipse.digitaltwin.basyx.aasservice.backend.InMemoryAasBackend;
import org.eclipse.digitaltwin.basyx.core.filerepository.FileRepository;
import org.eclipse.digitaltwin.basyx.core.filerepository.InMemoryFileRepository;
import org.eclipse.digitaltwin.basyx.submodelrepository.SubmodelRepository;
import org.eclipse.digitaltwin.basyx.submodelservice.InMemorySubmodelBackend;
import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelServiceFactory;
import org.eclipse.digitaltwin.basyx.submodelservice.backend.CrudSubmodelServiceFactory;
import org.eclipse.digitaltwin.basyx.submodelrepository.backend.CrudSubmodelRepositoryFactory;
import org.eclipse.digitaltwin.basyx.submodelrepository.feature.authorization.AuthorizedSubmodelRepository;
import org.eclipse.digitaltwin.basyx.submodelrepository.feature.operation.delegation.HTTPOperationDelegation;
import org.eclipse.digitaltwin.basyx.submodelrepository.feature.operation.delegation.OperationDelegation;
import org.eclipse.digitaltwin.basyx.submodelrepository.feature.operation.delegation.
    OperationDelegationSubmodelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.basyx2.common.AssetServerKeyStoreDescriptor;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.BaSyxExceptionResolver;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.BaSyxNames;
import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.security.RbacUtils;
import de.iip_ecosphere.platform.support.aas.basyx2.common.Tools;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Spring application for serving a submodel repository with spring. 
 * 
 * Somehow feature-based composition does not work in here. More investigation needed.
 * 
 * @author Monika Staciwa
 */
@SpringBootApplication
@Configuration
@ComponentScan(
    basePackages = { BaSyxNames.PACKAGE_BASYX, BaSyxNames.PACKAGE_PLUGIN_BASYX_SERVER_SECURITY }, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = SubmodelRepositoryTypeFilter.class))
public class SubmodelRepositorySpringApp implements WebMvcConfigurer {

    @Autowired(required = false)
    private AssetServerKeyStoreDescriptor kstore;
    @Autowired(required = false)
    private AuthenticationDescriptor authDesc;
    
    /**
     * Starts the application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SubmodelRepositorySpringApp.class, args);
    }

    /**
     * Customizes the operation delegation. [Copied]
     * 
     * @param mapper the object mapper to customize
     * @return the operation delegation instance
     */
    @Bean
    public OperationDelegation getOperationDelegation(ObjectMapper mapper) {
        return new HTTPOperationDelegation(createWebClient(mapper));
    }

    /**
     * Creates the web client.
     * 
     * @param mapper the object mapper to customize
     * @return the configured web client
     */
    private WebClient createWebClient(ObjectMapper mapper) {
        ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(configurer -> {
            configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
            configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
        }).build();

        WebClient.Builder builder = WebClient.builder().exchangeStrategies(strategies);
        if (kstore != null) {
            KeyStoreDescriptor ksd = kstore.getDescriptor();
            if (ksd.appliesToClient()) {
                try {
                    JdkClientHttpConnector httpConnector = new JdkClientHttpConnector(
                        Tools.createHttpClient(ksd).build());
                    builder = builder.clientConnector(httpConnector);
                } catch (IOException e) {
                    LoggerFactory.getLogger(SubmodelRepositorySpringApp.class).error(
                        "While creating WebClient for SubmodelRepository, staying with https: {}", e.getMessage());
                }
            }
        }
        if (null != authDesc) {
            final WebClient.Builder b = builder;
            AuthenticationDescriptor.authenticate((n, v) -> b.defaultHeader(n, v), authDesc);
        }
        return builder.build();
    }
    
    /**
     * Returns the submodel repository instance.
     * 
     * @param operationDelegation the configured/created operation delegation instance
     * @param authDesc the actual authentication descriptor
     * @return the instance
     */
    @Bean 
    public static SubmodelRepository getSubmodelRepository(OperationDelegation operationDelegation, 
        @Nullable AuthenticationDescriptor authDesc) {
        SubmodelRepository result = CrudSubmodelRepositoryFactory.builder()
            .backend(new InMemorySubmodelBackend())
            .fileRepository(new InMemoryFileRepository())
            .create();
        result = new OperationDelegationSubmodelRepository(result, operationDelegation);
        if (AuthenticationDescriptor.definesRbac(authDesc)) {
            result = new AuthorizedSubmodelRepository(result, RbacUtils.createSubmodelPermissionResolver(authDesc));
        }
        return result;
    }

    /**
     * Returns the AAS backend instance.
     * 
     * @return the instance
     */
    @Bean 
    public static AasBackend getAasBackend() {
        return new InMemoryAasBackend();
    }
      
    /**
     * Returns the file repository instance.
     * 
     * @return the instance
     */
    @Bean 
    public static FileRepository getFileRepository() {
        return new InMemoryFileRepository();
    }
    
    /**
     * Returns the file repository factory instance.
     * 
     * @return the instance
     */
    @Bean
    public AasRepositoryFactory aasRepositoryFactory() {
        return new CrudAasRepositoryFactory(
            new InMemoryAasBackend(),
            new CrudAasServiceFactory(new InMemoryAasBackend(), new InMemoryFileRepository()), "");
    }

    /**
     * Returns the submodel service factory.
     * 
     * @return the instance
     */
    @Bean
    @Lazy // to mitigate circular dependencies between bean :(
    public SubmodelServiceFactory getSubmodelServiceFactory() {
        return new CrudSubmodelServiceFactory(new InMemorySubmodelBackend(), new InMemoryFileRepository());
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

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, new BaSyxExceptionResolver());
    }
    
}
