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

package de.iip_ecosphere.platform.support.aas.basyx2.apps.asset;

import java.util.Arrays;
import java.util.function.Function;

import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.basyx.http.Aas4JHTTPSerializationExtension;
import org.eclipse.digitaltwin.basyx.http.BaSyxHTTPConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.aas.basyx2.AasOperationsProvider;
import de.iip_ecosphere.platform.support.aas.basyx2.apps.common.ExcludeBasyxTypeFilter;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Spring application representing asset operations. Operations must be registered in the operations provider 
 * defined via dependency injection in the application context.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootApplication
@Configuration
@ComponentScan(
    basePackages = { "org.eclipse.digitaltwin.basyx", "de.iip_ecosphere.platform.support.aas.basyx2.apps.security" }, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = ExcludeBasyxTypeFilter.class))
@RestController("AssetApp")
@EnableWebSecurity
public class AssetSpringApp implements WebMvcConfigurer {
    
    @Autowired
    private AasOperationsProvider opProvider;
    
    /**
     * Starts the application.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AssetSpringApp.class, args);
    }
    /**
     * Returns the configured object mapper for serialization/deserialization.
     * 
     * @return the object mapper
     */
    @Bean
    public ObjectMapper getMapper() {
        return new BaSyxHTTPConfiguration().jackson2ObjectMapperBuilder(
            Arrays.asList(new Aas4JHTTPSerializationExtension())).build();
    } 
    
    // checkstyle: stop exception type check
    
    /**
     * Defines the security filter chain.
     * 
     * @param httpSecurity the security object
     * @return the modified/defined filter chain
     * @throws Exception if defining fails
     */
    @Profile("!test")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).httpBasic(
            Customizer.withDefaults()).csrf(
                c -> c.ignoringRequestMatchers(r -> checkPath(r)));
        return httpSecurity.build();
    }
    
    /**
     * Checks a path for being equipped with requested prefix.
     * 
     * @param request the request
     * @return {@code true} has prefix, {@code false} else
     */
    private boolean checkPath(HttpServletRequest request) {
        String path = request.getPathTranslated();
        return null == path ? false : path.startsWith("/" + AasOperationsProvider.PREFIX_OPERATIONS);
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

    /**
     * Generically executes service functions defined by the operations provider.
     * 
     * @param opName the operation name
     * @param requestData the request data
     * @return the operation execution result
     */
    @PostMapping("/" + AasOperationsProvider.PREFIX_SERVICE + "{opName}")
    public ResponseEntity<OperationVariable[]> invokeService(@PathVariable("opName") String opName, 
        @RequestBody OperationVariable[] requestData) {
        return invokeOperation(opName, requestData, opProvider.getServiceFunction(opName));
    }

    /**
     * Generically executes functions defined by the operations provider.
     * 
     * @param opName the operation name
     * @param requestData the request data
     * @return the operation execution result
     */
    @PostMapping("/" + AasOperationsProvider.PREFIX_OPERATIONS + "{category}/{opName}")
    public ResponseEntity<OperationVariable[]> invokeOperation(@PathVariable("category") String category, 
        @PathVariable("opName") String opName, @RequestBody OperationVariable[] requestData) {
        return invokeOperation(opName, requestData, opProvider.getOperation(category, opName));
    }

    // checkstyle: stop exception type check

    /**
     * Executes a given function.
     * 
     * @param opName the operation name
     * @param requestData the request data
     * @param func the function, may be <b>null</b>
     * @return the function execution result
     */
    static ResponseEntity<OperationVariable[]> invokeOperation(String opName, OperationVariable[] requestData, 
        Function<Object[], Object> func) {
        HttpStatus responseStatus;
        OperationVariable[] result = null;
        
        if (null != func) {
            try {
                Object[] params = new Object[requestData.length];
                for (int r = 0; r < requestData.length; r++) {
                    SubmodelElement re = requestData[r].getValue();
                    if (re instanceof Property) {
                        params[r] = ((Property) re).getValue();
                    }
                }
                Object funcResult = func.apply(params);
                result = new OperationVariable[1];
                result[0] = new DefaultOperationVariable();
                Property resultProperty = new DefaultProperty();
                result[0].setValue(resultProperty);
                resultProperty.setValue(null == funcResult ? null : funcResult.toString());
                responseStatus = HttpStatus.OK;
            } catch (Throwable t) {
                LoggerFactory.getLogger(AssetSpringApp.class).error("Calling AAS operation '{}': {}", 
                    opName, t.getMessage());
                responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } else {
            responseStatus = HttpStatus.NOT_IMPLEMENTED;
        }
        return new ResponseEntity<OperationVariable[]>(result, responseStatus);        
    }

    // checkstyle: resume exception type check

}
