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

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.configuration.ivml.IvmlUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;
import net.ssehub.easy.instantiation.core.model.common.ITraceFilter;
import net.ssehub.easy.instantiation.core.model.common.NoTraceFilter;
import net.ssehub.easy.instantiation.core.model.common.TopLevelExecutionTraceFilter;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.ModelQueryException;

/**
 * Instantiates the platform using EASy-Producer.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformInstantiator {

    public static final String KEY_PROPERTY_TRACING = "iip.easy.tracing";
    public static final String KEY_PROPERTY_MVNARGS = "iip.easy.mvnArgs";
    public static final String KEY_PROPERTY_APPS = "iip.easy.apps";
    private static int exitCode = 0;
    
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
        private String startRuleName = "mainCli";
        
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
         * Optionally sets the start rule name. The default name is "main".
         * 
         * @param startRuleName the start rule name
         * @return <b>this</b> (builder style)
         */
        public InstantiationConfigurer setStartRuleName(String startRuleName) {
            this.startRuleName = null == startRuleName || startRuleName.length() == 0 ? "main" : startRuleName;
            return this;
        }

        /**
         * Configures the platform instantiation via the given {@link ConfigurationSetup}.
         * 
         * @param setup the setup instance
         */
        public void configure(ConfigurationSetup setup) {
            EasySetup easySetup = setup.getEasyProducer();
            easySetup.setIvmlModelName(ivmlModelName);
            if (null != modelFolder) {
                easySetup.setIvmlConfigFolder(modelFolder);
            }
            if (cleanOutputFolder()) {
                FileUtils.deleteQuietly(outputFolder);
                outputFolder.mkdirs();
            }
            easySetup.setGenTarget(outputFolder);    
        }
        
        /**
         * Returns the VIL start rule name.
         * 
         * @return the VIL start rule name
         */
        protected String getStartRuleName() {
            return startRuleName;
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
        public ConfigurationLifecycleDescriptor obtainLifecycleDescriptor() {
            return new ConfigurationLifecycleDescriptor();
        }
        
        /**
         * Validates the configuration after reasoning. May terminate the program or throw an exception.
         * 
         * @param conf the configuration
         * @throws ExecutionException if the validation fails
         */
        protected void validateConfiguration(Configuration conf) throws ExecutionException {
        }
        
        /**
         * Validates the reasoning result. May terminate the program or throw an exception.
         * 
         * @param res the reasoning result
         * @throws ExecutionException if reasoning fails
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
         * @throws ExecutionException may re-throw the exception
         */
        protected void handleExecutionException(ExecutionException ex) throws ExecutionException {
            System.out.println(ex.getMessage());
            exitCode = 1;
        }
        
    }

    /**
     * An instantiation configurer that does not clean the output folder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class NonCleaningInstantiationConfigurer extends InstantiationConfigurer {

        /**
         * Creates a configurer instance.
         * 
         * @param ivmlModelName the name of the IVML model representing the topmost platform configuration
         * @param modelFolder the folder where the model is located (ignored if <b>null</b>)
         * @param outputFolder the output folder for code generation
         */
        public NonCleaningInstantiationConfigurer(String ivmlModelName, File modelFolder, File outputFolder) {
            super(ivmlModelName, modelFolder, outputFolder);
        }
        
        @Override
        protected boolean cleanOutputFolder() {
            return false;
        }

    }

    /**
     * Performs the platform instantiation.
     * 
     * @param configurer the configurer
     * @throws ExecutionException in case that the instantiation fails and the configurer re-throws the exception
     */
    public static void instantiate(InstantiationConfigurer configurer) throws ExecutionException {
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        configurer.validateConfiguration(ConfigurationManager.getIvmlConfiguration());
        ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
        if (null == rRes) {
            throw new ExecutionException("No valid IVML model loaded/found.", null);
        }
        EasyExecutor.printReasoningMessages(rRes);
        configurer.validateReasoningResult(rRes);
        
        Configuration cfg = ConfigurationManager.getIvmlConfiguration();
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
                    LoggerFactory.getLogger(PlatformInstantiator.class).warn("No (username) identity token for key "
                        + "'{}' found. Container deployment may fail", authKey);
                }
            } else {
                LoggerFactory.getLogger(PlatformInstantiator.class).warn("No autentication key/value for decision "
                    + "variable '{}' found. Container deployment may fail", containerAuthKeyDecName);
            }
        } catch (ModelQueryException e) {
            LoggerFactory.getLogger(PlatformInstantiator.class).warn("No decision "
                + "variable '{}' found. Container deployment may fail. {}", containerAuthKeyDecName, e.getMessage());
        }
        
        try {
            ConfigurationManager.instantiate(configurer.getStartRuleName()); // throws exception if it fails
        } catch (ExecutionException e) {
            configurer.handleExecutionException(e);
        } finally {
            lcd.shutdown();
            setup.getEasyProducer().reset();
        }
    }
    
    /**
     * Tracing of language units and function executions but not deeper.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class FunctionLevelTraceFilter implements ITraceFilter {

        @Override
        public boolean isEnabled(LanguageElementKind kind) {
            return kind == LanguageElementKind.FAILURE || kind == LanguageElementKind.LANGUAGE_UNIT 
                || kind == LanguageElementKind.FUNCTION_EXECUTION;
        }
        
        @Override
        public boolean isWarningEnabled() {
            return false;
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
        if (args.length < 3 || args.length > 4 ) {
            System.out.println("IIP-Ecosphere platform instantiator");
            System.out.println("Following arguments are required:");
            System.out.println(" - name of the model/configuration");
            System.out.println(" - folder the model is located in, src/main/easy is used for the metamodel");
            System.out.println(" - output folder where to place the generated artifacts");
            System.out.println(" - optional VIL start rule name (\"main\", \"generateApps\", \"generateInterfaces\"");
            System.out.println("   - main: app interfaces, apps, platform components (default)");
            System.out.println("   - generateInterfaces: app interfaces, no apps");
            System.out.println("   - generateAppsNoDeps: app interfaces, apps without artifact dependencies");
            System.out.println("   - generateApps: app interfaces, apps with artifact dependencies");
            System.out.println("   - generateBroker: create a sample broker");
            System.out.println("   - generatePlatform: platform components only");
            System.out.println("Optional: Output filtering -D" + KEY_PROPERTY_TRACING 
                + "=ALL|FUNC|TOP, default TOP=toplevel");
        } else {
            mainImpl(args);
            System.exit(exitCode);
        }
    }
    
    /**
     * Main functionality without returning exit code/output of help for re-use. Could be with explicit parameters...
     * 
     * @param args command line arguments
     * @return the exit code
     * @throws ExecutionException in case that the VIL instantiation fails, shall not occur here as handled by 
     *     default {@link InstantiationConfigurer}
     */
    public static int mainImpl(String[] args) throws ExecutionException {
        String tracing = System.getProperty(KEY_PROPERTY_TRACING, "TOP").toUpperCase();
        ITraceFilter filter = TopLevelExecutionTraceFilter.INSTANCE;
        if ("FUNC".equals(tracing)) {
            filter = new FunctionLevelTraceFilter();
        } else if ("ALL".equals(tracing)) {
            filter = NoTraceFilter.INSTANCE;
        }
        TracerFactory.setTraceFilter(filter);
        InstantiationConfigurer c = new InstantiationConfigurer(args[0], new File(args[1]), new File(args[2]));
        if (args.length == 4) {
            c.setStartRuleName(args[3]);
        }
        instantiate(c);
        return exitCode;
    }

}
