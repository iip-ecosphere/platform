/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Basic functions for active AAS with notification calls.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ActiveAasBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveAasBase.class);
    private static NotificationMode mode = NotificationMode.ASYNCHRONOUS;
    private static ExecutorService exec = Executors.newFixedThreadPool(5);

    /**
     * Supported notification modes.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum NotificationMode {
        
        /**
         * Parallel asynchronous notifications.
         */
        ASYNCHRONOUS,
        
        /**
         * Sequential, synchronous notifications, e.g., for testing.
         */
        SYNCHRONOUS,
        
        /**
         * No notifications, e.g., for testing.
         */
        NONE;
    }
    
    /**
     * Defines the interface for a notification processor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface NotificationProcessor {
        
        /**
         * Processes the notification.
         * 
         * @param submodel the primary submodel to be processed
         * @param aas the parent AAS (as the submodel does not have a link to it)
         */
        public void process(Submodel submodel, Aas aas);
    }

    /**
     * Processes a notification on a submodel of {@link AasPartRegistry#retrieveIipAas()} with the notification 
     * {@code #mode mode} set in this class.
     * 
     * @param subId the short id of the submodel
     * @param processor the processor to execute
     */
    public static void processNotification(String subId, NotificationProcessor processor) {
        processNotification(subId, mode, processor);
    }
    
    /**
     * Processes a notification on a submodel of {@link AasPartRegistry#retrieveIipAas()}.
     * 
     * @param subId the short id of the submodel
     * @param mode explicit notification mode to be used (if <b>null</b>, use {@link #mode})
     * @param processor the processor to execute
     */
    public static void processNotification(String subId, NotificationMode mode, NotificationProcessor processor) {
        if (null == mode) {
            mode = ActiveAasBase.mode;
        }
        if (mode != NotificationMode.NONE) {
            try {
                Aas aas = AasPartRegistry.retrieveIipAas();
                if (null != aas) {
                    Submodel submodel = aas.getSubmodel(subId);
                    if (null != submodel) {
                        if (NotificationMode.SYNCHRONOUS == mode) {
                            processor.process(submodel, aas);
                        } else {
                            exec.execute(() -> processor.process(submodel, aas));
                        }
                    }
                } else {
                    LOGGER.error("Cannot find submodel: " + subId);
                }
            } catch (IOException e) {
                LOGGER.error("While retrieving the IIP-Ecosphere AAS: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Changes the notification execution mode. [for testing]
     * 
     * @param mo the new mode
     * @return the last notification mode
     */
    public static NotificationMode setNotificationMode(NotificationMode mo) {
        NotificationMode old = mode;
        mode = mo;
        return old;
    }
    
    /**
     * Obtains a submodel of {@link AasPartRegistry#retrieveIipAas()}.
     * 
     * @param name the name of the submodel
     * @return the submodel
     * @throws IOException if the submodel cannot be found
     */
    public static Submodel getSubmodel(String name) throws IOException {
        Aas aas = AasPartRegistry.retrieveIipAas();
        if (null == aas) {
            throw new IOException("No IIP-AAS found");
        }
        Submodel submodel = aas.getSubmodel(name);
        if (null == submodel) {
            throw new IOException("No submodel '" + name + "' found");
        }
        return submodel;
    }

    
}
