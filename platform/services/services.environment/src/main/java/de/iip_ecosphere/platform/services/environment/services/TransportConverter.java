/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.function.IOConsumer;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;

/**
 * Implements a generic converter from transport stream entries to some protocol.
 * 
 * @param <T> the data type
 * @author Holger Eichelberger, SSE
 */
public abstract class TransportConverter<T> {

    protected static final String PREFIX_GETTER = "get";

    private TraceRecordReceptionCallback callback;
    private List<IOConsumer<T>> notifier = new ArrayList<>();
    
    private String transportStream;
    private Class<T> dataType;
    private Supplier<Boolean> aasEnabledSupplier = () -> true;

    /**
     * Creates a service instance.
     *
     * @param transportStream the transport stream to be observed (name of the stream)
     * @param dataType the type of the data in the transport stream
     */
    public TransportConverter(String transportStream, Class<T> dataType) {
        this.transportStream = transportStream;
        this.dataType = dataType;
    }
    
    /**
     * Defines a new notifier which is called when new data arrives. Currently only one notifier is supported.
     * 
     * @param notifier the notifier, ignored if <b>null</b>
     */
    public void addNotifier(IOConsumer<T> notifier) {
        if (null != notifier) {
            this.notifier.add(notifier);
        } else {
            LoggerFactory.getLogger(getClass()).warn("No notifier given. Ignoring call.");
        }
    }
    
    /**
     * Handles a new trace record. Called upon arrival. [protected for mocking]
     * 
     * @param data the trace record data
     */
    protected abstract void handleNew(T data);
    
    /**
     * Handles a new trace record by calling {@link #handleNew(Object)} upon arrival. Entry point for handling new
     * data, calling {@link #handleNew(Object)} to ensure that {@link #notifier} are processed.
     * 
     * @param data the trace record data
     */
    private void handleNewAndNotify(T data) {
        for (IOConsumer<T> n: notifier) {
            try { // notify independently
                n.accept(data);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot inform notifier: {}", e.getMessage());
            }
        }
        handleNew(data);
    }
    
    /**
     * Returns whether a payload entry for a given field in a given class shall be created. Large entries may cause 
     * AAS performance/memory issues.
     * 
     * @param fieldName the field name
     * @param fieldType the declared field type (real value may be a sub-type)
     * @param cls the class declaring the field
     * @return {@code true} for creating a payload entry, {@code false} else
     */
    protected boolean createPayloadEntry(String fieldName, Class<?> fieldType, Class<?> cls) {
        return true;
    }

    /**
     * Obtains the value return by {@code method} on {@code payload}.
     * 
     * @param object the object to call the method on
     * @param method the method to call
     * @param field the represented field (for logging)
     * @return the value of {@code field} via {@code method}
     */
    protected Object getValue(Object object, Method method, String field) {
        Object result;
        try {
            result = method.invoke(object);
        } catch (SecurityException | InvocationTargetException | IllegalAccessException e) {
            result = null;
            LoggerFactory.getLogger(getClass()).error(
                "Cannot obtain value of operation {}/field {} of class {} to AAS: {}", 
                method.getName(), field, method.getDeclaringClass().getName(), e.getMessage());
        }
        return result;
    }
    
    /**
     * Allows for application specific payload type names.
     * 
     * @param cls the type
     * @return the mapped name
     */
    protected String mapPayloadType(Class<?> cls) {
        return cls.getName();
    }
    
    /**
     * Returns whether {@code method} is an usual getter.
     * 
     * @param method the method to analyze
     * @return {@code true} for getter, {@code false} else
     */
    protected static boolean isGetter(Method method) {
        int modifier = method.getModifiers();
        boolean pubNonStatic = Modifier.isPublic(modifier) && !Modifier.isStatic(modifier);
        return method.getName().startsWith(PREFIX_GETTER) && method.getParameterCount() == 0 && pubNonStatic; 
    }
    
    /**
     * A trace reception callback calling {@link TransportConverter#handleNew(Object)} 
     * in own threads.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class TraceRecordReceptionCallback implements ReceptionCallback<T> {
        
        @Override
        public void received(T data) {
            new Thread(() -> handleNewAndNotify(data)).start(); // thread pool?
        }

        @Override
        public Class<T> getType() {
            return dataType;
        }
        
    }
    
    /**
     * Watches for updates.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Watcher {
        
        /**
         * Starts the watcher.
         * 
         * @return <b>this</b>
         */
        public Watcher start();

        /**
         * Stops the watcher.
         * 
         * @return <b>this</b>
         */
        public Watcher stop();

    }
    
    /**
     * Returns the handled data type.
     * 
     * @return the type
     */
    protected final Class<T> getType() {
        return dataType;
    }

    /**
     * Starts the transport tracer.
     * 
     * @param aasSetup the AAS setup to use
     * @param deploy whether the AAS represented by this converter shall be deployed
     */
    public void start(AasSetup aasSetup, boolean deploy) {
        callback = new TraceRecordReceptionCallback();
        TransportConnector conn = Transport.createConnector();
        if (null != conn) {
            try {
                conn.setReceptionCallback(transportStream, callback);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Registring transport callback: " + e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(getClass()).error("No transport setup, will not listen to trace records.");
        }
    }
    
    /**
     * Returns whether the AAS functionality shall be enabled.
     * 
     * @return {@code true} for enabled, {@code false} else
     */
    protected boolean isAasEnabled() {
        return aasEnabledSupplier.get();
    }

    /**
     * Changes the optional supplier for the AAS enabled state.
     * 
     * @param enabledSupplier the new supplier, ignored if <b>null</b>
     */
    public void setAasEnabledSupplier(Supplier<Boolean> enabledSupplier) {
        if (null != enabledSupplier) {
            this.aasEnabledSupplier = enabledSupplier;
        }
    }

    /**
     * Returns whether the AAS was started/startup is done.
     * 
     * @return {@code true} for started, {@code false} else
     */
    public boolean isAasStarted() {
        return true;
    }
    
    /**
     * Changes the timeout until trace events are deleted.
     * 
     * @param timeout the timeout in ms
     */
    public void setTimeout(long timeout) {
        // ignored
    }

    /**
     * Changes the cleanup timeout, i.e., the time between two cleanups.
     * 
     * @param cleanupTimeout the timeout in ms
     */
    public void setCleanupTimeout(long cleanupTimeout) {
        // ignored
    }
    
    /**
     * Pursues a cleanup of the internal data structures, if applicable.
     * 
     * @return whether a cleanup process was executed (not whether elements were deleted)
     */
    public boolean cleanup() {
        return true; // pretend success
    }

    /**
     * Stops the transport, deletes the AAS.
     */
    public void stop() {
        try {
            TransportConnector conn = Transport.getConnector();
            if (null != conn) {
                conn.detachReceptionCallback(transportStream, callback);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Detaching transport connector: " + e.getMessage());
        }
    }
    
    /**
     * Creates a watcher instance.
     * 
     * @param period the watching period in ms
     * @return the watcher
     */
    public abstract Watcher createWatcher(int period);

}
