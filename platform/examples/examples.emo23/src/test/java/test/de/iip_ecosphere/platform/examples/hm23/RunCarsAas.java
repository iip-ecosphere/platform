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

package test.de.iip_ecosphere.platform.examples.hm23;

import de.iip_ecosphere.platform.examples.hm23.carAas.CarAas;

/**
 * Runs the cars AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RunCarsAas {
    
    /**
     * Main program.
     * 
     * @param args the command line args, the first one may be the port of the AAS/Registry server
     */
    public static void main(String[] args) {
        int port = 9989;
        String hostname = "localhost";
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("First arg is not a numer, using default port " + port);
            }
            if (args.length > 1) {
                hostname = args[1];
            }
        }
        CarAas.buildAas(hostname, port); // starts thread and runs forever
    }

}
