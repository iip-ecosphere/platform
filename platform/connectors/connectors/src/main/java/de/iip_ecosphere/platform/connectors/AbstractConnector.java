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

package de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess.NotificationChangedListener;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

/**
 * Provides a reusable base of a {@link Connector} implementation using the {@link ProtocolAdapter}. Call 
 * {@link #initializeModelAccess()} on {@link #connect(ConnectorParameter)} as soon as the connector is connected. 
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * @param <D> the model data type (see @link {@link ModelAccess})
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractConnector<O, I, CO, CI, D> implements Connector<O, I, CO, CI, D>, 
    NotificationChangedListener {

    private ProtocolAdapter<O, I, CO, CI, D> adapter;
    private ReceptionCallback<CO> callback;
    private Timer timer;
    private TimerTask pollTask;
    private ConnectorParameter params;

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter
     */
    protected AbstractConnector(ProtocolAdapter<O, I, CO, CI, D> adapter) {
        this.adapter = adapter;
    }
    
    /**
     * Connects the connector to the underlying machine/platform. Calls {@link #connectImpl(ConnectorParameter)} 
     * and if successful (no exception thrown) {@link #initializeModelAccess()}. 
     * 
     * @param params connection parameter
     * @throws IOException in case that connecting fails
     */
    @Override
    public void connect(ConnectorParameter params) throws IOException {
        this.params = params;
        connectImpl(params);
        initializeModelAccess();
    }
    
    /**
     * Implements the {@link #connect(ConnectorParameter)} method assuming that everything
     * is ok if no exception have been thrown.
     *  
     * @param params connection parameter
     * @throws IOException in case that connecting fails
     */
    protected abstract void connectImpl(ConnectorParameter params) throws IOException;
    
    /**
     * Returns the connector parameters after the last {@link #connect(ConnectorParameter)}.
     * 
     * @return the connector params
     */
    protected ConnectorParameter getConnectorParameter() {
        return params;
    }
    
    /**
     * Install poll task. No task will be installed if {@link ConnectorParameter#getNotificationInterval()} is 
     * less than 1. Call only after {@link #connect(ConnectorParameter)} and before {@link #disconnect()}.
     */
    protected void installPollTask() {
        int pollingPeriod = params.getNotificationInterval();
        if (null == timer && pollingPeriod > 0) {
            timer = new Timer();
            pollTask = new TimerTask() {

                @Override
                public void run() {
                    try {
                        O data = read();
                        if (null != data) {
                            received(data);
                        }
                    } catch (IOException e) {
                        error("While polling. Data discarded.", e);
                    }
                }
                
            };
            timer.scheduleAtFixedRate(pollTask, 0, pollingPeriod);
        }
    }

    /**
     * Uninstall poll task.
     */
    protected void uninstallPollTask() {
        if (null != pollTask) {
            pollTask.cancel();
        }
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }
    
    @Override
    public void write(CI data) throws IOException {
        writeImpl(adapter.adaptInput(data));
    }

    /**
     * Does the actual writing to the underlying machine/platform. Can be left empty if 
     * {@link MachineConnector#hasModel()}.
     * 
     * @param data the data to be send
     * @throws IOException if sending fails
     */
    protected abstract void writeImpl(I data) throws IOException;

    /**
     * Call this if data was received. 
     * 
     * @param data the received data, further processed if {@link #callback} is not <b>null</b>
     * @throws IOException if receiving/translation fails
     */
    protected void received(O data) throws IOException {
        if (null != callback) {
            callback.received(adapter.adaptOutput(data));
        }
    }
    
    @Override
    public void setReceptionCallback(ReceptionCallback<CO> callback) throws IOException {
        this.callback = callback;
    }
    
    /**
     * Reads data from the underlying machine. Used for polling, but shall then be implemented by returning 
     * at least a dummy object so that the {@link #installPollTask(ConnectorParameter) polling task} can initiate
     * a translation request and forward it to the {@link #callback}. In particular, can be a dummy object or
     * the actual changes in the model if {@link MachineConnector#hasModel()}. 
     * 
     * @return the data from the machine, <b>null</b> for none, i.e., also no call to {@link #callback}
     * @throws IOException in case that reading fails
     */
    protected abstract O read() throws IOException;
    
    /**
     * Logs an error.
     * 
     * @param message the message to log
     * @param th information about the error
     */
    protected abstract void error(String message, Throwable th);
    
    /**
     * Called to initialize the model access, e.g., to setup notifications. Shall be called only, when the connector is 
     * connected.
     * 
     * @throws IOException in case the initialization fails, e.g., monitors cannot be set up
     */
    protected void initializeModelAccess() throws IOException {
        adapter.initializeModelAccess();
    }

    /**
     * Called when the notifications setting has been changed in {@link #useNotifications(boolean)}.
     * 
     * @param useNotifications the new value after changing
     */
    public void notificationsChanged(boolean useNotifications) {
        if (useNotifications) {
            uninstallPollTask(); // nothing happens if there is no poll task, otherwise cancel it
        } else {
            installPollTask();
        }        
    }

}
