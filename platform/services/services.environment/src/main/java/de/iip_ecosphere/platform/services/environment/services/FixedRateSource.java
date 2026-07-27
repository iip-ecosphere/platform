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

package de.iip_ecosphere.platform.services.environment.services;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

/**
 * A fixed rate source for experiments. May be used in a service, may be used standalone.
 * 
 * @param <D> the data handled by the source
 * @author Holger Eichelberger, SSE
 */
public abstract class FixedRateSource<D> {

    private Timer timer = new Timer();
    private Supplier<D> dataCreator;
    private FixedRateParams params;
    private DataIngestor<D> ingestor;
    private Supplier<ServiceState> stateSupplier;
    
    /**
     * A parameter/configuration object for the {@link FixedRateSource}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class FixedRateParams {

        private long maxMessagesPerPoll;
        private long fixedDelay;
        private int duration;
        private Supplier<String> headlineSupplier;

        /**
         * Creates a fixed rate parameters object.
         * 
         * @param fixedDelay the fixed delay between two polls/ingestions in ms
         * @param maxMessagesPerPoll the (maximum) number of messages per poll
         * @param duration the overall duration of the run/experiment in ms
         */
        public FixedRateParams(long fixedDelay, long maxMessagesPerPoll, int duration) {
            this(fixedDelay, maxMessagesPerPoll, duration, null);
        }

        /**
         * Creates a fixed rate parameters object.
         * 
         * @param fixedDelay the fixed delay between two polls/ingestions in ms
         * @param maxMessagesPerPoll the (maximum) number of messages per poll
         * @param duration the overall duration of the run/experiment in ms
         * @param headlineSupplier the headline text supplier, may be <b>null</b>
         */
        public FixedRateParams(long fixedDelay, long maxMessagesPerPoll, int duration, 
            Supplier<String> headlineSupplier) {
            this.fixedDelay = fixedDelay;
            this.maxMessagesPerPoll = Math.max(1, maxMessagesPerPoll);
            this.duration = duration;
            this.headlineSupplier = headlineSupplier;
        }

        /**
         * Returns the (maximum) number of messages per poll/ingestions.
         * 
         * @return the (maximum) number of messages per poll
         */
        public long getMaxMessagesPerPoll() {
            return maxMessagesPerPoll;
        }

        /**
         * Returns the fixed delay between two polls/ingestions.
         * 
         * @return the fixed delay in ms
         */
        public long getFixedDelay() {
            return fixedDelay;
        }

        /**
         * Returns the overall duration of the run/experiment, i.e., when an auto-stop shall set in.
         * 
         * @return the duration in ms
         */
        public int getDuration() {
            return duration;
        }

