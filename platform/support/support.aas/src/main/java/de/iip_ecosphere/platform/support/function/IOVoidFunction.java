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

package de.iip_ecosphere.platform.support.function;

import java.io.IOException;

import org.slf4j.LoggerFactory;

/**
 * A simple (optional) function that may throw an {@code IOException}.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface IOVoidFunction {
    
    /**
     * Executes the function.
     * 
     * @throws IOException may be thrown but also caught in {@link #optional(IOVoidFunction)}
     */
    public void execute() throws IOException;

    /**
     * Executes {@code func} but consumes {@link IOException} as execution is considered optional.
     * 
     * @param func the function to execute
     * @return {@code true} for success withozt exception, {@code false} for failed with caught exception
     */
    public static boolean optional(IOVoidFunction func) {
        boolean success = true;
        try {
            func.execute();
        } catch (IOException e) {
            LoggerFactory.getLogger(IOVoidFunction.class).debug(
                "Function call failed, but considered optional. " + e.getMessage());
            success = false;
        }
        return success;
    }
}
