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
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_hildesheim.sse.easy.loader.ListLoader;
import net.ssehub.easy.basics.modelManagement.ModelInfo;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.instantiation.core.model.buildlangModel.BuildModel;
import net.ssehub.easy.instantiation.core.model.buildlangModel.Script;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.execution.Executor;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.instantiation.core.model.templateModel.TemplateModel;
import net.ssehub.easy.instantiation.core.model.tracing.ConsoleTracerFactory;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.frontend.ReasonerFrontend;
import net.ssehub.easy.reasoning.core.reasoner.ReasonerConfiguration;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.Project;

/**
 * Holds the platform configuration and provides operations on the configuration. The 
 * {@link ConfigurationLifecycleDescriptor} must be used and executed before.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationManager {
    
    private static EasyExecutor executor;
    private static boolean initialized = false;
    
    /**
     * Defines the executor instance. Called from {@link ConfigurationLifecycleDescriptor}.
     * @param instance
     */
    static void setExecutor(EasyExecutor instance) {
        executor = instance;
        if (null == instance) {
            initialized = false;
        }
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
            initialized = true;
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
     * Validates the model and propagates values within the model.
     * 
     * @return the reasoning result (preliminary)
     */
    public static ReasoningResult validateAndPropagate() {
        init();
        return executor != null ? executor.propagateOnIvmlModel() : null;
    }
    
    /**
     * Performs a platform instantiation.
     * 
     * @throws ExecutionException if the instantiation fails for some reason
     */
    public static void instantiate() throws ExecutionException {
        init();
        if (executor != null) {
            try {
                executor.executeVil();
            } catch (ModelManagementException | VilException e) {
                throw new ExecutionException(e);
            }
        }
    }

    /**
     * Returns the logger for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ConfigurationLifecycleDescriptor.class);
    }
    
    // ------------------------- preliminary for testing -----------------------

    /**
     * Test execution.
     * 
     * @param args ignored
     * @throws IOException shall not occur
     * @throws ModelManagementException shall not occur
     */
    public static void main(String[] args) throws IOException, ModelManagementException {
        final File modelFolder = new File("src/main/easy");
        ListLoader loader = new ListLoader(); // file .easyStartup from classloader
        loader.setVerbose(true);
        System.out.println("EASy starting");
        loader.startup();
        System.out.println("EASy started");
        VarModel.INSTANCE.locations().addLocation(modelFolder, ProgressObserver.NO_OBSERVER);
        BuildModel.INSTANCE.locations().addLocation(modelFolder, ProgressObserver.NO_OBSERVER);
        TemplateModel.INSTANCE.locations().addLocation(modelFolder, ProgressObserver.NO_OBSERVER);
        System.out.println("Location added");
        List<ModelInfo<Project>> models = VarModel.INSTANCE.availableModels().getModelInfo("IIPEcosphere");
        if (null != models && !models.isEmpty()) {
            ModelInfo<Project> prjInfo = models.get(0);
            try {
                Project prj = VarModel.INSTANCE.load(prjInfo);
                Configuration cfg = new Configuration(prj);
                ReasonerConfiguration rCfg = new ReasonerConfiguration();
                ReasoningResult rResult = ReasonerFrontend.getInstance().propagate(cfg, rCfg, 
                    ProgressObserver.NO_OBSERVER);
                System.out.println("Reasoning is ok: " + (!rResult.hasConflict()));
                
                List<ModelInfo<Script>> vil = BuildModel.INSTANCE.availableModels().getModelInfo("IIPEcosphere");
                if (null != vil && !vil.isEmpty()) {
                    ModelInfo<Script> vilInfo = vil.get(0);
                    Script script = BuildModel.INSTANCE.load(vilInfo);
                    
                    TracerFactory.setInstance(ConsoleTracerFactory.INSTANCE);
                    new Executor(script)
                        .addBase(new File("."))
                        .addSource(new File("."))
                        .addConfiguration(cfg)
                        .addTarget(new File("."))
                        .execute();
                }
            } catch (ModelManagementException e) {
                System.out.println("CANNOT READ " + prjInfo.getName() + ": " + e.getMessage());
            } catch (VilException e) {
                System.out.println("VIL " + prjInfo.getName() + ": " + e.getMessage());
            }
        }
        System.out.println("Removing location");
        TemplateModel.INSTANCE.locations().removeLocation(modelFolder, ProgressObserver.NO_OBSERVER);
        BuildModel.INSTANCE.locations().removeLocation(modelFolder, ProgressObserver.NO_OBSERVER);
        VarModel.INSTANCE.locations().removeLocation(modelFolder, ProgressObserver.NO_OBSERVER);
        System.out.println("EASy stopping");
        loader.shutdown();
        System.out.println("EASy stopped");
    }

}
