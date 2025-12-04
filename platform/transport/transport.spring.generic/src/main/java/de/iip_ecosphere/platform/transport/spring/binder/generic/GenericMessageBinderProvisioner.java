/********************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.spring.binder.generic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;
import org.springframework.stereotype.Component;

/**
 * Provisions consumer and producer properties.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
public class GenericMessageBinderProvisioner implements ProvisioningProvider<ConsumerProperties, ProducerProperties> {

    @Autowired
    private GenericConfiguration options;
    
    private GenericClient client;

    /**
     * Creates a provisioner instance.
     * 
     * @param amqpClient the client instance
     */
    public GenericMessageBinderProvisioner(GenericClient amqpClient) {
        this.client = amqpClient;
    }
    
    @Override
    public ProducerDestination provisionProducerDestination(String name, ProducerProperties properties)
            throws ProvisioningException {
        client.createClient(options);
        return new GenericMessageDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group, ConsumerProperties properties)
            throws ProvisioningException {
        client.createClient(options);
        return new GenericMessageDestination(name);
    }

    /**
     * Implements a generic message destination.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class GenericMessageDestination implements ProducerDestination, ConsumerDestination {

        private final String destination;

        /**
         * Implements a message destination.
         * 
         * @param destination the destination (topic)
         */
        private GenericMessageDestination(final String destination) {
            this.destination = destination;
        }

        @Override
        public String getName() {
            return destination.trim(); // TODO check, fix if appropriate
        }

        @Override
        public String getNameForPartition(int partition) {
            throw new UnsupportedOperationException("Partitioning is not implemented.");
        }

    }

}
