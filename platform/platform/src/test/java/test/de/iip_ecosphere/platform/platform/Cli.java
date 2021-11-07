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

package test.de.iip_ecosphere.platform.platform;

import de.iip_ecosphere.platform.platform.PlatformSetup;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.iip_aas.config.EndpointHolder;

/**
 * Modifies the Cli setup programmatically so that it fits to the tests in configuration.configuration.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Cli extends de.iip_ecosphere.platform.platform.Cli {

    /**
     * The command line main function.
     * 
     * @param args interactive mode if no arguments are given, else one shot execution (may be 
     */
    public static void main(String[] args) {
        PlatformSetup setup = PlatformSetup.getInstance();
        EndpointHolder server = setup.getAas().getServer();
        server.setPort(9001);
        server.setPath("");
        server.setSchema(Schema.HTTP);
        EndpointHolder registry = setup.getAas().getRegistry();
        registry.setPort(9002);
        registry.setPath("registry");
        registry.setSchema(Schema.HTTP);
        de.iip_ecosphere.platform.platform.Cli.main(args);
    }
    
}
