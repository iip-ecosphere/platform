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

}
