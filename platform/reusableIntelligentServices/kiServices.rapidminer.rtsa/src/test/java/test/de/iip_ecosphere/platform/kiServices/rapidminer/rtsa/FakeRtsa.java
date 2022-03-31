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

package test.de.iip_ecosphere.platform.kiServices.rapidminer.rtsa;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

/**
 * A very simple RTSA fake server as we are not allowed to publish RTSA.
 * 
 * @author Holger Eichelberger, SSE
 */
@SuppressWarnings("restriction")
public class FakeRtsa {

    /**
     * Executes the fake server.
     * 
     * @param args ignored
     * @throws IOException shall not occur
     */
    public static void main(String[] args) throws IOException {
        int serverPort = 8090;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/services/iip_basic/score_v1", (exchange -> {
            System.out.println("Received Request");
            String respText = "{\"data\":[{\"value2\":3.0,\"value1\":1.3,"
                + "\"confidence\":0.863,\"prediction\":\"false\",\"id\":1.0}]}";
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
            exchange.close();
        }));
        server.setExecutor(null); // creates a default executor
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // puuh
            }
            System.out.println("Started Application in 2500 ms"); // we need some output for state change
        }).start();
        server.start();
    }

}
