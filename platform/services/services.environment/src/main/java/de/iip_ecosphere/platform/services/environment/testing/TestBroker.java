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

package de.iip_ecosphere.platform.services.environment.testing;

import java.io.IOException;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

/**
 * Startup plugin for in-situ testing the transport broker.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestBroker implements de.iip_ecosphere.platform.services.environment.Starter.Plugin {

    /**
     * Creates a transport setup instance from {@code args}.
     * 
     * @param args the command line arguments
     * @return the setup instance or <b>null</b> if none can be created
     */
    public static TransportSetup createSetup(String[] args) {
        TransportSetup setup = null;
        String host = CmdLine.getArg(args, "test.host", null);
        int port = CmdLine.getIntArg(args, "test.port", -1);
        String authKey = CmdLine.getArg(args, "test.authKey", null);
        if (host != null && port > 0) {
            setup = new TransportSetup();
            setup.setHost(host);
            setup.setPort(port);
            if (null != authKey) {
                setup.setAuthenticationKey(authKey);
            }
        }
        return setup;
    }

    /**
     * Creates a default string reception callback.
     * 
     * @return the callback
     */
    public static ReceptionCallback<String> createStringReceptionCallback() {
        ReceptionCallback<String> rcp = new ReceptionCallback<String>() {

            @Override
            public void received(String data) {
                System.out.println("Received: " + data);
            }

            @Override
            public Class<String> getType() {
                return String.class;
            }
        };
        return rcp;
    }
    
    @Override
    public void run(String[] args) {
        TransportSetup setup = createSetup(args);
        String channel = CmdLine.getArg(args, "test.channel", null);
        if (setup != null && channel != null) {
            try {
                Supplier<TransportSetup> old = Transport.setTransportSetup(() -> setup);
                ReceptionCallback<String> rcp = createStringReceptionCallback();
                TransportConnector conn = Transport.createConnector();
                conn.setReceptionCallback(channel, rcp);
                conn.asyncSend(channel, "Async Testdata");
                conn.syncSend(channel, "Sync Testdata");
                conn.detachReceptionCallback(channel, rcp);
                Transport.releaseConnector(false);
                Transport.setTransportSetup(old);
            } catch (IOException e) {
                System.out.println("Failed: " + e.getMessage());
            }
        } else {
            System.out.println("Insufficient parameter: " + getHelp(""));
        }
    }
    
    @Override
    public String getHelp(String indent) {
        return "tests the transport broker functionality: --test.host=<host> --test.port=<port> "
            + "--test.authKey=<authKey> --test.channel=<channel>";
    }

}
