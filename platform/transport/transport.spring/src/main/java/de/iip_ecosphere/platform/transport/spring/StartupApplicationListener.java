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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

@Component
@EnableConfigurationProperties({SerializerConfiguration.class, TransportFactoryConfiguration.class})
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StartupApplicationListener.class);

    @Autowired
    private SerializerConfiguration cfg;
    @Autowired
    private TransportFactoryConfiguration transportCfg;
    private boolean done = false;
 
    @Override 
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!done) {
            registerSerializers();
            configureTransportFactory();
            done = true;
        }
    }

    /**
     * Registers serializers based on class names from the Spring configuration.
     */
    private void registerSerializers() {
        for (String s: cfg.getSerializers()) {
            Serializer<?> ser = obtainInstance(s, "Serializer registration:", Serializer.class);
            if (null != ser) {
                SerializerRegistry.registerSerializer(ser);
                LOGGER.info("Registered Serializer " + s);
            }
        }
        SerializerRegistry.setName(cfg.getName());
    }
    
    /**
     * Configures the transport factory.
     */
    private void configureTransportFactory() {
        ConnectorCreator cr = obtainConnectorCreator(transportCfg.getMainTransportClassName(), 
            "Configuring main transport connector");
        if (null != cr) {
            TransportFactory.setMainImplementation(cr);
            LOGGER.error("Registered main transport connector " + transportCfg.getMainTransportClassName());
        }

        cr = obtainConnectorCreator(transportCfg.getDirectMemoryClassName(), 
            "Configuring direct memory transport connector");
        if (null != cr) {
            TransportFactory.setDmImplementation(cr);
            LOGGER.error("Registered direct-memory transport connector " + transportCfg.getDirectMemoryClassName());
        }
        
        cr = obtainConnectorCreator(transportCfg.getInterProcessClassName(), 
            "Configuring inter-process transport connector");
        if (null != cr) {
            TransportFactory.setIpcImplementation(cr);
            LOGGER.error("Registered inter-process transport connector " + transportCfg.getInterProcessClassName());
        }
    }
    
    /**
     * Obtains a connector creator.
     * 
     * @param className the class name
     * @param context a context description to be included into an error message
     * @return the connector creator, may be <b>null</b> if {@code className} does not exist
     */
    private @Nullable ConnectorCreator obtainConnectorCreator(String className, String context) {
        ConnectorCreator result = null;
        if (className.length() > 0) {
            TransportConnector conn = obtainInstance(className, context, TransportConnector.class);
            if (null != conn) {
                final Class<? extends TransportConnector> cls = conn.getClass();
                final String name = conn.getName();
                result = new ConnectorCreator() {
                    
                    @Override
                    public TransportConnector createConnector() {
                        TransportConnector result;
                        try {
                            result = cls.getConstructor().newInstance();
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                            result = null;
                            // we already tested this before
                        }
                        return result;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                };
            }
        }
        return result;
    }
    
    /**
     * Obtains an instance of {@code className}.
     * 
     * @param <T> the type to create
     * @param className the class name
     * @param context a context description to be included into an error message
     * @param type the type to create
     * @return the connector creator, may be <b>null</b> if {@code className} does not exist
     */
    private <T> T obtainInstance(String className, String context, Class<? extends T> type) {
        T result;
        try {
            result = type.cast(Class.forName(className).getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            result = null;
            LOGGER.error(context + ": class " + className + " cannot be created: " + e.getMessage());
        }
        return result;
    }
    
}
