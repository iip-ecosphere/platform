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

package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.net.URI;

/**
 * Represents a sender instance for converted transport data.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Sender<T> {

    /**
     * Initiates a connection. This method does not block.
     */
    public void connect();
    
    /**
     * Blocks until connected or failed to do so.
     *
     * @return returns whether it succeeded or not.
     * @throws InterruptedException thrown when blocking thread gets interrupted
     */
    public boolean connectBlocking() throws InterruptedException;

    /**
     * Is the state open.
     *
     * @return {@code true} for open, {@code false} for not open
     */
    boolean isOpen();

    /**
     * Is the state closed.
     *
     * @return {@code true} for closed, {@code false} for not closed
     */
    boolean isClosed();

    /**
     * Sends data to the connected server.
     *
     * @param data the data that will be transmitted.
     * @throws IOException if the data cannot be send/translated
     */
    public void send(T data) throws IOException;

    /**
     * Closes the sender. This method does not block.
     */
    public void close();
    
    /**
     * Closes the sender but blocks until closing is done or failed to do so.
     *
     * @throws InterruptedException thrown when the blocking thread gets interrupted
     */
    public void closeBlocking() throws InterruptedException;

    /**
     * Returns the URI that this sender is connected to.
     *
     * @return the URI connected to
     */
    public URI getURI();

}
