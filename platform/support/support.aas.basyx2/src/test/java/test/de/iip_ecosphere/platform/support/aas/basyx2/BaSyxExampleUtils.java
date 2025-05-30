/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx2;

import java.io.IOException;
import java.util.Arrays;

import de.iip_ecosphere.platform.support.setup.CmdLine;
import test.de.iip_ecosphere.platform.support.aas.AbstractAasExample;

/**
 * Utility functions for example tests with BaSyx.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxExampleUtils {
    
    /**
     * Executes a test standalone.
     * 
     * @param instance the example/test instance
     * @param args command line arguments, the first argument may be {@code --withOperations} to 
     *   enable the creation of operations
     */
    public static void execute(AbstractAasExample instance, String[] args) {
        instance.setCreateOperations(false); // fails in AASPackageExplorer (Dec 2023)
        instance.setCreateMultiLanguageProperties(false);
        if (CmdLine.getBooleanArg(args, "withOperations", false)) {
            instance.setCreateOperations(true);
        }
        try {
            instance.testCreateAndStore(false);
            System.out.println("Success: Output written to " + Arrays.toString(instance.getTargetFiles()));
        } catch (IOException e) {
            System.out.println("Creation of the AAS failed: " + e.getMessage());
        }
    }

}
