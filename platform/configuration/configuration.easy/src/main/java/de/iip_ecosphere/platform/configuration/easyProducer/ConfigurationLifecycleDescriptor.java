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

package de.iip_ecosphere.platform.configuration.easyProducer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.configuration.cfg.StatusCache;
import de.iip_ecosphere.platform.configuration.easyProducer.EasyLogger.LogConsumer;
import de.iip_ecosphere.platform.configuration.easyProducer.EasyLogger.LogLevel;
import de.iip_ecosphere.platform.services.environment.services.Sender;
import de.iip_ecosphere.platform.services.environment.services.TransportConverterFactory;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;
import de.uni_hildesheim.sse.easy.loader.ManifestLoader;
import de.uni_hildesheim.sse.easy.loader.framework.Log;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.ILogger;
import net.ssehub.easy.basics.logger.LoggingLevel;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.execution.IInstantiatorTracer;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.instantiation.core.model.templateModel.ITracer;
import net.ssehub.easy.instantiation.core.model.tracing.ConsoleTracerFactory;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;

/**
 * The lifecycle descriptor for the configuration component. The default execution mode is now 
 * {@link ExecutionMode#IVML} as this lifecycle descriptor is intended to boot the platform service, which, 
 * through the UI, only needs IVML rather than VIL/VTL. If the caller needs more configuration abilities, please use
 * {@link ExecutionMode#TOOLING} or {@link ExecutionMode#FULL} with {@link #setExecutionMode(ExecutionMode)} before
 * calling {@link #startup(String[])}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationLifecycleDescriptor implements LifecycleDescriptor {

    public static final boolean INCREMENTAL 
        = OsUtils.getBooleanProperty(PlatformInstantiator.KEY_PROPERTY_INCREMENTAL, false);
    private static Set<String> noEasyLogging = new HashSet<>();

    private ManifestLoader loader;
    private boolean doLogging = true;
    private boolean doFilterLogs = false;
    private ClassLoader classLoader = ConfigurationLifecycleDescriptor.class.getClassLoader();
    private ExecutionMode executionMode = ExecutionMode.IVML;
    private SenderCloseable logSender = null;
    
    static {
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.vilTypes.TypeRegistry.class);
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.artifactModel.ArtifactFactory.class);
        noEasyLogging.add("net.ssehub.easy.varModel.management.DefaultImportResolver");
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.templateModel.TemplateLangExecution.class);
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.templateModel.Template.class);
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.buildlangModel.Script.class);
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.expressions.ExpressionParserRegistry.class);
        addNoEasyLogging(net.ssehub.easy.varModel.confModel.AssignmentResolver.class);
    }
    
    /**
     * Registers the name of {@code cls} for not-logging EASY-producer logging messages.
     * 
     * @param cls the class to register
     */
    private static void addNoEasyLogging(Class<?> cls) {
        noEasyLogging.add(cls.getName());
    }
    
    /**
     * Returns whether EASy-Logging is disabled on the given class name.
     * 
     * @param cls the class name
     * @return {@code true} for disabled, {@code false} else
     */
    public static boolean isEasyLoggingDisabled(String cls) {
        return null != cls && noEasyLogging.contains(cls);
    }
    
    /**
     * Defines execution modes implying logging, model loading setup etc.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum ExecutionMode {
        
        /**
         * Full execution, the default.
         */
        FULL(true),
        
        /**
         * Run IVML only. Mode for the platform server.
         */
        IVML(true),

        /**
         * Run IVML only. Mode for the platform server.
         */
        IVML_QUIET(false),

        /**
         * Run in tooling, e.g., Maven.
         */
        TOOLING(false);
        
        private boolean exendedLogging;
        
        /**
         * Creates a mode value.
         * 
         * @param extendedLogging whether extended logging is desired
         */
        private ExecutionMode(boolean extendedLogging) {
            this.exendedLogging = extendedLogging;
        }
        
        /**
         * Returns whether extended logging is desired.
         * 
         * @return {@code true} for extended logging, {@code false} else
         */
        public boolean extendedLogging() {
            return exendedLogging;
        }
        
    }
    
    /**
     * SLF4J-to-EASy logging adapter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class Slf4EasyLogger extends EasyLogger {
        
        /**
         * Creates a logger instance that logs to the oktoflow logger of this class.
         */
        public Slf4EasyLogger() {
            super(getLogger());
        }

        @Override
        protected boolean allowLogging(String msg, Class<?> clazz, String bundleName, LogLevel level) {
            boolean emit = doLogging;
            if (doFilterLogs && (LogLevel.ERROR != level && LogLevel.WARN != level)) { // limit main decision override 
                emit = clazz == EasyExecutor.class;
            }
            if (emit) { // emit warn/error anyway
                emit = super.allowLogging(msg, clazz, bundleName, level);
            }
            if (emit && clazz.getSimpleName().equals("YamlFileArtifact") && msg.contains("While reading") 
                && msg.contains(File.separator + "resources")) {
                emit = false; // yml
            }
            return emit;
        }

    }
    

    /**
     * Mapping EASy executor logger information into this logger.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ExecLogger implements net.ssehub.easy.producer.core.mgmt.EasyExecutor.Logger {

        @Override
        public void warn(String text) {
            getLogger().warn(text);
        }

        @Override
        public void error(String text) {
            ConfigurationSetup setup = ConfigurationSetup.getSetup();
            EasySetup eSetup = setup.getEasyProducer();
            getLogger().error("{} [base: {} model: {} meta: {} cfg: {} gen: {} additional: {}]", text, eSetup.getBase(),
                eSetup.getIvmlModelName(), eSetup.getIvmlMetaModelFolder(), eSetup.getIvmlConfigFolder(), 
                eSetup.getGenTarget(), eSetup.getAdditionalIvmlFolders());
        }

        @Override
        public void info(String text) {
        }
        
    }
    
    /**
     * Changes the execution mode. Must be called before {@link #startup(String[])}.
     * 
     * @param mode the new mode, ignored if <b>null</b>
     */
    public void setExecutionMode(ExecutionMode mode) {
        if (mode != null) {
            this.executionMode = mode;
        }
    }
    
    /**
     * Explicitly defines the class loader for loading EASy. Default is the class loader of this class. Must be called 
     * before {@link #startup(String[])}.
     * 
     * @param classLoader the class loader, ignored if <b>null</b>
     */
    public void setClassLoader(ClassLoader classLoader) {
        if (null != classLoader) {
            this.classLoader = classLoader;
        }
    }

    /**
     * Starts up in a given execution mode.
     * 
     * @param mode the new mode, ignored if <b>null</b>
     * @param args the command line arguments
     * @see #setExecutionMode(ExecutionMode)
     * @see #startup(String[])
     */
    public void startup(ClassLoader classLoader, ExecutionMode mode, String[] args) {
        setClassLoader(classLoader);
        startup(mode, args);
    }

    /**
     * Starts up in a given execution mode.
     * 
     * @param mode the new mode, ignored if <b>null</b>
     * @param args the command line arguments
     * @see #setExecutionMode(ExecutionMode)
     * @see #startup(String[])
     */
    public void startup(ExecutionMode mode, String[] args) {
        setExecutionMode(mode);
        startup(args);
    }
    
    /**
     * Sets the piggyback EASy log consumer.
     * 
     * @param consumer the consumer, may be <b>null</b> for none
     * @return the current EASy tracer factory
     */
    public static TracerFactory setLogConsumer(LogConsumer consumer) {
        TracerFactory orig = TracerFactory.getInstance(); // may be quiet
        ILogger logger = EASyLoggerFactory.INSTANCE.getLogger();
        if (logger instanceof EasyLogger) {
            ((EasyLogger) logger).setLogConsumer(consumer);
        }
        if (null != consumer) {
            Consumer<String> logConsumer = s -> consumer.log(LogLevel.TEXT, s);
            TracerFactory f = new TracerFactory() {

                @Override
                public ITracer createTemplateLanguageTracerImpl() {
                    ITracer result = ConsoleTracerFactory.INSTANCE.createTemplateLanguageTracerImpl();
                    result.setLogConsumer(logConsumer);
                    return result;
                }

                @Override
                public net.ssehub.easy.instantiation.core.model.buildlangModel.ITracer createBuildLanguageTracerImpl() {
                    net.ssehub.easy.instantiation.core.model.buildlangModel.ITracer result 
                        = ConsoleTracerFactory.INSTANCE.createBuildLanguageTracerImpl();
                    result.setLogConsumer(logConsumer);
                    return result;
                }

                @Override
                public IInstantiatorTracer createInstantiatorTracerImpl() {
                    IInstantiatorTracer result = ConsoleTracerFactory.INSTANCE.createInstantiatorTracerImpl();
                    result.setLogConsumer(logConsumer);
                    return result;
                }
                
            };
            TracerFactory.setDefaultInstance(f);
        }
        return orig;
    }
    
    /**
     * A closeable for the transport logger sender that also re-sets the EASY tracer factory.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class SenderCloseable {
        
        private Sender<String> sender;
        private TracerFactory factory;
        private TracerFactory origFactory;

        /**
         * Closes the sender, resets the factory.
         */
        public void close() {
            Sender.close(sender, false);
            if (null != origFactory) {
                TracerFactory.setDefaultInstance(origFactory);
            }
        }
        
    }
    
    /**
     * Tolerantly closes the given closeable.
     * 
     * @param closeable the closeable to close, may be <b>null</b>
     */
    public static void close(SenderCloseable closeable) {
        if (null != closeable) {
            closeable.close();
        }
    }

    /**
     * Sets the log consumer for transport logging.
     * 
     * @param logPath the log path
     * @return something that closes the sender/resets the state
     * @see Sender#close(Sender, boolean)
     */
    public static SenderCloseable setTransportLogConsumer(String logPath, ExecutionMode executionMode) {
        SenderCloseable result = new SenderCloseable();
        if (logPath != null && logPath.length() > 0 && executionMode != ExecutionMode.TOOLING) {
            ConfigurationSetup setup = ConfigurationSetup.getSetup();
            Sender<String> sender = TransportConverterFactory.getInstance().createSender(setup.getAas(), 
                setup.getTransport(), logPath, TypeTranslators.STRING, String.class);
            TracerFactory factory = null;
            try {
                sender.connectBlocking();
                getLogger().info("Setting up log consumer, connected to path {}", logPath);
                factory = setLogConsumer((l, m) -> {
                    try {
                        String prefix = "";
                        if (LogLevel.TEXT != l) {
                            prefix = l.name() + " ";
                        }
                        sender.send(prefix + m);
                    } catch (IOException e) {
                        getLogger().warn("Logging to transport: {}", e.getMessage());
                    }
                });
            } catch (IOException e) {
                getLogger().warn("Setting up transport logging: {}", e.getMessage());
            }
            result = new SenderCloseable();
            result.sender = sender;
            result.factory = TracerFactory.getDefaultInstance();
            result.origFactory = factory;
        }
        return result;
    }

    @Override
    public void startup(String[] args) {
        try {
            long t1 = System.currentTimeMillis();
            Slf4EasyLogger logger = new Slf4EasyLogger();
            Log.setLogger(logger);
            EASyLoggerFactory.INSTANCE.setLogger(logger);
            // pass through everything and let platform logger decide
            EASyLoggerFactory.INSTANCE.setLoggingLevel(LoggingLevel.INFO);
            String logPath = System.getProperty(PlatformInstantiatorExecutor.PROP_LOG_PATH, "");
            logSender = setTransportLogConsumer(logPath, executionMode);
            ConfigurationSetup setup = ConfigurationSetup.getSetup(executionMode.extendedLogging());
            loader = new ManifestLoader(false, classLoader); // to debug, replace first parameter by true, mvn install
            EasySetup easySetup = setup.getEasyProducer();
            loader.setVerbose(easySetup.getLogLevel() == EasyLogLevel.EXTRA_VERBOSE);
            getLogger().info("EASy-Producer is starting ({} mode)", executionMode);
            doFilterLogs = easySetup.getLogLevel() == EasyLogLevel.NORMAL;
            doLogging = !doFilterLogs;
            loader.startup();
            long t2 = System.currentTimeMillis();
            getLogger().info("EASy-Producer started in {} ms", t2 - t1);
            t1 = t2;
            doLogging = true;
            try {
                EasyExecutor exec = createExecutor(easySetup, executionMode, logSender);
                ConfigurationManager.setExecutor(exec);
                t2 = System.currentTimeMillis();
                getLogger().info("EASy-Producer models loaded in {} ms", t2 - t1);
                if (executionMode != ExecutionMode.TOOLING) {
                    t1 = t2;
                    StatusCache.start();
                    getLogger().info("Status cache started in {} ms", System.currentTimeMillis() - t1);
                }
            } catch (ModelManagementException e) {
                getLogger().error("Cannot set model locations. Configuration capabilities may be disabled. " 
                    + e.getMessage(), e);
            }
        } catch (IOException e) {
            getLogger().error("Cannot start EASy-Producer. Configuration capabilities may be disabled. " 
                + e.getMessage(), e);
        }
    }

    /**
     * Creates an EASy-Producer executor with the given execution mode taking the setup information 
     * from {@link ConfigurationSetup}. EASy-Producer must be loaded before, i.e., {@link #startup(String[])} must be
     * called before.
     * 
     * @param executionMode the execution mode
     * @param sender optional log sender, may be <b>null</b>
     * @return the EASy executor
     * @throws ModelManagementException when the executor cannot be created/models cannot be loaded
     */
    public static EasyExecutor createExecutor(ExecutionMode executionMode, SenderCloseable sender) 
        throws ModelManagementException {
        ConfigurationSetup setup = ConfigurationSetup.getSetup(false);
        EasySetup easySetup = setup.getEasyProducer();
        return createExecutor(easySetup, executionMode, sender);
    }

    /**
     * Cleans the output folder.
     */
    public static void cleanOutputFolder() {
        ConfigurationSetup setup = ConfigurationSetup.getSetup(false);
        // equip EASyExecutor with access to output folder?
        PlatformInstantiator.cleanOutputFolder(setup.getEasyProducer().getGenTarget());
    }
    
    /**
     * Creates an EASy-Producer executor with the given execution mode taking the setup information 
     * from {@link ConfigurationSetup}. EASy-Producer must be loaded before, i.e., {@link #startup(String[])} must be
     * called before or this call may be issued within {@link #startup(String[])}.
     * 
     * @param easySetup easy producer setup
     * @param executionMode the execution mode
     * @param sender optional log sender, may be <b>null</b>
     * @return the EASy executor
     * @throws ModelManagementException when the executor cannot be created/models cannot be loaded
     */
    public static EasyExecutor createExecutor(EasySetup easySetup, ExecutionMode executionMode, SenderCloseable sender) 
        throws ModelManagementException {
        EasyExecutor.enablePrepareArtifactsDefault(INCREMENTAL);
        EasyExecutor.enableIncrementalInstantiation(INCREMENTAL);
        if (INCREMENTAL) { // not always
            getLogger().info("Setting up incremental build mode: {}", INCREMENTAL);
        }
        getLogger().info("Setting up configuration base: {}", easySetup.getBase());
        getLogger().info("Setting up configuration meta model: {}", easySetup.getIvmlMetaModelFolder());
        getLogger().info("Setting up configuration model name: {}", easySetup.getIvmlModelName());
        EasyExecutor exec = new EasyExecutor(
            easySetup.getBase(), 
            easySetup.getIvmlMetaModelFolder(), 
            easySetup.getIvmlModelName());
        if (null != sender) {
            if (null != sender.factory) {
                exec.setTracerFactory(sender.factory);
            }
        }        
        exec.setLogger(new ExecLogger());
        // VIL model name is fix, IVML/Configuration name may change
        exec.setVilModelName(EasySetup.PLATFORM_META_MODEL_NAME);
        // self-instantiation into gen, assumed to be empty, may be cleaned up
        //exec.setVtlFolder(new File(setup.getIvmlMetaModelFolder(), "vtl")); // can be, but not needed
        getLogger().info("Setting up generation target: {}", easySetup.getGenTarget());
        exec.setVilSource(easySetup.getGenTarget());
        exec.setVilTarget(easySetup.getGenTarget());
        if (ExecutionMode.IVML == executionMode) { // disable VIL/VTL in IVML execution mode
            exec.setVilFolder(null);
            exec.setVtlFolder(null);
        }
        File ivmlCfg = easySetup.getIvmlConfigFolder();
        if (null != ivmlCfg && !ivmlCfg.equals(easySetup.getIvmlMetaModelFolder())) {
            getLogger().info("Setting up configuration folder: {}", ivmlCfg);
            exec.prependIvmlFolder(ivmlCfg);
        }
        if (null != easySetup.getAdditionalIvmlFolders()) {
            for (File f : easySetup.getAdditionalIvmlFolders()) {
                getLogger().info("Setting up additional configuration folder: {}", f);
                exec.addIvmlFolder(f);
            }
        }
        getLogger().info("Setting up EASy-Producer locations, loading models");
        exec.setupLocations();
        return exec;
    }

    @Override
    public void shutdown() {
        close(logSender);
        logSender = null;
        StatusCache.stop();
        EasyExecutor exec = ConfigurationManager.getExecutor();
        if (null != exec) {
            try {
                exec.discardLocations();
            } catch (ModelManagementException e) {
                getLogger().error("Cannot set model locations. Configuration capabilities may be disabled. " 
                    + e.getMessage(), e);
            }
        }
        
        exec.clearModels();
        
        ConfigurationManager.setExecutor(null);
        if (null != loader) {
            getLogger().info("EASy-Producer is stopping");
            doLogging = !doFilterLogs;
            loader.shutdown();
            doLogging = true;
            getLogger().info("EASy-Producer stopped");
        }
    }

    @Override
    public Thread getShutdownHook() {
        return null;
    }

    @Override
    public int priority() {
        return INIT_PRIORITY;
    }

    /**
     * Returns the logger for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ConfigurationLifecycleDescriptor.class);
    }
    
}
