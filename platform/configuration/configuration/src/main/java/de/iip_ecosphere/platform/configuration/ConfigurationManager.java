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

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.configuration.ConfigurationAas.IipGraphMapper;
import de.iip_ecosphere.platform.configuration.ivml.AasIvmlMapper;
import de.iip_ecosphere.platform.configuration.ivml.IvmlUtils;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper.OperationCompletedListener;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.progress.BasicProgressObserver;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.vilTypes.configuration.NoVariableFilter;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.ModelQueryException;

/**
 * Holds the platform configuration and provides operations on the configuration. The 
 * {@link ConfigurationLifecycleDescriptor} must be used and executed before.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationManager {
    
    private static Logger logger;
    private static EasyExecutor executor;
    private static boolean initialized = false;
    private static BasicProgressObserver observer = new BasicProgressObserver();
    private static AasIvmlMapper aasIvmlMapper;
    private static OperationCompletedListener aasOpListener;
    private static boolean standalone = false;

    /**
     * Bridges between EASy progress monitoring and IIP progress notifications.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class IipProgressMonitor implements BasicProgressObserver.IProgressMonitor {

        private String taskName;
        private int maxSteps;
        private int steps;
        private String subTask;
        private int lastSteps = -1;
        private int lastMaxSteps = -1;
        
        @Override
        public void setTaskName(String name) {
            // ignore
        }

        /**
         * Sends the status via transport.
         */
        private void sendStatus() {
            String heading;
            String[] alias;
            if (null != subTask) {
                alias = new String[] {taskName, subTask};
                heading = taskName + "/" + subTask;
            } else {
                alias = new String[] {taskName};
                heading = taskName;
            }
            if (lastSteps != steps || lastMaxSteps != maxSteps) {
                int progress = 0;
                if (steps > 0 && maxSteps > 0) {
                    progress = (int) ((steps / (double) maxSteps) * 100);
                }
                if (!standalone) {
                    StatusMessage msg = new StatusMessage(ActionTypes.PROCESS, "Configuration", alias);
                    if (maxSteps > 0) {
                        msg.withProgress(progress);
                    }
                    msg.withDescription(heading);
                    Transport.sendStatus(msg);
                }
                getLogger().info("{}: {}%", heading, (maxSteps > 0 ? progress : "?"));
                lastSteps = steps;
                lastMaxSteps = maxSteps;
            }
        }
        
        @Override
        public void beginTask(String name, int max) {
            this.taskName = name;
            this.subTask = null;
            this.maxSteps = max;
            this.steps = -1;
            sendStatus();
        }

        @Override
        public void worked(int step) {
            this.steps = step;
            sendStatus();
        }

        @Override
        public void subTask(String name) {
            this.subTask = name;
            sendStatus();
        }

    }
    
    static {
        observer = new BasicProgressObserver();
        observer.register(new IipProgressMonitor());
    }
    
    /**
     * Defines the executor instance. Called from {@link ConfigurationLifecycleDescriptor}.
     * 
     * @param instance the executor instance
     */
    static void setExecutor(EasyExecutor instance) {
        executor = instance;
        if (null == instance) {
            initialized = false;
        } else {
            executor.setProgressObserver(observer);
        }
    }
    
    /**
     * Defines whether the hosting process is running as part of an installed platform (requiring transport, 
     * e.g., for status notifications) or standalone, e.g., in a build process.
     * 
     * @param isStandalone {@code true} if we run standalone, {@code false} for platorm process (the default)
     */
    static void setStandalone(boolean isStandalone) {
        standalone = isStandalone;
    }
    
    /**
     * Returns the executor instance.
     * 
     * @return the executor instance
     */
    static EasyExecutor getExecutor() {
        return executor;
    }
    
    /**
     * Lazy initialization of the executor through loading the IVML model. Assumption: Locations are set before.
     */
    private static void init() {
        if (!initialized) {
            if (null != executor) {
                try {
                    executor.loadIvmlModel();
                } catch (ModelManagementException e) {
                    getLogger().error("Cannot load EASy-Producer models: " + e.getMessage());
                }
            }
            if (null == aasIvmlMapper) {
                aasIvmlMapper = new AasIvmlMapper(() -> ConfigurationManager.getVilConfiguration(), 
                     new IipGraphMapper(), null);
                aasIvmlMapper.addGraphFormat(new DrawflowGraphFormat());
            }
            initialized = true;
        }
    }
    
    /**
     * Reloads the model.
     */
    public static void reload() {
        if (null != executor) {
            try {
                executor.discardLocations();
                executor.clearModels();
                executor.setupLocations();
                executor.loadIvmlModel();
            } catch (ModelManagementException e) {
                getLogger().error("Cannot load EASy-Producer models: " + e.getMessage());
            }
        } else {
            getLogger().error("No executor, cannot reload EASy-Producer models");
        }
    }
    
    /**
     * Returns the IVML configuration.
     * 
     * @return the configuration
     */
    public static Configuration getIvmlConfiguration() {
        init();
        return executor != null ? executor.getConfiguration() : null;
    }
    
    /**
     * Sets up container properties for local deployment.
     */
    public static void setupContainerProperties() {
        Configuration cfg = getIvmlConfiguration();
        final String containerAuthKeyDecName = "containerManager.authenticationKey";
        try {
            IDecisionVariable varAuthKey = cfg.getDecision(containerAuthKeyDecName, false);
            String authKey = IvmlUtils.getStringValue(varAuthKey, "");
            if (authKey.length() > 0) {
                IdentityToken tok = IdentityStore.getInstance().getToken(authKey);
                if (null != tok && TokenType.USERNAME == tok.getType()) {
                    System.setProperty("iip.container.user." + authKey, tok.getUserName());
                    System.setProperty("iip.container.password." + authKey, tok.getTokenDataAsString());
                } else {
                    LoggerFactory.getLogger(ConfigurationManager.class).warn("No (username) identity token for key "
                        + "'{}' found. Container deployment may fail", authKey);
                }
            } else {
                LoggerFactory.getLogger(ConfigurationManager.class).warn("No autentication key/value for decision "
                    + "variable '{}' found. Container deployment may fail", containerAuthKeyDecName);
            }
        } catch (ModelQueryException e) {
            LoggerFactory.getLogger(ConfigurationManager.class).warn("No decision "
                + "variable '{}' found. Container deployment may fail. {}", containerAuthKeyDecName, e.getMessage());
        }
    }
    
    /**
     * Returns a VIL configuration.
     * 
     * @return a configuration
     */
    public static net.ssehub.easy.instantiation.core.model.vilTypes.configuration.Configuration getVilConfiguration() {
        init();
        Configuration cfg = getIvmlConfiguration();
        return cfg != null 
            ? new net.ssehub.easy.instantiation.core.model.vilTypes.configuration
                .Configuration(cfg, NoVariableFilter.INSTANCE)
            : null;
    }
    
    // checkstyle: stop exception type check
    
    /**
     * Validates the model and propagates values within the model.
     * 
     * @return the reasoning result (preliminary)
     */
    public static ReasoningResult validateAndPropagate() {
        init();
        try {
            return executor != null ? executor.propagateOnIvmlModel() : null;
        } catch (IllegalStateException e) {
            getLogger().error(e.getMessage());
            return null;
        } catch (Throwable e) { // not nice but if something goes wrong with the reasoner...
            getLogger().error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Performs a platform instantiation.
     * 
     * @param startRuleName the name of the start rule to execute
     * @throws ExecutionException if the instantiation fails for some reason
     */
    public static void instantiate(String startRuleName) throws ExecutionException {
        init();
        if (executor != null) {
            try {
                executor.setVilStartRuleName(startRuleName);
                executor.executeVil();
            } catch (ModelManagementException | VilException | IllegalStateException e) {
                throw new ExecutionException(e);
            } catch (Throwable e) { // not nice but if something goes wrong with the reasoner...
                e.printStackTrace();
                throw new ExecutionException(e);
            }
        }
    }

    // checkstyle: resume exception type check

    /**
     * Performs a platform instantiation.
     * 
     * @throws ExecutionException if the instantiation fails for some reason
     */
    public static void instantiate() throws ExecutionException {
        instantiate("main");
    }
    
    /**
     * Defines the global AAS IVML mapper (for AAS lambda functions).
     * 
     * @param mapper the mapper instance (ignored if <b>null</b>)
     */
    public static void setAasIvmlMapper(AasIvmlMapper mapper) {
        if (null != mapper) {
            aasIvmlMapper = mapper;
        }
    }

    /**
     * Returns the global AAS IVML mapper (for AAS lambda functions).
     * 
     * @return the mapper instance
     */
    public static AasIvmlMapper getAasIvmlMapper() {
        init();
        return aasIvmlMapper;
    }
    
    /**
     * Defines the global AAS operation completed listener (for AAS lambda functions).
     * 
     * @param listener the listener
     */
    public static void setAasOperationCompletedListener(OperationCompletedListener listener) {
        aasOpListener = listener;
    }

    /**
     * Returns the global AAS operation completed listener (for AAS lambda functions).
     * 
     * @return the listener (may be <b>null</b>)
     */
    public static OperationCompletedListener getAasOperationCompletedListener() {
        return aasOpListener;
    };


    /**
     * Returns the logger for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        logger = FallbackLogger.getLogger(logger, 
            ConfigurationManager.class, 
            FallbackLogger.LoggingLevel.WARN);
        return logger;
    }

}
