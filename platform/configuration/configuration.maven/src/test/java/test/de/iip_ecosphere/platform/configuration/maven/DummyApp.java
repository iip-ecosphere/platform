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

package test.de.iip_ecosphere.platform.configuration.maven;

/**
 * A testing application for {@link ProcessUnitTest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DummyApp {

    public static final String PARAM_PREFIX = "--";
    public static final String PARAM_VALUE_SEP = "=";

    /**
     * Emulates reading a Spring-like parameter if the configuration is not yet in place.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code dflt}
     */
    public static String getArg(String[] args, String argName, String dflt) {
        String result = dflt;
        String prefix = PARAM_PREFIX + argName + PARAM_VALUE_SEP;
        for (int a = 0; a < args.length; a++) {
            String arg = args[a];
            if (arg.startsWith(prefix)) {
                result = arg.substring(prefix.length());
                break;
            }
        }
        return result;
    }
    
    /**
     * Returns an int command line argument.
     * 
     * @param args the arguments
     * @param argName the argument name (without {@link #PARAM_PREFIX} or {@link #PARAM_VALUE_SEP})
     * @param dflt the default value if the argument cannot be found
     * @return the value of argument or {@code dflt}
     */
    public static int getIntArg(String[] args, String argName, int dflt) {
        int result;
        try {
            result = Integer.parseInt(getArg(args, argName, String.valueOf(dflt)));
        } catch (NumberFormatException e) {
            result = dflt;
        }
        return result;
    }
    
    /**
     * Just sleeps for the given amount of milliseconds.
     * 
     * @param ms the milliseconds to wait for
     */
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }
    
    /**
     * Simple test program.
     * 
     * @param args command line arguments; 
     *     {@code --start=int} indicates the first value to start counting at (default {@code 1}); 
     *     {@code --max=int} indicates the maximum value to stop looping at (default {@code 10});
     *     {@code --modulo=int} indicates the group size for the second output on the error stream (default {@code 5})
     */
    public static void main(String[] args) {
        int start = getIntArg(args, "start", 1);
        int max = getIntArg(args, "max", 10);
        int modulo = getIntArg(args, "modulo", 5);
        for (int i = start; i <= max; i++) {
            System.out.println("STEP: " + i);
            if (i % modulo == 0) {
                System.err.println("DONE: " + modulo);
            }
            sleep(500);
        }
    }

}
