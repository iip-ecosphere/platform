/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.BinderConfiguration;
import org.springframework.cloud.stream.binder.BinderCustomizer;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.BinderType;
import org.springframework.cloud.stream.binder.BinderTypeRegistry;
import org.springframework.cloud.stream.binder.DefaultBinderFactory;
import org.springframework.cloud.stream.config.BinderProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Provides a fix for multi-binder configurations in Spring Cloud Stream at least until version 3.1.1. There, the
 * type value for custom binders is not (always) correctly used and reported as null. We use 
 * either the Spring-like binders structure {@code binders.properties} (then like structure below 
 * {@code spring.cloud.stream.binders}, see {@link BinderFixProperties}) or, as fallback, 
 * {@code spring.cloud.stream.defaultBinder} but without settings, environment etc. Needs to be revisited 
 * for more recent versions. Code is based on 
 * <a href="// https://stackoverflow.com/questions/64826496/
 * injected-dependency-in-customized-kafkaconsumerinterceptor-is-null-with-spring-c">StackOverflow</a>. 
 * 
 * @author Gary Russell, StackOverflow
 * @author Holger Eichelberger, SSE
 */
@Component
public class BinderFix {

    @Autowired(required = false)
    private Collection<DefaultBinderFactory.Listener> binderFactoryListeners;

    @Autowired
    private BinderFixProperties properties;
    
    @Value("spring.cloud.stream.defaultBinder:")
    private String defaultBinder;

    /**
     * Returns the binder factory instance.
     * 
     * @param binderTypeRegistry the binder type registry
     * @param bindingServiceProperties the binding service properties
     * @return the binder factory
     */
    @Bean
    public BinderFactory binderFactory(BinderTypeRegistry binderTypeRegistry,
        BindingServiceProperties bindingServiceProperties) {
        BinderCustomizer binderCustomizer = null; // instance/origin unclear, ignored

        DefaultBinderFactory binderFactory = new DefaultBinderFactory(
            getBinderConfigurations(binderTypeRegistry, bindingServiceProperties, defaultBinder, properties),
            binderTypeRegistry, binderCustomizer) {

                @Override
                public synchronized <T> Binder<T, ?, ?> getBinder(String name,
                        Class<? extends T> bindingTargetType) {

                    Binder<T, ?, ?> binder = super.getBinder(name, bindingTargetType);
                    return binder;
                }

        };
        binderFactory.setDefaultBinder(bindingServiceProperties.getDefaultBinder());
        binderFactory.setListeners(this.binderFactoryListeners);
        return binderFactory;
    }

    /**
     * Returns the binder configurations. Copied and adapted from 
     * {@link org.springframework.cloud.stream.config.BindingServiceConfiguration}.
     * 
     * @param binderTypeRegistry the binder type registry
     * @param bindingServiceProperties the binding service properties
     * @param defaultBinder the default binder as second-level fallback
     * @param properties the binder properties as first-level fallback
     * @return the binder configurations
     */
    private static Map<String, BinderConfiguration> getBinderConfigurations(
        BinderTypeRegistry binderTypeRegistry, BindingServiceProperties bindingServiceProperties, 
        String defaultBinder, BinderFixProperties properties) {

        Map<String, BinderConfiguration> binderConfigurations = new HashMap<>();
        Map<String, BinderProperties> declaredBinders = bindingServiceProperties
                .getBinders();
        boolean defaultCandidatesExist = false;
        Iterator<Map.Entry<String, BinderProperties>> binderPropertiesIterator = declaredBinders
                .entrySet().iterator();
        while (!defaultCandidatesExist && binderPropertiesIterator.hasNext()) {
            defaultCandidatesExist = binderPropertiesIterator.next().getValue()
                    .isDefaultCandidate();
        }
        List<String> existingBinderConfigurations = new ArrayList<>();
        for (Map.Entry<String, BinderProperties> binderEntry : declaredBinders
                .entrySet()) {
            BinderProperties binderProperties = binderEntry.getValue();
            if (binderTypeRegistry.get(binderEntry.getKey()) != null) {
                binderConfigurations.put(binderEntry.getKey(),
                    new BinderConfiguration(binderEntry.getKey(),
                        binderProperties.getEnvironment(),
                        binderProperties.isInheritEnvironment(),
                        binderProperties.isDefaultCandidate()));
                existingBinderConfigurations.add(binderEntry.getKey());
            } else {
                // START ADDITION/MODIFICATION - replace binder properties/fallback default binder
                BinderProperties binderProp = binderProperties;
                String type = binderProperties.getType();
                if (null == type) {
                    if (null != properties && null != properties.getProperties()) {
                        binderProp = properties.getProperties().get(binderEntry.getKey());
                        type = binderProp.getType();
                    }
                    if (null == type) {
                        type = defaultBinder;
                    }
                }
                Assert.hasText(type,
                    "No 'type' property present for custom binder "
                            + binderEntry.getKey());
                binderConfigurations.put(binderEntry.getKey(),
                    new BinderConfiguration(type, 
                        binderProp.getEnvironment(), 
                        binderProp.isInheritEnvironment(), 
                        binderProp.isDefaultCandidate()));
                existingBinderConfigurations.add(binderEntry.getKey());
                // END ADDITION/MODIFICATION
            }
        }
        for (Map.Entry<String, BinderConfiguration> configurationEntry : binderConfigurations
                .entrySet()) {
            if (configurationEntry.getValue().isDefaultCandidate()) {
                defaultCandidatesExist = true;
            }
        }
        if (!defaultCandidatesExist) {
            for (Map.Entry<String, BinderType> binderEntry : binderTypeRegistry.getAll()
                    .entrySet()) {
                if (!existingBinderConfigurations.contains(binderEntry.getKey())) {
                    binderConfigurations.put(binderEntry.getKey(),
                        new BinderConfiguration(binderEntry.getKey(), new HashMap<>(),
                            true, "integration".equals(binderEntry.getKey()) ? false : true));
                }
            }
        }
        return binderConfigurations;
    }
    
}
