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

package de.iip_ecosphere.platform.support.commons;

import java.util.concurrent.ExecutionException;

/**
 * Watches out for file system modifications. Abstracted from apache commons.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface FileAlterationMonitor {

    /**
     * Starts monitoring.
     *
     * @throws ExecutionException if an error occurs initializing the observer
     */
    public void start() throws ExecutionException;

    /**
     * Stops monitoring.
     *
     * @throws ExecutionException if an error occurs initializing the observer
     */
    public void stop() throws ExecutionException;

}
