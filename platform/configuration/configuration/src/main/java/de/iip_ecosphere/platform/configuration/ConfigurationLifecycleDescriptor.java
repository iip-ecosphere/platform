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

import org.apache.log4j.lf5.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.uni_hildesheim.sse.easy.loader.ListLoader;
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

    private ListLoader loader;
    private boolean doLogging = true;
    private boolean doFilterLogs = false;
    
    /**
     * SLF4J-to-EASy logging adapter.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class Slf4EasyLogger implements ILogger {

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
            if (allowLogging(msg, clazz, bundleName, LogLevel.FATAL)) {
                getLogger().error("[" + clazz.getName() + "] " + msg);
            }
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
        if (doFilterLogs) {
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
            getLogger().error(text);
        }

        @Override
        public void info(String text) {
        }
        
    }
    
    @Override
    public void startup(String[] args) {
        try {
            EASyLoggerFactory.INSTANCE.setLogger(new Slf4EasyLogger());
            // pass through everything and let platform logger decide
            EASyLoggerFactory.INSTANCE.setLoggingLevel(LoggingLevel.INFO);
            ConfigurationSetup setup = ConfigurationSetup.getSetup();
            loader = new ListLoader(); // file .easyStartup from classloader
            EasySetup easySetup = setup.getEasySetup();
            loader.setVerbose(easySetup.getLogLevel() == EasyLogLevel.EXTRA_VERBOSE);
            getLogger().info("EASy-Producer is starting");
            doFilterLogs = easySetup.getLogLevel() == EasyLogLevel.NORMAL;
            doLogging = !doFilterLogs;
            loader.startup();
            doLogging = true;
            EasyExecutor exec = new EasyExecutor(
                easySetup.getBase(), 
                easySetup.getIvmlMetaModelFolder(), 
                easySetup.getIvmlModelName());
            exec.setLogger(new ExecLogger());
            // VIL model name is fix, IVML/Configuration name may change
            exec.setVilModelName(EasySetup.PLATFORM_META_MODEL_NAME);
            // self-instantiation into gen, assumed to be empty, may be cleaned up
            //exec.setVtlFolder(new File(setup.getIvmlMetaModelFolder(), "vtl")); // can be, but not needed
            exec.setVilSource(easySetup.getGenTarget());
            exec.setVilTarget(easySetup.getGenTarget());
            File ivmlCfg = easySetup.getIvmlConfigFolder();
            if (null != ivmlCfg && !ivmlCfg.equals(easySetup.getIvmlMetaModelFolder())) {
                exec.prependIvmlFolder(ivmlCfg);
            }
            try {
                exec.setupLocations();
                ConfigurationManager.setExecutor(exec);
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
        EasyExecutor exec = ConfigurationManager.getExecutor();
        if (null != exec) {
            try {
                exec.discardLocations();
            } catch (ModelManagementException e) {
                getLogger().error("Cannot set model locations. Configuration capabilities may be disabled. " 
                    + e.getMessage(), e);
            }
        }
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
