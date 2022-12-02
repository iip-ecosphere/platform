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

package de.iip_ecosphere.platform.examples.python;

import java.io.File;

import de.iip_ecosphere.platform.examples.SpringStartup;

/**
 * Starts the application by emulating a bit platform functionality (Spring Cloud Stream service manager).
 * 
 * @author Holger Eichelberger, SSE
 */
public class Starter {
    
    /**
     * Starts the application.
     * 
     * @param args ignored
     */
    public static void main(String[] args) {
        File f = new File("gen/py/SimpleSynchronousPythonDemoFlowApp/target/"
            + "SimpleSynchronousPythonDemoFlowApp-0.1.0-SNAPSHOT-bin.jar");
        SpringStartup.start(f, args);
    }

}
