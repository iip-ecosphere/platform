/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment.spring;

import java.util.function.Supplier;

import de.iip_ecosphere.platform.services.environment.testing.TestBroker;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Startup plugin for in-situ testing the transport broker, her testing via {@link SpringAsyncServiceBase}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestSpringBroker implements de.iip_ecosphere.platform.services.environment.Starter.Plugin {

    /**
     * Local service base for testing, disable setup as done below.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyServiceBase extends SpringAsyncServiceBase {
        
        @Override
        protected void ensureSetup() {
            // do nothing here, happens in run
        }

    }
    
    @Override
    public void run(String[] args) {
        TransportSetup setup = TestBroker.createSetup(args);
        String channel = CmdLine.getArg(args, "test.channel", null);
        String routingKey = CmdLine.getArg(args, "test.routingKey", "");
        if (setup != null && channel != null) {
            ReceptionCallback<String> rcp = TestBroker.createStringReceptionCallback();
            Supplier<TransportSetup> old = Transport.setTransportSetup(() -> setup);
            MyServiceBase service = new MyServiceBase();
            service.createReceptionCallback(channel, d -> rcp.received(d), String.class, routingKey);
            
            Transport.send(c -> c.asyncSend(channel, "Async Testdata"), "Async Testdata", routingKey);
            Transport.send(c -> c.syncSend(channel, "Sync Testdata"), "Sync Testdata", routingKey);
            service.detach();
            Transport.releaseConnector(false);
            Transport.setTransportSetup(old);
        } else {
            System.out.println("Insufficient parameter: " + getHelp(""));
        }
    }
    
    @Override
    public String getHelp(String indent) {
        return "tests the transport broker functionality: --test.host=<host> --test.port=<port> "
            + "--test.authKey=<authKey> --test.channel=<channel> --test.routingKey=<routingKey>";
    }

}
