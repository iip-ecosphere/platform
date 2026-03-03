/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A shared producer/consumer buffer.
 * 
 * @param <T> the entity type
 * @author Holger Eichelberger, SSE
 */
public class SharedBuffer<T> {
    
    private final Queue<T> buffer = new LinkedList<>();
    private final int capacity;

    /**
     * Creates the buffer.
     * 
     * @param capacity the initial capacity
     */
    public SharedBuffer(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Offers an entity to the queue.
     * 
     * @param value the value to be offered
     * @throws InterruptedException
     */
    public synchronized void offer(T value) throws InterruptedException {
        while (buffer.size() == capacity) {
            wait(); // Wait if buffer is full
        }
        buffer.add(value);
        notifyAll(); // Notify consumer
    }

    /**
     * Polls an entity from the buffer; if there is none, waits for the next entity.
     * 
     * @return the entity
     * @throws InterruptedException if waiting is interrupted
     */
    public synchronized T poll() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait(); // Wait if buffer is empty
        }
        T value = buffer.poll();
        notifyAll(); // Notify producer
        return value;
    }
    
    /**
     * Clears the buffer.
     */
    public synchronized void clear() {
        buffer.clear();
    }

}
