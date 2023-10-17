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

package de.iip_ecosphere.platform.examples.hm23.carAas;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.setup.CmdLine;

/**
 * Runs the cars AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CarsAasServer implements Server {

    private CarAas aas;

    /**
     * Creates a Car AAS Server.
     * 
     * @param args the command line arguments, recognizes --port=&lt;int&gt; with default 9989 
     *     and --host=&lt;String&gt; with default localhost
     */
    public CarsAasServer(String[] args) {
        this(args, false);
    }

    /**
     * Creates a Car AAS Server.
     * 
     * @param args the command line arguments, recognizes --port=&lt;int&gt; with default 9989 
     *     and --host=&lt;String&gt; with default localhost
     * @param withHook adds a shutdown hook
     */
    public CarsAasServer(String[] args, boolean withHook) {
        int port = CmdLine.getIntArg(args, "port", 9989);
        String hostname = CmdLine.getArg(args, "host", ServerAddress.LOCALHOST);
        aas = new CarAas(hostname, port, withHook);
    }
    
    /**
     * Main program.
     * 
     * @param args the command line args, the first one may be the port of the AAS/Registry server
     */
    public static void main(String[] args) {
        LoggerFactory.getLogger(CarsAasServer.class).info("Starting Car AAS server with args {}", (Object) args);
        CarsAasServer server = new CarsAasServer(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(true);
            LoggerFactory.getLogger(CarsAasServer.class).info("AAS server stopped");
        }));
        server.start();
    }

    @Override
    public Server start() {
        new Thread(aas).start();
        return this;
    }

    @Override
    public void stop(boolean dispose) {
        aas.stop(dispose);
    }

}
