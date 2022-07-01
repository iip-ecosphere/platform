/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.test.apps.serviceImpl.routingTest;

import de.iip_ecosphere.platform.connectors.events.ConnectorInputHandler;
import de.iip_ecosphere.platform.connectors.events.EventHandlingConnector;
import iip.datatypes.RoutingCommand;

/**
 * Connector event handler for asynchronous, backward {@link RoutingCommand}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorCommandEventHandler implements ConnectorInputHandler<RoutingCommand> {

    @Override
    public void received(RoutingCommand data, EventHandlingConnector connector) {
        System.out.println("Connector received cmd: " + data);
    }

}
