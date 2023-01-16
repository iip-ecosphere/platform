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

package de.iip_ecosphere.platform.services.environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.IipVersion;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import de.iip_ecosphere.platform.transport.streams.StreamNames;

/**
 * Logs transport messages for debugging. Must be executed within an environment where a 
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportLogger {
    
    private static EnvironmentSetup setup;
    private static TransportConnector conn;
    private static Map<String, TransportHandler<?>> handlers = new HashMap<>();
    private static Consumer<EnvironmentSetup> setupCustomizer = s -> { };
    private static BiConsumer<Category, String> receptionConsumer = (c, s) -> { };
    private static Supplier<Boolean> loopEndSupplier = () -> true;
    private static Runnable shutdownRunnable = () -> shutdown();
    private static PrintStream fileOut = null;
    
    /**
     * Implements a generic transport handler for logging.
     * 
     * @param <T> the type of data to be handled
     * @author Holger Eichelberger, SSE
     */
    protected static class TransportHandler<T> implements ReceptionCallback<T> {
        
        private String stream;
        private Class<T> cls;
        private Consumer<T> cons;
        
        /**
         * Creates a handler.
         * 
         * @param stream the transport stream to listen to
         * @param cls the class of data to listen to
         * @param cons the consumer to be called
         */
        protected TransportHandler(String stream, Class<T> cls, Consumer<T> cons) {
            this.stream = stream;
            this.cls = cls;
            this.cons = cons;
        }

        /**
         * Initializes the handler.
         * 
         * @throws IOException if registering the handler as reception callback fails
         */
        public void initialize() throws IOException {
            conn.setReceptionCallback(stream, this);
        }

        /**
         * Detaches the handler from {@code Transport}.
         * 
         * @throws IOException if detaching the handler as reception callback fails
         */
        public void detach() throws IOException {
            cons = null;
            //conn.detachReceptionCallback(stream, this); // TODO fails with Qpid-J
        }

        @Override
        public void received(T data) {
            if (null != cons) {
                cons.accept(data);
            }
        }

        @Override
        public Class<T> getType() {
            return cls;
        }

    }
    
    /**
     * Adds a transport handler.
     * 
     * @param arg the argument name to activate the handler (ignored if <b>null</b>)
     * @param handler the handler (ignored if <b>null</b>)
     */
    protected static void addHandler(String arg, TransportHandler<?> handler) {
        if (null != arg && null != handler) {
            handlers.put(arg, handler);
        }
    }

    /**
     * Adds a transport handler.
     * 
     * @param <T> the type to be handled
     * @param arg the argument name to activate the handler
     * @param stream the stream to handle
     * @param cls the data type on {@code stream} to handle
     * @param cons the consumer for received data
     */
    protected static <T> void addHandler(String arg, String stream, Class<T> cls, Consumer<T> cons) {
        addHandler(arg, new TransportHandler<>(stream, cls, cons));
    }
    
    /**
     * Generic toString method.
     * 
     * @param obj the object to be turned into a string
     * @return the formatted string
     */
    protected static String toString(Object obj) {
        return ReflectionToStringBuilder.toString(obj, IipStringStyle.SHORT_STRING_STYLE);
    }
    
    /**
     * Defines the setup customizer. [testing]
     * 
     * @param customizer the customizer, ignored if <b>null</b>
     */
    public static void setSetupCustomizer(Consumer<EnvironmentSetup> customizer) {
        if (null != customizer) {
            setupCustomizer = customizer;
        }
    }
    
    /**
     * Defines an optional consumer to be called on data reception. [testing]
     * 
     * @param consumer the consumer, may be <b>null</b> for none
     */
    public static void setReceptionConsumer(BiConsumer<Category, String> consumer) {
        receptionConsumer = consumer;
    }

    /**
     * Changes the default shutdown runnable. [testing]
     * 
     * @param runnable the runnable, ignored if <b>null</b>
     */
    public static void setShutdownRunnable(Runnable runnable) {
        if (null != runnable) {
            shutdownRunnable = runnable;
        }
    }

    /**
     * Defines an optional loop end supplier (default constant {@code true}). [testing]
     * 
     * @param supplier the supplier, ignored if <b>null</b>
     */
    public static void setLoopEndSupplier(Supplier<Boolean> supplier) {
        if (null != supplier) {
            loopEndSupplier = supplier;
        }
    }
    
    /**
     * The output data category.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum Category {
        
        /**
         * Trace data.
         */
        TRACE,

        /**
         * Platform status data.
         */
        STATUS,

        /**
         * Platform monitoring/metrics data.
         */
        METRICS
    }
    
    /**
     * Logs the output. 
     * 
     * @param category the data category
     * @param text the text representing the data
     */
    private static void log(Category category, String text) {
        System.out.println(category + " " + text);
        if (null != fileOut) {
            fileOut.println(category + " " + text);
        }
        if (null != receptionConsumer) {
            receptionConsumer.accept(category, text);
        }
    }

    /**
     * Starts the transport logger. Three default streams are taken into account, namely the 
     * optional service traces, the platform status and the platform monitoring metrics. By default,
     * the setup is taken from the application's "application.yml", but on platform level it may be 
     * needed to define the {@code setupFile}.
     * 
     * @param args command line arguments, e.g., --traces=true, --setupFile=iipecosphere.yml, 
     *     --status=true, --metrics=true
     */
    public static void main(String[] args) {
        addHandler("traces", TraceRecord.TRACE_STREAM, TraceRecord.class, 
            r -> log(Category.TRACE, toString(r)));
        addHandler("status", StatusMessage.STATUS_STREAM, StatusMessage.class, 
            s -> log(Category.STATUS, toString(s)));
        addHandler("metrics", StreamNames.RESOURCE_METRICS, String.class, 
            m -> log(Category.METRICS, m));

        try {
            System.out.println("IIP-Ecosphere transport message logger (" 
                + IipVersion.getInstance().getVersion() + ")");
            System.out.println(" --traces=true enables reception/output of service traces (if enabled on services)");
            System.out.println(" --status=true enables reception/output of platform status messages");
            System.out.println(" --metrics=true enables reception/output of platform monitoring messages");
            System.out.println(" --setupFile=<resource/file> defines setup file, also -DsetupFile=<file> "
                + "(default application.yml)");
            System.out.println(" --outFile=<file> writes log also to file (default none)");
            
            String file = CmdLine.getArg(args, "setupFile", System.getProperty("setupFile", "application.yml"));
            System.out.println("Using setup file: " + file);
            InputStream in = ResourceLoader.getResourceAsStream(file, new FolderResourceResolver(new File(".")));
            setup = EnvironmentSetup.readFromYaml(EnvironmentSetup.class, in);
            setupCustomizer.accept(setup);
            Transport.setTransportSetup(() -> setup.getTransport());
            
            file = CmdLine.getArg(args, "outFile", null);
            if (null != file) {
                try {
                    fileOut = new PrintStream(new FileOutputStream(file));
                } catch (IOException e) {
                    System.out.println("Cannot open output stream to '" + file + "'. Discarding output. Reason: " 
                        + e.getMessage());
                }
            }
            
            conn = Transport.createConnector();
            for (String arg: handlers.keySet()) {
                if (CmdLine.getBooleanArg(args, arg, false)) {
                    System.out.println("Enabling handler for " + arg);
                    handlers.get(arg).initialize();
                }
            }
            Runtime.getRuntime().addShutdownHook(new Thread(shutdownRunnable));
            System.out.println("Receiving transport messages until Ctrl-C");
            do {
                TimeUtils.sleep(500);
            } while (loopEndSupplier.get());
            
            // cleanup (also for testing)
            for (TransportHandler<?> h : handlers.values()) {
                h.detach();
            }
            handlers.clear();
            if (null != fileOut) {
                fileOut.flush();
                FileUtils.closeQuietly(fileOut);
                fileOut = null;
            }
        } catch (IOException e) {
            System.out.println("Cannot read environment/transport setup. Started from within app?");
        }
    }

    /**
     * Shuts down this logger and disconnects from {@code Transport}. [testing]
     */
    public static void shutdown() {
        if (null != conn) {
            Transport.releaseConnector(false);
            conn = null;
        }
    }

}
