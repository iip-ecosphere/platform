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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskData;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

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
     * @param mode explicit notification mode to be used if {@link #mode} is not {@link NotificationMode#NONE}. If 
     *     parameter is <b>null</b>, use {@link #mode} instead
     * @param processor the processor to execute
     */
    public static void processNotification(String subId, NotificationMode mode, NotificationProcessor processor) {
        if (ActiveAasBase.mode != NotificationMode.NONE) {
            if (null == mode) {
                mode = ActiveAasBase.mode;
            }
            try {
                Aas aas = AasPartRegistry.retrieveIipAas();
                if (null != aas) {
                    Submodel submodel = aas.getSubmodel(subId);
                    if (null != submodel) {
                        if (NotificationMode.SYNCHRONOUS == mode) {
                            processor.process(submodel, aas);
                        } else {
                            final TaskData data = TaskRegistry.getTaskData();
                            exec.execute(() -> {
                                TaskRegistry.associateTask(null, data);
                                processor.process(submodel, aas);
                                TaskRegistry.unassociateTask(null);
                            });
                        }
                    }
                } else {
                    if (!AasFactory.isNoInstanceWarningEmitted()) {
                        LOGGER.error("Cannot find submodel: " + subId);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("While retrieving the IIP-Ecosphere AAS: " + e.getMessage());
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

    /**
     * Clears a collection from elements responding to the given with the given predicate.
     * 
     * @param coll the collection to be cleared
     * @param pred the predicate
     */
    public static void clearCollection(SubmodelElementCollection coll, Predicate<SubmodelElementCollection> pred) {
        if (null != coll) {
            for (SubmodelElement e: coll.elements()) {
                if (e instanceof SubmodelElementCollection) {
                    SubmodelElementCollection ec = (SubmodelElementCollection) e;
                    if (pred.test(ec)) {
                        coll.deleteElement(e.getIdShort());
                    }
                }
            }
        }
    }

    /**
     * Returns a predicate, e.g., for {@link #clearCollection(SubmodelElementCollection, Predicate)} to delete the given
     * collection based on a property value.
     * 
     * @param propertyIdShort the property to look for
     * @param propertyValue the value to cause the deletion
     * @param failMessage if an exception occurs, the message lead in (will be followed by the exception message)
     * @return the predicate
     */
    public static Predicate<SubmodelElementCollection> createPropertyPredicate(String propertyIdShort, 
        Object propertyValue, String failMessage) {
        return s -> {
            boolean result = false;
            try {
                Property prop = s.getProperty(propertyIdShort);
                if (null != prop && propertyValue.equals(prop.getValue())) {
                    result = true;
                }
            } catch (ExecutionException ex) {
                LoggerFactory.getLogger(ActiveAasBase.class).error("{}: {} ", 
                    failMessage, ex.getMessage());
            }
            return result;
        };
    }

}
