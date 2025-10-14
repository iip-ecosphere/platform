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

package de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.io.IOException;

import de.iip_ecosphere.platform.configuration.ivml.IvmlUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.uni_hildesheim.sse.easy.loader.ManifestLoader;
import de.uni_hildesheim.sse.easy.loader.framework.Log;
import de.uni_hildesheim.sse.easy.loader.framework.Log.LoaderLogger;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.ILogger;
import net.ssehub.easy.basics.logger.LoggingLevel;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;

/**
 * The lifecycle descriptor for the configuration component.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationLifecycleDescriptor implements LifecycleDescriptor {

    private ManifestLoader loader;
    private boolean doLogging = true;
    private boolean doFilterLogs = false;
    private ClassLoader classLoader = ConfigurationLifecycleDescriptor.class.getClassLoader();
    
    /**
     * Defines the basic logging levels in here.
     * 
     * @author Holger Eichelberger, SSE
     */
    private enum LogLevel {
        DEBUG,
        INFO, 
        WARN, 
        ERROR,
        TRACE,
        FATAL,
        OFF
    }
    
    /**
     * SLF4J-to-EASy logging adapter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class Slf4EasyLogger implements ILogger, LoaderLogger {
        
        @Override
        public void info(String msg, Class<?> clazz, String bundleName) {
            if (allowLogging(msg, clazz, bundleName, LogLevel.INFO)) {
                getLogger().info("[" + clazz.getName() + "] " + msg);
            }
        }

        @Override
        public void error(String msg, Class<?> clazz, String bundleName) {
            if (allowLogging(msg, clazz, bundleName, LogLevel.ERROR)) {
                getLogger().error("[" + clazz.getName() + "] " + msg);
            }
        }

        @Override
        public void warn(String msg, Class<?> clazz, String bundleName) {
            if (allowLogging(msg, clazz, bundleName, LogLevel.WARN)) {
                getLogger().warn("[" + clazz.getName() + "] " + msg);
            }
        }

        @Override
        public void debug(String msg, Class<?> clazz, String bundleName) {
            if (allowLogging(msg, clazz, bundleName, LogLevel.DEBUG)) {
                getLogger().debug("[" + clazz.getName() + "] " + msg);
            }
        }

        @Override
        public void exception(String msg, Class<?> clazz, String bundleName) {
            if (allowLogging(msg, clazz, bundleName, LogLevel.ERROR)) {
                getLogger().error("[" + clazz.getName() + "] " + msg);
            }
        }

        @Override
        public void error(String error) {
            getLogger().error("[Loader] " + error);
        }

        @Override
        public void error(String error, Exception exception) {
            getLogger().error("[Loader] " + error);
        }

        @Override
        public void warn(String warning) {
            getLogger().warn("[Loader] " + warning);
        }

        @Override
        public void warn(String warning, Exception exception) {
            getLogger().warn("[Loader] " + warning);
        }

        @Override
        public void info(String msg) {
            getLogger().warn("[Loader] " + msg); // warn for now
        }
        
    }
    
    /**
     * Returns whether logging is allowed.
     * 
     * @param msg the message
     * @param clazz the originating class
     * @param bundleName the originating bundle
     * @param level the logging level
     * @return {@code true} for log the message, {@code false} for consume and be quiet
     */
    private boolean allowLogging(String msg, Class<?> clazz, String bundleName, LogLevel level) {
        boolean emit = doLogging;
        if (doFilterLogs && (LogLevel.ERROR != level && LogLevel.WARN != level)) { // limit main decision override 
            emit = clazz == EasyExecutor.class;
        }
        return emit;
    }

    /**
     * Mapping EASy executor logger information into this logger.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class ExecLogger implements net.ssehub.easy.producer.core.mgmt.EasyExecutor.Logger {

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
     * Explicitly defines the class loader for loading EASy. Default is the class loader of this class.
     * 
     * @param classLoader the class loader, ignored if <b>null</b>
     */
    public void setClassLoader(ClassLoader classLoader) {
        if (null != classLoader) {
            this.classLoader = classLoader;
        }
    }
    
    @Override
    public void startup(String[] args) {
        try {
            Slf4EasyLogger logger = new Slf4EasyLogger();
            Log.setLogger(logger);
            EASyLoggerFactory.INSTANCE.setLogger(logger);
            // pass through everything and let platform logger decide
            EASyLoggerFactory.INSTANCE.setLoggingLevel(LoggingLevel.INFO);
            ConfigurationSetup setup = ConfigurationSetup.getSetup();
            loader = new ManifestLoader(false, classLoader); // to debug, replace first parameter by true, mvn install
            EasySetup easySetup = setup.getEasyProducer();
            loader.setVerbose(easySetup.getLogLevel() == EasyLogLevel.EXTRA_VERBOSE);
            getLogger().info("EASy-Producer is starting");
            doFilterLogs = easySetup.getLogLevel() == EasyLogLevel.NORMAL;
            doLogging = !doFilterLogs;
            loader.startup();
            doLogging = true;
            getLogger().info("Setting up configuration base: {}", easySetup.getBase());
            getLogger().info("Setting up configuration meta model: {}", easySetup.getIvmlMetaModelFolder());
            getLogger().info("Setting up configuration model name: {}", easySetup.getIvmlModelName());
            EasyExecutor exec = new EasyExecutor(
                easySetup.getBase(), 
                easySetup.getIvmlMetaModelFolder(), 
                easySetup.getIvmlModelName());
            //exec.setReasoningProjectFilter(p -> !IvmlUtils.isTemplate(p));
            exec.setLogger(new ExecLogger());
            // VIL model name is fix, IVML/Configuration name may change
            exec.setVilModelName(EasySetup.PLATFORM_META_MODEL_NAME);
            // self-instantiation into gen, assumed to be empty, may be cleaned up
            //exec.setVtlFolder(new File(setup.getIvmlMetaModelFolder(), "vtl")); // can be, but not needed
            getLogger().info("Setting up generation target: {}", easySetup.getGenTarget());
            exec.setVilSource(easySetup.getGenTarget());
            exec.setVilTarget(easySetup.getGenTarget());
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
            try {
                getLogger().info("Setting up EASy-Producer locations, loading models");
                exec.setupLocations();
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