        /**
         * Returns a supplier for a headline when starting the run/experiment.
         * 
         * @return the headline supplier, may be <b>null</b>
         */
        Supplier<String> getHeadlineSupplier() {
            return headlineSupplier;
        }

    }

    /**
     * Creates a fixed-rate source.
     * 
     * @param dataCreator a supplier creating the data
     * @param params the parameter/configuration object
     */
    public FixedRateSource(Supplier<D> dataCreator, FixedRateParams params) {
        this(dataCreator, params, noIngestor(), runningState());
    }

    /**
     * Creates a fixed-rate source.
     * 
     * @param dataCreator a supplier creating the data
     * @param params the parameter/configuration object
     * @param ingestor data ingestor
     * @param stateSupplier service state supplier
     */
    public FixedRateSource(Supplier<D> dataCreator, FixedRateParams params, DataIngestor<D> ingestor, 
        Supplier<ServiceState> stateSupplier) {
        this.dataCreator = dataCreator;
        this.params = params;
        setIngestor(ingestor);
        this.stateSupplier = stateSupplier;
    }

    /**
     * Creates a no-ingestor that ignores data.
     * 
     * @param <D> the type of data
     * @return the ingestor
     */
    public static <D> DataIngestor<D> noIngestor() {
        return d -> { };
    }
    
    /**
     * Creates a standard running state supplier.
     * 
     * @return the supplier
     */
    public static Supplier<ServiceState> runningState() {
        return () -> ServiceState.RUNNING;
    }

    /**
     * Changes the ingestor.
     * 
     * @param ingestor the new ingestor
     */
    protected void setIngestor(DataIngestor<D> ingestor) {
        this.ingestor = ingestor;
    }

    /**
     * Starts the source, if enabled starting/stopping the experiment.
     */
    public void start() {
        if (doStartExperiment()) {
            CompletableFuture.runAsync(() -> {
                notifyStarting();
                if (null != params.getHeadlineSupplier()) {
                    LoggerFactory.getLogger(this).info(params.headlineSupplier.get());
                }
                TimeUtils.sleep(params.getDuration());
                notifyStopping();
                timer.cancel();
                try {
                    disconnect();
                } catch (IOException e) {
                    LoggerFactory.getLogger(this).error("During cleanup: {}", e.getMessage());
                }
                System.exit(0);
            });
        }
        
        timer.scheduleAtFixedRate(new TimerTask() {
            
            @Override
            public void run() {
                if (stateSupplier.get() == ServiceState.RUNNING) {
                    int count = 0;
                    while (count < params.getMaxMessagesPerPoll()) {
                        try {
                            ingest(dataCreator.get());
                        } catch (IOException e) {
                            LoggerFactory.getLogger(this).error("While sending data: {}", e.getMessage());
                        }
                        count++;
                    }
                }
            }
        }, 0, params.getFixedDelay());
    }
    
    /**
     * Shall the run/experiment be started by this instance.
     * 
     * @return {@code true} for starting, {@code false} for not starting
     */
    protected abstract boolean doStartExperiment();
    
    /**
     * Notifies about starting the run/experiment.
     */
    protected abstract void notifyStarting();

    /**
     * Notifies about stopping the run/experiment.
     */
    protected abstract void notifyStopping();

    /**
     * Notifies that data has been sent.
     */
    protected abstract void notifySent();

    /**
     * Is called to ingest data provided by {@link #dataCreator}.
     * 
     * @param data the data
     * @throws IOException if ingesting fails
     */
    protected void ingest(D data) throws IOException {
        ingestor.ingest(data);
        notifySent();
    }
        
    /**
     * Called to disconnect from the ingestor.
     * 
     * @throws IOException if disconnecting fails
     */
    protected abstract void disconnect() throws IOException;
    
    /**
     * Specializes the fixed-rate source for {@link TransportConnector}.
     * 
     * @param <D> the type of data
     * @author Holger Eichelberger, SSE
     */
    public abstract static class TransportFixedRateSource<D> extends FixedRateSource<D> {

        private TransportConnector connector;
        private String channel;
        
        /**
         * A parameter/configuration object for the {@link FixedRateSource}.
         * 
         * @author Holger Eichelberger, SSE
         */
        public static class TransportFixedRateParams extends FixedRateParams {

            private TransportConnector connector;
            private String channel;

            /**
             * Creates a fixed rate parameters object.
             * 
             * @param fixedDelay the fixed delay between two polls/ingestions in ms
             * @param maxMessagesPerPoll the (maximum) number of messages per poll
             * @param duration the overall duration of the run/experiment in ms
             */
            public TransportFixedRateParams(long fixedDelay, long maxMessagesPerPoll, int duration) {
                super(fixedDelay, maxMessagesPerPoll, duration);
            }

            /**
             * Creates a fixed rate parameters object.
             * 
             * @param fixedDelay the fixed delay between two polls/ingestions in ms
             * @param maxMessagesPerPoll the (maximum) number of messages per poll
             * @param duration the overall duration of the run/experiment in ms
             * @param headlineSupplier the headline text supplier, may be <b>null</b>
             */
            public TransportFixedRateParams(long fixedDelay, long maxMessagesPerPoll, int duration, 
                Supplier<String> headlineSupplier) {
                super(fixedDelay, maxMessagesPerPoll, duration, headlineSupplier);
            }
            
            /**
             * Specifies the transport settings.
             * 
             * @param connector the connector instance to use
             * @param channel the default channel on {@code connector} to use
             * @return <b>this</b> for chaining
             */
            public TransportFixedRateParams with(TransportConnector connector, String channel) {
                this.connector = connector;
                this.channel = channel;
                return this;
            }

            /**
             * Returns the transport connector.
             * 
             * @return the connector
             */
            public TransportConnector getConnector() {
                return connector;
            }

            /**
             * Returns the default transport channel.
             * 
             * @return the channel
             */
            public String getChannel() {
                return channel;
            }
            
        }

        /**
         * Creates a transport-based fixed rate source.
         * 
         * @param dataCreator the data creator incrementally creating the data to ingest
         * @param params the parameter/configuration object
         */
        public TransportFixedRateSource(Supplier<D> dataCreator, TransportFixedRateParams params) {
            this(dataCreator, params, runningState());
        }

        /**
         * Creates a transport-based fixed rate source.
         * 
         * @param dataCreator the data creator incrementally creating the data to ingest
         * @param params the parameter/configuration object
         */
        public TransportFixedRateSource(Supplier<D> dataCreator, TransportFixedRateParams params, 
            Supplier<ServiceState> stateSupplier) {
            this(dataCreator, params, noIngestor(), stateSupplier);
            setIngestor(d -> sendSafe(channel, d));
        }

        /**
         * Creates a transport-based fixed rate source. Ingestion happens through {@code ingestor} rather than 
         * the supplied transport connector while explicit sending happens via the transport connector.
         * 
         * @param dataCreator the data creator incrementally creating the data to ingest
         * @param params the parameter/configuration object
         * @param stateSupplier service state supplier
         */
        public TransportFixedRateSource(Supplier<D> dataCreator, TransportFixedRateParams params, 
            DataIngestor<D> ingestor, Supplier<ServiceState> stateSupplier) {
            super(dataCreator, params, ingestor, stateSupplier);
            this.connector = params.getConnector();
            this.channel = params.getChannel();
        }

        @Override
        protected void disconnect() throws IOException {
            connector.disconnect();
        }

        /**
         * Sends on the transport connector, logging exceptions.
         * 
         * @param channel the channel to send on
         * @param data the data to send
         */
        protected void sendSafe(String channel, Object data) {
            try {
                send(channel, data);
            } catch (IOException e) {
                LoggerFactory.getLogger(this).error("While sending data: {}", e.getMessage());
            }
        }

        /**
         * Sends on the transport connector.
         * 
         * @param channel the channel to send on
         * @param data the data to send
         * @throws IOException if sending fails
         */
        protected void send(String channel, Object data) throws IOException {
            connector.asyncSend(channel, data);
        }

        @Override
        protected void ingest(D data) throws IOException {
            connector.asyncSend(channel, data);
        }
        
    }

}
