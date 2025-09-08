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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.configuration.ivml.IvmlUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import net.ssehub.easy.instantiation.core.model.common.ITraceFilter;
import net.ssehub.easy.instantiation.core.model.common.NoTraceFilter;
import net.ssehub.easy.instantiation.core.model.common.TopLevelExecutionTraceFilter;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.Configuration;

/**
 * Instantiates the platform using EASy-Producer.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformInstantiator {

    public static final String KEY_PROPERTY_TRACING = "iip.easy.tracing";
    public static final String KEY_PROPERTY_MVNARGS = "iip.easy.mvnArgs";
    public static final String KEY_PROPERTY_APPS = "iip.easy.apps";
    private static final String ARG_PROPS_START = "props>";
    private static final String ARG_PROPS_END = "<props";
    private static int exitCode = 0;
    private static ClassLoader classLoader;
    
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
        private File metaModelFolder;
        private String startRuleName = "mainCli";
        private Map<String, String> properties = new HashMap<>();
        private boolean emitReasonerWarnings = false;
        
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
         * Creates a configurer instance from command line arguments delivered by {@link #toArgs(boolean)}.
         * 
         * @param args the command line arguments
         */
        public InstantiationConfigurer(String[] args) {
            int pos = 0;
            this.ivmlModelName = args[pos++];
            this.modelFolder = fromArg(args[pos++]);
            this.outputFolder = fromArg(args[pos++]);
            this.metaModelFolder = fromArg(args[pos++]);
            this.startRuleName = args[pos++];
            if (args.length > 4 && ARG_PROPS_START.equals(args[pos])) {
                pos++;
                int startPos = pos;
                String lastKey = "";
                for (int p = pos; p < args.length && !ARG_PROPS_END.equals(args[p]); p++) {
                    if (p - startPos % 2 == 0) {
                        lastKey = args[p];
                        properties.put(lastKey, null);
                    } else {
                        properties.put(lastKey, args[p]);
                    }
                }
            }
        }
        
        
        /**
         * Returns the last command line argument index consumed by this configurer.
         * 
         * @param args the command line arguments
         * @return the index
         */
        protected int getLastArgsIndex(String[] args) {
            int pos = 4;
            if (args.length > 4 && ARG_PROPS_START.equals(args[5])) {
                pos++;
                while (pos < args.length && !ARG_PROPS_END.equals(args[pos])) {
                    pos++;
                }
            }
            return pos;
        }

        /**
         * Turns a textual file name into a file object.
         * 
         * @param file the file name
         * @return the file, may be <b>null</b> if {@code file} was <b>null</b> or "-"
         */
        protected File fromArg(String file) {
            return file == null || file.equals("-") ? null : new File(file);
        }
        
        /**
         * Turns a file name into a textual file specification.
         * 
         * @param file the file
         * @return the file specification, may be "-" if {@code file} was <b>null</b>
         */
        protected String toArg(File file) {
            return null == file ? "-" : file.toString();
        }

        /**
         * Returns the qualified name of the class containing a main method for execution
         * through {@link PlatformInstantiatorExecutor}.
         * 
         * @return the class name of {@link PlatformInstantiator}
         */
        public String getMainClass() {
            return PlatformInstantiator.class.getName();
        }
        
        /**
         * Returns whether this configurer is currently used for testing.
         * 
         * @return {@code false} for production (the default), {@code true} for testing
         */
        public boolean inTesting() {
            return false;
        }

        /**
         * Turns this configurer into command line arguments.
         * 
         * @param all {@code true} add all arguments for passong on setup between configurers, {@code false} only 
         *     command line arguments for execution 
         * @return the arguments
         */
        public String[] toArgs(boolean all) {
            List<String> args = new ArrayList<>();
            args.add(ivmlModelName); 
            args.add(toArg(modelFolder)); 
            args.add(toArg(outputFolder)); 
            args.add(toArg(metaModelFolder)); 
            args.add(startRuleName);
            if (all && properties.size() > 0) {
                args.add(ARG_PROPS_START);
                for (String key : properties.keySet()) {
                    args.add(key);
                    args.add(properties.get(key));
                }
                args.add(ARG_PROPS_END);
            }
                
            return args.toArray(new String[args.size()]); 
        }

        /**
         * Returns whether reasoning warnings shall be emitted.
         * 
         * @return {@code true} for warnings, {@code false} else
         */
        protected boolean isEmitReasonerWarnings() {
            return emitReasonerWarnings;
        }
        
        /**
         * Enables emitting reasoner warnings.
         * 
         * @return <b>this</b> (builder style)
         */
        public InstantiationConfigurer emitReasonerWarnings() {
            emitReasonerWarnings = true;
            return this;
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
         * Changes the meta model folder.
         * 
         * @param metaModelFolder the meta model folder (ignored if <b>null</b> or does not exist)
         * @return the meta model folder
         */
        public InstantiationConfigurer setIvmlMetaModelFolder(File metaModelFolder) {
            if (null != metaModelFolder && metaModelFolder.exists()) {
                this.metaModelFolder = metaModelFolder;
            }
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
            if (null != metaModelFolder) {
                easySetup.setIvmlMetaModelFolder(metaModelFolder);
            }
            if (cleanOutputFolder()) {
                FileUtils.deleteQuietly(outputFolder);
                outputFolder.mkdirs();
            }
            easySetup.setGenTarget(outputFolder);    
        }
        
        /**
         * Returns the VIL start rule name. [public for testing]
         * 
         * @return the VIL start rule name
         */
        public String getStartRuleName() {
            return startRuleName;
        }
        
        /**
         * Returns the IVML model name. [testing]
         * 
         * @return the model name
         */
        public String getIvmlModelName() {
            return ivmlModelName;
        }

        /**
         * Returns the VIL output folder. [testing]
         * 
         * @return the output folder
         */
        public File getOutputFolder() {
            return outputFolder;
        }

        /**
         * Returns the model folder. [testing]
         * 
         * @return the model folder
         */
        public File getModelFolder() {
            return modelFolder;
        }

        /**
         * Returns the meta model folder. [testing]
         * 
         * @return the meta model folder
         */
        public File getMetaModelFolder() {
            return metaModelFolder;
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
            if (IvmlUtils.analyzeReasoningResult(res, isEmitReasonerWarnings(), true)) {
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
        
        /**
         * Sets a JVM system property for execution.
         * 
         * @param key the key
         * @param value the value
         */
        public InstantiationConfigurer setProperty(String key, String value) {
            properties.put(key, value);
            return this;
        }

        /**
         * Returns the properties.
         * 
         * @return the properties
         */
        public Map<String, String> getProperties() {
            return new HashMap<>(properties);
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
     * Sets an explicit class loader for loading EASy-Producer. By default, it's the class loader of the 
     * {@link ConfigurationLifecycleDescriptor}.
     * 
     * @param loader the class loader, ignored if <b>null</b>
     */
    public static void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

    /**
     * Performs the platform instantiation. Considers the class loader set before through 
     * {@link #setClassLoader(ClassLoader)}.
     * 
     * @param configurer the configurer
     * @throws ExecutionException in case that the instantiation fails and the configurer re-throws the exception
     */
    public static void instantiate(InstantiationConfigurer configurer) throws ExecutionException {
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.setClassLoader(classLoader); // ignored if null
        lcd.startup(new String[0]); // shall register executor
        configurer.validateConfiguration(ConfigurationManager.getIvmlConfiguration());
        ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
        if (null == rRes) {
            throw new ExecutionException("No valid IVML model loaded/found.", null);
        }
        configurer.validateReasoningResult(rRes);
        ConfigurationManager.setupContainerProperties();
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
        if (args.length < 3) {
            System.out.println("oktoflow platform instantiator");
            System.out.println("Following arguments are required:");
            System.out.println(" - name of the model/configuration");
            System.out.println(" - folder the model is located in, src/main/easy is used for the metamodel");
            System.out.println(" - output folder where to place the generated artifacts");
            System.out.println(" - optional VIL start rule name (\"main\", \"generateApps\", \"generateInterfaces\"");
            System.out.println(" - optional IVML meta model folder");
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
     * Sets the trace filter based on the system property {@link #KEY_PROPERTY_TRACING}.
     */
    public static void setTraceFilter() {
        String tracing = System.getProperty(KEY_PROPERTY_TRACING, "TOP").toUpperCase();
        ITraceFilter filter = TopLevelExecutionTraceFilter.INSTANCE;
        if ("FUNC".equals(tracing)) {
            filter = new FunctionLevelTraceFilter();
        } else if ("ALL".equals(tracing)) {
            filter = NoTraceFilter.INSTANCE;
        }
        TracerFactory.setTraceFilter(filter);
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
        ConfigurationManager.setStandalone(true);
        setTraceFilter();
        InstantiationConfigurer c = new InstantiationConfigurer(args[0], new File(args[1]), new File(args[2]));
        if (args.length >= 4) {
            c.setStartRuleName(args[3]);
        }
        if (args.length >= 5) {
            c.setIvmlMetaModelFolder(new File(args[4]));
        }
        instantiate(c);
        return exitCode;
    }

}
