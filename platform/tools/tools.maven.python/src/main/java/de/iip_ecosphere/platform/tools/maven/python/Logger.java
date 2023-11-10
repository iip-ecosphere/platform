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

package de.iip_ecosphere.platform.tools.maven.python;

/**
 * Simple maven-independent logger interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Logger {

    /**
     * Logs a warning.
     * 
     * @param warning the warning
     */
    public void warn(String warning);

    /**
     * Logs an error.
     * 
     * @param error the error
     */
    public void error(String error);

    /**
     * Logs a throwable.
     * 
     * @param throwable the throwable
     */
    public void error(Throwable throwable);

    /**
     * Logs an information.
     * 
     * @param info the information
     */
    public void info(String info);

}
