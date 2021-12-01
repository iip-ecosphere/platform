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

package de.iip_ecosphere.platform.transport.spring.binder.amqp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.spring.BeanHelper;

/**
 * Represents the AMQP message binder plugin.
 * 
 * @author Holger Eichelberger, SSE
 */
@Configuration
@EnableConfigurationProperties(AmqpConfiguration.class)
public class AmqpMessageBinderConfiguration {
    
    /**
     * Returns the binder provisioner.
     * 
     * @param amqpClient the client instance (autowired)
     * @return the binder provisioner
     */
    @Bean
    @ConditionalOnMissingBean
    public AmqpMessageBinderProvisioner amqpBinderProvisioner(AmqpClient amqpClient) {
        return new AmqpMessageBinderProvisioner(amqpClient);
    }
    
    /**
     * Returns the client instance.
     * 
     * @return the client instance
     */
    @Bean
    @ConditionalOnMissingBean
    public AmqpClient amqpClient() {
        return new AmqpClient();
    }

    /**
     * Returns the message binder.
     * 
     * @param messageBinderProvisioner the provisioner with access to the destinations
     * @param amqpClient the client instance (autowired)
     * @return the message binder
     */
    @Bean
    @ConditionalOnMissingBean
    public AmqpMessageBinder amqpBinder(AmqpMessageBinderProvisioner messageBinderProvisioner, AmqpClient amqpClient) {
        return new AmqpMessageBinder(null, messageBinderProvisioner, amqpClient);
    }

    /**
     * Provides a transport parameter instance configured through the binder configuration.
     * 
     * @param ctx the current application context (autowired)
     * @param config the actual MQTT configuration
     * @return the transport parameter instance
     */
    @Bean
    @ConditionalOnMissingBean
    public TransportParameter mqttTransportParameter(@Autowired ApplicationContext ctx, 
        @Autowired AmqpConfiguration config) {
        return BeanHelper.registerInParentContext(ctx, config.toTransportParameter(), "amqp");
    }

}
