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

/**
 * Listener for events from a {@link Tailer}. Abstracted from apache commons.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface TailerListener {

    /**
     * Handles a line from a Tailer.
     * 
     * @param line the line.
     */
    public default void handle(final String line) {
    }
    
    /**
     * Called if a file rotation is detected.
     *
     * This method is called before the file is reopened, and fileNotFound may
     * be called if the new file has not yet been created.
     */
    public default void fileRotated() {
    }
    
    /**
     * Called each time the Tailer reaches the end of the file.
     *
     */
    public default void endOfFileReached() {
    }

    /**
     * This method is called if the tailed file is not found.
     */
    public default void fileNotFound() {
    }    

}
