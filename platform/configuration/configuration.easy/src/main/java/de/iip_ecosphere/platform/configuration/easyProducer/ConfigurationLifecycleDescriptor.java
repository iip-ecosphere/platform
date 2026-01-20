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

import de.iip_ecosphere.platform.configuration.cfg.StatusCache;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.uni_hildesheim.sse.easy.loader.ManifestLoader;
import de.uni_hildesheim.sse.easy.loader.framework.Log;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.LoggingLevel;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
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

    private static Set<String> noEasyLogging = new HashSet<>();

    private ManifestLoader loader;
    private boolean doLogging = true;
    private boolean doFilterLogs = false;
    private ClassLoader classLoader = ConfigurationLifecycleDescriptor.class.getClassLoader();
    private ExecutionMode executionMode = ExecutionMode.IVML;
    
    static {
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.vilTypes.TypeRegistry.class);
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.artifactModel.ArtifactFactory.class);
        noEasyLogging.add("net.ssehub.easy.varModel.management.DefaultImportResolver");
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.templateModel.TemplateLangExecution.class);
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.templateModel.Template.class);
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.buildlangModel.Script.class);
        addNoEasyLogging(net.ssehub.easy.instantiation.core.model.expressions.ExpressionParserRegistry.class);
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
            if (emit && level.ordinal() < LogLevel.WARN.ordinal()) { // emit warn/error anyway
                emit = !noEasyLogging.contains(clazz.getName());
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
    
    @Override
    public void startup(String[] args) {
        try {
            Slf4EasyLogger logger = new Slf4EasyLogger();
            Log.setLogger(logger);
            EASyLoggerFactory.INSTANCE.setLogger(logger);
            // pass through everything and let platform logger decide
            EASyLoggerFactory.INSTANCE.setLoggingLevel(LoggingLevel.INFO);
            ConfigurationSetup setup = ConfigurationSetup.getSetup(executionMode.extendedLogging());
            loader = new ManifestLoader(false, classLoader); // to debug, replace first parameter by true, mvn install
            EasySetup easySetup = setup.getEasyProducer();
            loader.setVerbose(easySetup.getLogLevel() == EasyLogLevel.EXTRA_VERBOSE);
            getLogger().info("EASy-Producer is starting ({} mode)", executionMode);
            doFilterLogs = easySetup.getLogLevel() == EasyLogLevel.NORMAL;
            doLogging = !doFilterLogs;
            loader.startup();
            doLogging = true;
            try {
                EasyExecutor exec = createExecutor(easySetup, executionMode);
                ConfigurationManager.setExecutor(exec);
                getLogger().info("EASy-Producer models loaded");
                StatusCache.start();
                getLogger().info("Status cache started");
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
     * @return the EASy executor
     * @throws ModelManagementException when the executor cannot be created/models cannot be loaded
     */
    public static EasyExecutor createExecutor(ExecutionMode executionMode) throws ModelManagementException {
        ConfigurationSetup setup = ConfigurationSetup.getSetup(false);
        EasySetup easySetup = setup.getEasyProducer();
        return createExecutor(easySetup, executionMode);
    }
    
    /**
     * Creates an EASy-Producer executor with the given execution mode taking the setup information 
     * from {@link ConfigurationSetup}. EASy-Producer must be loaded before, i.e., {@link #startup(String[])} must be
     * called before or this call may be issued within {@link #startup(String[])}.
     * 
     * @param executionMode the execution mode
     * @return the EASy executor
     * @throws ModelManagementException when the executor cannot be created/models cannot be loaded
     */
    public static EasyExecutor createExecutor(EasySetup easySetup, ExecutionMode executionMode) 
        throws ModelManagementException {
        getLogger().info("Setting up configuration base: {}", easySetup.getBase());
        getLogger().info("Setting up configuration meta model: {}", easySetup.getIvmlMetaModelFolder());
        getLogger().info("Setting up configuration model name: {}", easySetup.getIvmlModelName());
        EasyExecutor exec = new EasyExecutor(
            easySetup.getBase(), 
            easySetup.getIvmlMetaModelFolder(), 
            easySetup.getIvmlModelName());
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
