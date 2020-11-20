/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.spring;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

@Component
@EnableConfigurationProperties(SerializerConfiguration.class)
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupApplicationListener.class);

    @Autowired
    private SerializerConfiguration cfg;
    private boolean done = false;
 
    @Override 
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!done) {
            for (String s: cfg.getSerializers()) {
                try {
                    Serializer<?> ser = (Serializer<?>) Class.forName(s).getConstructor().newInstance();
                    SerializerRegistry.registerSerializer(ser);
                    LOGGER.info("Registered Serializer " + s);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException
                        | ClassNotFoundException e) {
                    LOGGER.error("Serializer class " + s + " cannot be registered: " + e.getMessage());
                }
            }
            done = true;
        }
    }
    
}
