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

package test.de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import net.ssehub.easy.reasoning.core.frontend.ReasonerFrontend;
import net.ssehub.easy.reasoning.core.reasoner.ReasonerConfiguration;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.Project;

/**
 * Just a simple starter class with full EASy code/no EasyExecutor for debugging/testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Starter {

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
