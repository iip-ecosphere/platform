/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.transport;

/**
 * Just a second type for tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Command {
    
    private String command;

    /**
     * Creates a command instance.
     * 
     * @param command the command
     */
    public Command(String command) {
        this.command = command;
    }

    /**
     * Returns the description of the product.
     * 
     * @return the description
     */
    public String getCommand() {
        return command;
    }
    
}
