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

package de.iip_ecosphere.platform.connectors.model;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;

/**
 * Basic implementation of the model access.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractModelAccess implements ModelAccess {

    private boolean detailNotifications = false; 
    private Boolean useNotifications;
    private NotificationChangedListener notificationChangedListener;

    /**
     * Listener for notification changes.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface NotificationChangedListener {

        /**
         * Called when the notifications setting has been changed in 
         * {@link AbstractModelAccess#useNotifications(boolean)}.
         * 
         * @param useNotifications the new value after changing
         */
        public void notificationsChanged(boolean useNotifications);
        
    }

    /**
     * Creates an abstract model access with notification changed listener.
     * 
     * @param notificationChangedListener listener to be called when the notification settings
     *   have been changed, typically during initialization of the connector/model 
     */
    protected AbstractModelAccess(NotificationChangedListener notificationChangedListener) {
        this.notificationChangedListener = notificationChangedListener;
    }

    /**
     * Returns whether detailed notifications for monitored items is enabled.
     * 
     * @return {@code true} for details, {@code false} for <b>null</b>
     */
    protected final boolean isDetailNotifiedItemEnabled() {
        return detailNotifications;
    }
    
    @Override
    public final void setDetailNotifiedItem(boolean detail) {
        detailNotifications = detail;
    }
    
    /**
     * Returns whether (event-based) notifications or polling shall be used.
     * 
     * @return {@code true} for notifications, {@code false} for polling
     */
    protected final boolean useNotifications() {
        return null == useNotifications ? false : useNotifications;
    }
    
    @Override
    public final void useNotifications(boolean useNotifications) {
        Boolean old = this.useNotifications;
        this.useNotifications = useNotifications;
        if ((null == old || old != useNotifications) && notificationChangedListener != null) {
            notificationChangedListener.notificationsChanged(useNotifications);
        }
    }
    
    /**
     * Composes a qualified name.
     * 
     * @param init the initialization for the result to be used when the first non-empty name shall be appended
     * @param names the names to be appended
     * @return the qualified name, empty if there was nothing to compose irrespective of {@code init} 
     */
    private String qName(String init, String... names) {
        String result = "";
        String sep = getQSeparator();
        for (int n = 0; n < names.length; n++) {
            if (names[n].length() > 0) {
                if (result.length() == 0) {
                    result = init;
                }
                if (result.length() > 0) {
                    result += sep;
                }
                result += names[n];
            }
        }
        return result;
    }
    
    @Override
    public String qName(String... names) {
        return qName("", names);
    }

    @Override
    public String iqName(String... names) {
        return qName(topInstancesQName(), names);
    }

    @Override
    public void monitor(String... qNames) throws IOException {
        monitor(getConnectorParameter().getNotificationInterval(), qNames);
    }

    @Override
    public void monitorModelChanges() throws IOException {
        monitorModelChanges(getConnectorParameter().getNotificationInterval());
    }

    /**
     * Returns the actual connector parameters that apply for this model instance.
     * 
     * @return the connector parameters
     */
    protected abstract ConnectorParameter getConnectorParameter();
    
}
