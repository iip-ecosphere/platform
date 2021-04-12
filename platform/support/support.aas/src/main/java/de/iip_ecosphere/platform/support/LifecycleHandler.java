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

package de.iip_ecosphere.platform.support;

import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 * Aggregated methods for all known lifecycle descriptors. {@link LifecycleDescriptor} shall be declared via 
 * Java Service Loading.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LifecycleHandler {

    /**
     * Calls {@link LifecycleDescriptor#startup(String[])} on all known descriptors.
     * 
     * @param args the command line arguments to be considered during startup
     */
    public static void startup(String[] args) {
        forEach(l -> l.startup(args));
    }

    /**
     * Calls {@link LifecycleDescriptor#shutdown()} on all known descriptors.
     */
    public static void shutdown() {
        forEach(l -> l.shutdown());
    }

    /**
     * Collects and attaches the shutdown hooks of all known descriptors. 
     */
    public static void attachShutdownHooks() {
        forEach(l -> {
            Thread t = l.getShutdownHook();
            if (null != t) {
                Runtime.getRuntime().addShutdownHook(t);
            }
        });
    }

    /**
     * Utility method to execute {@code consumer} on all currently known {@link LifecycleDescriptor descriptors}.
     * 
     * @param consumer the consumer
     */
    private static void forEach(Consumer<LifecycleDescriptor> consumer) {
        ServiceLoader.load(LifecycleDescriptor.class).forEach(consumer);
    }
    
    /**
     * Runs all lifecycle steps {@link #attachShutdownHooks()}, {@link #shutdown} as shutdown hook and 
     * {@link #startup(String[])}. 
     * 
     * @param args command line arguments to be passed to {@link #startup(String[])}
     */
    public static void waitUntilEnd(String[] args) {
        attachShutdownHooks();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
        startup(args);
        while (true) {
            TimeUtils.sleep(500);
        }
    }

}
