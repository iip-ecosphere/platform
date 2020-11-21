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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Represents the MQTT v3 message binder plugin.
 * 
 * @author Holger Eichelberger, SSE
 */
@Configuration
@EnableConfigurationProperties(AmqpConfiguration.class)
public class AmqpMessageBinderConfiguration {

    /**
     * Returns the binder provisioner.
     * 
     * @return the binder provisioner
     */
    @Bean
    @ConditionalOnMissingBean
    public AmqpMessageBinderProvisioner mqttMessageBinderProvisioner() {
        return new AmqpMessageBinderProvisioner();
    }

    /**
     * Returns the message binder.
     * 
     * @param messageBinderProvisioner the provisioner with access to the destinations
     * @return the message binder
     */
    @Bean
    @ConditionalOnMissingBean
    public AmqpMessageBinder mqttMessageBinder(AmqpMessageBinderProvisioner messageBinderProvisioner) {
        return new AmqpMessageBinder(null, messageBinderProvisioner);
    }

}
