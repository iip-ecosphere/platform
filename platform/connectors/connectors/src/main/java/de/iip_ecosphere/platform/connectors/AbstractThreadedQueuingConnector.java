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

import java.io.IOException;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;


/**
 * Queues incoming data for sequential delayed processing. Further, provides a reusable base for connectors that 
 * require a {@link ModelAccess} instance per calling thread.
 * 
 * Call {@link #startQueueProcessing()} in {@link #connectImpl(ConnectorParameter)} and {@link #stopQueueProcessing()} 
 * in {@link #disconnectImpl()}.
 * 
 * @param <O>  the output type from the underlying machine/platform
 * @param <I>  the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * @param <M> the model access type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractThreadedQueuingConnector<O, I, CO, CI, M extends ModelAccess> 
    extends AbstractThreadedConnector<O, I, CO, CI, M> {

    private final SharedBuffer<CI> queue = new SharedBuffer<>(50);
    private final QueueProcessor queueProcessor = new QueueProcessor();

    /**
     * Processes the queue.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class QueueProcessor implements Runnable {
        
        private boolean running = true;

        @Override
        public void run() {
            try {
                while (running) {
                    CI value = queue.poll();
                    origWrite(value);
                }
            } catch (IOException | InterruptedException e) {
                LoggerFactory.getLogger(AbstractThreadedQueuingConnector.class).error("While queue processing: {}", 
                    e.getMessage());
            }
        }

        /**
         * Starts queue processing.
         */
        private void start() {
            queue.clear();
            running = true;
        }
        
        /**
         * Stops queue processing.
         */
        private void stop() {
            running = false;
        }
        
    }
    
    /**
     * Creates an instance and installs the protocol adapter(s) with a default
     * selector for the first adapter. For integration compatibility, connector
     * constructors are supposed to accept a var-arg parameter for adapters.
     * 
     * @param adapter the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty
     *                                  or adapters are <b>null</b>
     * @see #setModelAccessSupplier(Supplier)
     */
    @SafeVarargs
    protected AbstractThreadedQueuingConnector(ProtocolAdapter<O, I, CO, CI>... adapter) {
        super(adapter);
    }

    /**
     * Creates an instance and installs the protocol adapter(s). For integration
     * compatibility, connector constructors are supposed to accept a var-arg
     * parameter for adapters.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector
     *                 for the first adapter)
     * @param adapter  the protocol adapter(s)
     * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty
     *                                  or adapters are <b>null</b>
     * @see #setModelAccessSupplier(Supplier)
     */
    @SafeVarargs
    protected AbstractThreadedQueuingConnector(AdapterSelector<O, I, CO, CI> selector, 
        ProtocolAdapter<O, I, CO, CI>... adapter) {
        super(selector, adapter);
    }
    
    /**
     * Starts queue processing. If not called, data is queued but not processed.
     */
    protected void startQueueProcessing() {
        new Thread(queueProcessor).start();
        queueProcessor.start();
    }
    
    /**
     * Stops queue processing.
     */
    protected void stopQueueProcessing() {
        if (null != queueProcessor) {
            queueProcessor.stop();
        }
    }
    
    @Override
    public void write(CI data) throws IOException {
        try {
            queue.offer(data);
        } catch (InterruptedException e) {
            throw new IOException(e); 
        }
    }
    
    /**
     * The original implementation of {@link #write(Object)}, overwritten for queuing in this class. Writes the 
     * given {@code data} to the underlying machine/platform.
     * 
     * @param data the data to send to {@code stream}
     * @throws IOException in case that problems during the connection happens
     */
    private void origWrite(CI data) throws IOException {
        super.write(data);
    }

}
