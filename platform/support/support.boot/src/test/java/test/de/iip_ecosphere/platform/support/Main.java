/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support;

import de.iip_ecosphere.platform.support.TimeUtils;

/**
 * Just a program that runs for a certain time. Used for process tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Main {
 
    /**
     * Runs the program.
     * 
     * @param args optional, first arg may determine the number of steps to emit
     */
    public static void main(String[] args) {
        int maxSteps = 10;
        if (args.length > 0) {
            try {
                maxSteps = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
            }
        }
        for (int i = 1; i <= maxSteps; i++) {
            System.out.println("Running step " + i);
            TimeUtils.sleep(200);
        }
    }

}
