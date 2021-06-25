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
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.FileUtils;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;

public class PlatformInstantiator {

    /**
     * Configures the instantiation. Default is for command line execution, but the configurer allows for adjusting the 
     * execution  to jUnit testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class InstantiationConfigurer {

        private String ivmlModelName;
        private File outputFolder;
        private File modelFolder;
        
        /**
         * Creates a configurer instance.
         * 
         * @param ivmlModelName the name of the IVML model representing the topmost platform configuration
         * @param modelFolder the folder where the model is located (ignored if <b>null</b>)
         * @param outputFolder the output folder for code generation
         */
        public InstantiationConfigurer(String ivmlModelName, File modelFolder, File outputFolder) {
            this.ivmlModelName = ivmlModelName;
            this.modelFolder = modelFolder;
            this.outputFolder = outputFolder;
        }

        /**
         * Configures the platform instantiation via the given {@link ConfigurationSetup}.
         * 
         * @param setup the setup instance
         */
        protected void configure(ConfigurationSetup setup) {
            setup.setIvmlModelName(ivmlModelName);
            if (null != modelFolder) {
                setup.setIvmlConfigFolder(modelFolder);
            }
            if (cleanOutputFolder()) {
                FileUtils.deleteQuietly(outputFolder);
                outputFolder.mkdirs();
            }
            setup.setGenTarget(outputFolder);    
        }
        
        /**
         * Returns whether the output folder shall be cleaned before code generation.
         * 
         * @return {@code true} for clean, {@code false} else
         */
        protected boolean cleanOutputFolder() {
            return true;
        }
        
        /**
         * Obtains the lifecycle descriptor.
         * 
         * @return the descriptor
         */
        protected ConfigurationLifecycleDescriptor obtainLifecycleDescriptor() {
            return new ConfigurationLifecycleDescriptor();
        }
        
        /**
         * Validates the configuration after reasoning. May terminate the program or throw an exception.
         * 
         * @param conf the configuration
         */
        protected void validateConfiguration(Configuration conf) throws ExecutionException {
        }
        
        /**
         * Validates the reasoning result. May terminate the program or throw an exception.
         * 
         * @param res the reasoning result
         */
        protected void validateReasoningResult(ReasoningResult res) throws ExecutionException {
            if (res.hasConflict()) {
                System.exit(-1);
            }
        }
        
        /**
         * Handles an instantiation exception.
         * 
         * @param ex the exception
         * 
         * @throws ExecutionException may re-throw the exception
         */
        protected void handleExecutionException(ExecutionException ex) throws ExecutionException {
            System.out.println(ex.getMessage());
        }
        
    }
    
    /**
     * Performs the platform instantiation.
     * 
     * @param configurer the configurer
     * @throws ExecutionException in case that the instantiation fails and the configurer re-throws the exception
     */
    public static void instantiate(InstantiationConfigurer configurer) throws ExecutionException {
        ConfigurationSetup setup = ConfigurationSetup.getConfiguration();
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        configurer.validateConfiguration(ConfigurationManager.getIvmlConfiguration());
        ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
        EasyExecutor.printReasoningMessages(rRes);
        configurer.validateReasoningResult(rRes);
        try {
            ConfigurationManager.instantiate(); // throws exception if it fails
        } catch (ExecutionException e) {
            configurer.handleExecutionException(e);
        } finally {
            lcd.shutdown();
            setup.reset();
        }
    }
    
    /**
     * Performs the platform instantiation.
     * 
     * @param args command line arguments
     * 
     * @throws ExecutionException in case that the VIL instantiation fails, shall not occur here as handled by 
     * default {@link InstantiationConfigurer}
     */
    public static void main(String[] args) throws ExecutionException {
        // TODO very initial, unify command line arg handling
        if (args.length != 3) {
            System.out.println("IIP-Ecosphere platform instantiator");
            System.out.println("Following arguments are required:");
            System.out.println(" - name of the model/configuration");
            System.out.println(" - folder the model is located in");
            System.out.println(" - output folder where to place the generated artifacts");
        } else {
            instantiate(new InstantiationConfigurer(args[0], new File(args[1]), new File(args[2])));
        }
    }

}
