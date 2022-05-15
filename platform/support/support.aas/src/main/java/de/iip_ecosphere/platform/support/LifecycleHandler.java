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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

/**
 * Aggregated methods for all known lifecycle descriptors. {@link LifecycleDescriptor} shall be declared via 
 * Java Service Loading. See also {@link TerminatingLifecycleDescriptor} and {@link PidLifecycleDescriptor}. 
 * Descriptors are loaded once so that instances can be considered to be singletons. Defines three (tested) 
 * default main programs that help avoiding repeated declaration of nearly empty starter classes.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LifecycleHandler {

    private static final String CMD_ARG = "--iip.profile=";
    private static String[] cmdArgs;
    
    /**
     * Default main program performing the steps, i.e., {@link LifecycleHandler#attachShutdownHooks()}, 
     * {@link LifecycleHandler#startup(String[])} and {@link LifecycleHandler#shutdown()} assuming
     * that all work is done in startup/shutdown.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class OneShotStarter {

        /**
         * Executes the program.
         * 
         * @param args the command line actions
         */
        public static void main(String[] args) {
            LifecycleHandler.attachShutdownHooks();
            LifecycleHandler.startup(args);
            LifecycleHandler.shutdown();
        }
        
    }
    
    /**
     * Default main program to wait endless or until a {@link TerminatingLifecycleDescriptor} stops the loop.
     *  
     * @author Holger Eichelberger, SSE
     */
    public static class WaitingStarter {
        
        /**
         * Executes the program through {@link LifecycleHandler#waitUntilEnd(String[])}.
         * 
         * @param args the command line actions
         */
        public static void main(String[] args) {
            waitUntilEnd(args);
        }
        
    }
    
    /**
     * Default main program to wait endless or until a {@link TerminatingLifecycleDescriptor} stops the loop.
     *  
     * @author Holger Eichelberger, SSE
     */
    public static class WaitingStarterWithShutdown {
        
        /**
         * Executes the program through {@link LifecycleHandler#waitUntilEnd(String[], boolean)} assuming that there
         * is a {@link TerminatingLifecycleDescriptor} so that {@link LifecycleHandler#shutdown()} can be executed.
         * 
         * @param args the command line actions
         */
        public static void main(String[] args) {
            waitUntilEnd(args, false);
        }
        
    }
    
    private static List<LifecycleDescriptor> descriptors;
    
    // checkstyle: stop exception type check
    
    /**
     * Calls {@link LifecycleDescriptor#startup(String[])} on all known descriptors.
     * 
     * @param args the command line arguments to be considered during startup
     */
    public static void startup(String[] args) {
        cmdArgs = args.clone();
        AtomicReference<String> pidFile = new AtomicReference<>();
        
        LifecycleProfile profile = DefaultProfile.INSTANCE;
        String profileName = DefaultProfile.NAME;
        for (int a = 0; a < args.length; a++) {
            if (args[a].startsWith(CMD_ARG)) {
                profileName = args[a].substring(CMD_ARG.length());
                break;
            }
        }
        if (!profileName.equals(DefaultProfile.NAME)) {
            Iterator<LifecycleProfile> iter = ServiceLoader.load(LifecycleProfile.class).iterator();
            while (iter.hasNext()) {
                LifecycleProfile p = iter.next();
                if (profileName.equals(p.getName())) {
                    profile = p;
                    break;
                }
            }
        }
        
        final LifecycleProfile activeProfile = profile;
        activeProfile.initialize(args);
        forEach(l -> {
            if (activeProfile.test(l.getClass())) {
                try {
                    LoggerFactory.getLogger(LifecycleHandler.class).info("Starting " + l.getClass().getName() 
                        + " (" + l.priority() + ")");
                    l.startup(args);
                    if (l instanceof PidLifecycleDescriptor && null == pidFile.get()) {
                        pidFile.set(((PidLifecycleDescriptor) l).getPidFileName());
                    }
                } catch (Throwable t) {
                    LoggerFactory.getLogger(LifecycleHandler.class).error("Startup failure in " 
                        + l.getClass().getName() + " with " + t.getMessage(), t);
                }
            }
        }, false);
        String pidFileName = pidFile.get();
        if (null != pidFileName) {
            try {
                PidFile.createInDefaultDir(pidFileName, true);
            } catch (IOException e) {
                LoggerFactory.getLogger(LifecycleHandler.class).warn("Cannot create PID file " 
                    + pidFileName + ": " + e.getMessage());
            }
        }
        LoggerFactory.getLogger(LifecycleHandler.class).info("Startup completed.");
    }
    
    /**
     * Calls {@link LifecycleDescriptor#shutdown()} on all known descriptors.
     */
    public static void shutdown() {
        forEach(l -> {
            try {            
                LoggerFactory.getLogger(LifecycleHandler.class).info("Stopping " + l.getClass().getName() 
                    + " (" + l.priority() + ")");
                l.shutdown();
            } catch (Throwable t) {
                LoggerFactory.getLogger(LifecycleHandler.class).error("Shutdown failure in " 
                        + l.getClass().getName() + " with " + t.getMessage(), t);
                
            }
        }, true);
    }

    // checkstyle: start exception type check

    /**
     * Collects and attaches the shutdown hooks of all known descriptors. 
     */
    public static void attachShutdownHooks() {
        forEach(l -> {
            Thread t = l.getShutdownHook();
            if (null != t) {
                Runtime.getRuntime().addShutdownHook(t);
            }
        }, true);
    }

    /**
     * Utility method to execute {@code consumer} on all currently known {@link LifecycleDescriptor descriptors}.
     * 
     * @param consumer the consumer
     * @param revert revert the sorting, i.e., lowest priority first
     */
    private static void forEach(Consumer<LifecycleDescriptor> consumer, boolean revert) {
        List<LifecycleDescriptor> desc = new ArrayList<>(getDescriptors()); // JDK 1.8

        Set<String> excluded = new HashSet<String>();
        for (LifecycleDescriptor d : desc) {
            LifecycleExclude exclude = d.getClass().getAnnotation(LifecycleExclude.class);
            if (null != exclude) {
                for (Class<?> c : exclude.value()) {
                    excluded.add(c.getName());
                }
                for (String s : exclude.names()) {
                    excluded.add(s);
                }
            }
        }
        
        int factor = revert ? -1 : 1;
        Collections.sort(desc, (d1, d2) -> factor * Integer.compare(d1.priority(), d2.priority()));
        for (LifecycleDescriptor d : desc) {
            if (!excluded.contains(d.getClass().getName())) {
                consumer.accept(d);
            }
        }
    }
    
    /**
     * Returns the descriptors as list.
     * 
     * @return the descriptors
     */
    private static List<LifecycleDescriptor> getDescriptors() {
        if (null == descriptors) {
            descriptors = CollectionUtils.toList(ServiceLoader.load(LifecycleDescriptor.class).iterator()); // JDK 1.8
        }
        return descriptors;
    }
    
    /**
     * Returns whether a main loop e.g. in {@link #waitUntilEnd(String[], boolean)} shall continue or not depending
     * on {@link TerminatingLifecycleDescriptor#continueWaiting()}.
     * 
     * @return {@code true} if waiting shall be continued, {@code false} else
     */
    public static boolean continueWaiting() {
        boolean cont = true;
        List<LifecycleDescriptor> desc = getDescriptors();
        for (int l = 0; cont && l < desc.size(); l++) {
            LifecycleDescriptor d = desc.get(l);
            if (d instanceof TerminatingLifecycleDescriptor) {
                cont = ((TerminatingLifecycleDescriptor) d).continueWaiting();
            }
        }
        return cont;
    }

    /**
     * Runs all lifecycle steps {@link #attachShutdownHooks()}, {@link #shutdown} as shutdown hook and 
     * {@link #startup(String[])}. Considers {@link TerminatingLifecycleDescriptor}.
     * 
     * @param args command line arguments to be passed to {@link #startup(String[])}
     * @see #waitUntilEnd(String[], boolean)
     * @see TerminatingLifecycleDescriptor
     */
    public static void waitUntilEnd(String[] args) {
        waitUntilEnd(args, true);
    }
    
    /**
     * Runs all lifecycle steps {@link #attachShutdownHooks()}, {@link #shutdown()} as specified and 
     * {@link #startup(String[])}.
     * 
     * @param args command line arguments to be passed to {@link #startup(String[])}
     * @param shutdownAsHook {@code true} if {@link #shutdown()} shall happen until waiting is not continued (through 
     *     {@link TerminatingLifecycleDescriptor}} or through a shutdown hook (may not be executed)
     * @see TerminatingLifecycleDescriptor
     */
    public static void waitUntilEnd(String[] args, boolean shutdownAsHook) {
        attachShutdownHooks();
        if (shutdownAsHook) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
        }
        startup(args);
        while (continueWaiting()) {
            TimeUtils.sleep(500);
        }
        if (!shutdownAsHook) {
            shutdown();
        }
    }
    
    /**
     * Returns the known descriptors.
     * 
     * @return the descriptors (unmodifiable)
     */
    public static List<LifecycleDescriptor> descriptors() {
        return Collections.unmodifiableList(getDescriptors());
    }
    
    /**
     * Returns any lifecycle descriptor of a certain type.
     * 
     * @param <L> the type of the lifecycle descriptor
     * @param cls the class describing the type
     * @return the descriptor (optional object)
     */
    public static <L extends LifecycleDescriptor> Optional<L> getAnyDescriptor(Class<L> cls) {
        return LifecycleHandler.descriptors()
            .stream()
            .filter(d -> cls.isInstance(d))
            .map(d -> cls.cast(d))
            .findAny();
    }
    
    /**
     * Returns the command line arguments as originally passed to the {@link LifecycleHandler}.
     * 
     * @return the command line arguments (may be <b>null</b> for none)
     */
    public static String[] getCmdArgs() {
        return null == cmdArgs ? null : cmdArgs.clone();
    }

}
