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

package de.iip_ecosphere.platform.transport.connectors.rabbitmq;

import de.iip_ecosphere.platform.transport.DefaultTransportFactoryDescriptor;
import de.iip_ecosphere.platform.transport.TransportFactory.ConnectorCreator;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

/**
 * The AMQP transport factory descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RabbitMqAmqpTransportFactoryDescriptor extends DefaultTransportFactoryDescriptor {

    /**
     * The main creator.
     */
    public static final ConnectorCreator MAIN = new ConnectorCreator() {

        @Override
        public TransportConnector createConnector() {
            return new RabbitMqAmqpTransportConnector();
        }

        @Override
        public String getName() {
            return RabbitMqAmqpTransportConnector.NAME;
        }
        
    };
    
    @Override
    public ConnectorCreator getMainCreator() {
        return MAIN;
    }

}
