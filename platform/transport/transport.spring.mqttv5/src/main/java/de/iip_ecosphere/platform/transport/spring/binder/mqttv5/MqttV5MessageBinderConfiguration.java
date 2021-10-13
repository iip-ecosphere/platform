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

package de.iip_ecosphere.platform.transport.spring.binder.mqttv5;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.spring.BeanHelper;

/**
 * Represents the MQTT v5 message binder plugin.
 * 
 * @author Holger Eichelberger, SSE
 */
@Configuration
@EnableConfigurationProperties(MqttConfiguration.class)
public class MqttV5MessageBinderConfiguration {

    private MqttClient client = new MqttClient(); // local instance, no autowiring
    
    /**
     * Returns the binder provisioner.
     * 
     * @return the binder provisioner
     */
    @Bean
    @ConditionalOnMissingBean
    public MqttV5MessageBinderProvisioner mqttv5BinderProvisioner() {
        return new MqttV5MessageBinderProvisioner(client);
    }

    /**
     * Returns the client instance.
     * 
     * @return the client instance
     */
    @Bean
    @ConditionalOnMissingBean
    public MqttClient mqttClient() {
        return client;
    }

    /**
     * Returns the message binder.
     * 
     * @param messageBinderProvisioner the provisioner with access to the destinations
     * @return the message binder
     */
    @Bean
    @ConditionalOnMissingBean // name of this method must be the same as in META-INF/spring.binders
    public MqttV5MessageBinder mqttv5Binder(MqttV5MessageBinderProvisioner messageBinderProvisioner) {
        return new MqttV5MessageBinder(null, messageBinderProvisioner, client);
    }

    /**
     * Provides a transport parameter instance configured through the binder configuration.
     * 
     * @param ctx the current application context (autowired)
     * @param config the actual MQTT configuration
     * @return the transport parameter instance
     */
    @Bean
    @ConditionalOnMissingBean // method is optional, only if needed in testing
    public TransportParameter mqttTransportParameter(@Autowired ApplicationContext ctx, 
        @Autowired MqttConfiguration config) {
        return BeanHelper.registerInParentContext(ctx, config.toTransportParameter(), "mqtt v5");
    }

}
