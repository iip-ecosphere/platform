/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import de.iip_ecosphere.platform.support.IOUtils;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JavaUtils;
import de.iip_ecosphere.platform.support.plugins.StreamGobbler;
import de.iip_ecosphere.platform.configuration.cfg.PlatformInstantiation;
import de.iip_ecosphere.platform.configuration.easyProducer.PlatformInstantiator.InstantiationConfigurer;

/**
 * Helper methods to execute the platform instantiator as an own process. This class must not have
 * dependencies into critical libraries that may conflict, e.g., logging or Google Guava. Please set 
 * JVM system properties on the executor so that they can be passed to the respective execution.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformInstantiatorExecutor implements PlatformInstantiation {
    
    public static final Consumer<String> SYSOUT_CONSUMER = t -> System.out.println(t);

    private File localRepo;
    private Consumer<String> warn;
    private Consumer<String> info;
    private Consumer<Long> executionTimeConsumer;
    private String mainCls = PlatformInstantiator.class.getName();
    private boolean inTesting = false;
    private Map<String, String> properties = new HashMap<>();
    
    /**
     * Creates an executor instance.
     * 
     * @param localRepo the local Maven repository, may be <b>null</b>
     * @param warn a warning message consumer
     * @param info an information message consumer
     * @param executionTimeConsumer optional consumer for the (successful) process execution time, may be <b>null</b> 
     *     for none
     */
    public PlatformInstantiatorExecutor(File localRepo, Consumer<String> warn, Consumer<String> info, 
        Consumer<Long> executionTimeConsumer) {
        this.localRepo = localRepo;
        this.warn = warn;
        this.info = info;
        this.executionTimeConsumer = executionTimeConsumer;
    }

    /**
     * Instantiates a platform as specified by the {@code configurer} running the instantiation in an 
     * own process. May also be a test execution.
     * 
     * @param configurer the instantiation configurer
     * @throws ExecutionException if the instantiation fails
     */
    public static void instantiate(InstantiationConfigurer configurer) throws ExecutionException {
        PlatformInstantiatorExecutor executor = new PlatformInstantiatorExecutor(
            null, SYSOUT_CONSUMER, SYSOUT_CONSUMER, null);
        executor.inTesting = configurer.inTesting();
        executor.mainCls = configurer.getMainClass();
        executor.properties = configurer.getProperties();
        executor.executeAsProcess(PlatformInstantiatorExecutor.class.getClassLoader(), 
            null, null, null, configurer.toArgs(false));
    }

    /**
     * Creates a JVM argument from {@code key} and {@code value}.
     * 
     * @param key the key
     * @param value the value, may be <b>null</b>
     * @return the JVM argument
     */
    public static String createJvmArg(String key, String value) {
        String result = "-D" + key;
        if (null != value) {
            result += "=" + value;
        }
        return result;
    }
    
    /**
     * Sets a JVM system property for execution.
     * 
     * @param key the key
     * @param value the value
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
        
    /**
     * Executes the platform instantiator directly within an own JVM. This may be required if there are significant
     * library overlaps that can also not resolved by creating a dedicated classloader.
     * 
     * @param loader the class loader to load the classpath resource file
     * @param resourcesDir the optional resources directory for the instantiation
     * @param tracingLevel the tracing level for the instantiation
     * @param mvnArgs optional maven arguments for the instantiation (may be <b>null</b> for none)
     * @param args the instantiator arguments
     * @see #createEasyClassLoader(ClassLoader)
     */
    public void executeAsProcess(ClassLoader loader, String resourcesDir, String tracingLevel, 
        String mvnArgs, String... args) throws ExecutionException {
        List<String> pArgs = new ArrayList<>();
        pArgs.add(JavaUtils.getJavaBinaryPath("java"));
        for (String key : properties.keySet()) {
            pArgs.add(createJvmArg(key, properties.get(key)));
        }
        File cpFile = null;
        List<String> cp = createEasyClasspath(loader);
        if (null != cp) {
            String cpString = String.join(File.pathSeparator, cp);
            cpFile = new File(FileUtils.getTempDirectory(), "config.cp-" + Thread.currentThread().getId());
            try {
                FileUtils.write(cpFile, "-cp " + cpString, Charset.defaultCharset());
                pArgs.add("@" + cpFile.getAbsolutePath());
            } catch (IOException e) {
                pArgs.add("-cp");
                pArgs.add(cpString);
                info.accept("Cannot write args file. Falling back to classpath as argument. " + e.getMessage());
            }
        }
        if (null != tracingLevel) {
            pArgs.add(createJvmArg(PlatformInstantiator.KEY_PROPERTY_TRACING, tracingLevel));
        }
        if (null != mvnArgs) {
            pArgs.add(createJvmArg(PlatformInstantiator.KEY_PROPERTY_MVNARGS, mvnArgs));
        }
        if (null != resourcesDir) {
            pArgs.add(createJvmArg("iip.resources", resourcesDir));
        }
        try {
            info.accept("Calling platform instantiator as process with " + java.util.Arrays.toString(args) 
                + ", tracing " + tracingLevel + (null == resourcesDir ? "" : ", resources dir " + resourcesDir));
            long start = System.currentTimeMillis();
            pArgs.add(mainCls);
            Collections.addAll(pArgs, args);
            //info.accept("cmd line: " + pArgs);
            Process process = new ProcessBuilder(pArgs)
                .start(); // inheritIO does not help
            StreamGobbler.attach(process);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new ExecutionException("Instantiation failed with exit code: " + exitCode, null);
            }
            if (null != executionTimeConsumer) {
                executionTimeConsumer.accept(System.currentTimeMillis() - start);
            }
        } catch (IOException | InterruptedException e) {
            throw new ExecutionException(e.getMessage(), null);
        }
        FileUtils.deleteQuietly(cpFile);
    }
    
    /**
     * Executes the platform instantiator through an on class loader within this JVM. This may fail if there are 
     * significant library overlaps that can also not resolved by creating a dedicated classloader.
     * 
     * @param loader the class loader to load the classpath resource file
     * @param resourcesDir the optional resources directory for the instantiation
     * @param tracingLevel the tracing level for the instantiation
     * @param mvnArgs optional maven arguments for the instantiation (may be <b>null</b> for none)
     * @param args the instantiator arguments
     * @see #createEasyClassLoader(ClassLoader)
     */
    public void execute(ClassLoader loader, String resourcesDir, String tracingLevel, 
        String mvnArgs, String... args) throws ExecutionException {
        if (null != tracingLevel) {
            System.setProperty(PlatformInstantiator.KEY_PROPERTY_TRACING, tracingLevel);
        }
        if (null != mvnArgs) {
            System.setProperty(PlatformInstantiator.KEY_PROPERTY_MVNARGS, mvnArgs);
        }
        if (null != resourcesDir) {
            System.setProperty("iip.resources", resourcesDir);
        }
        info.accept("Calling platform instantiator with " + java.util.Arrays.toString(args) + ", tracing "
            + tracingLevel + (null == resourcesDir ? "" : " and resources dir " + resourcesDir) + " and properties " 
            + properties);
        long start = System.currentTimeMillis();
        for (String key : properties.keySet()) {
            System.setProperty(key, properties.get(key));
        }
        PlatformInstantiator.setClassLoader(createEasyClassLoader(loader));
        int exitCode = PlatformInstantiator.mainImpl(args);
        if (exitCode != 0) {
            throw new ExecutionException("Instantiation failed with exit code: " + exitCode, null);
        }
        if (null != executionTimeConsumer) {
            executionTimeConsumer.accept(System.currentTimeMillis() - start);
        }
    }
    
    /**
     * Constructs/relocates the class path for EASy-Producer from the {@code config.classpath} file
     * in configuration.configuration.
     * 
     * @param loader the resource class loader
     * @return the classpath, <b>null</b> if creating the classpath fails
     */
    public List<String> createEasyClasspath(ClassLoader loader) {
        List<String> result = null;
        String cpFile = inTesting ? "config-test.classpath" : "config.classpath";
        System.out.println("Loading classpath from " + cpFile);
        if (inTesting) {
            System.out.println("Hint: to update the classpath, see README.MD");
        }
        boolean debug = Boolean.valueOf(System.getProperty("okto.instantiator.debug", "false"));
        InputStream cpIn = loader.getResourceAsStream(cpFile);
        if (null != cpIn) {
            if (null == localRepo) { // usual fallback
                String tmp = System.getenv("M2_HOME");
                if (null == tmp) {
                    tmp = System.getProperty("user.home") + "/.m2/repository";
                }
                localRepo = new File(tmp);
            }
            if (debug) {
                System.out.println("Local repo: " + localRepo);
            }
            try {
                String cpElementString = IOUtils.toString(cpIn, StandardCharsets.UTF_8);
                StringTokenizer cpElements = new StringTokenizer(cpElementString, ":");
                result = new ArrayList<>();
                while (cpElements.hasMoreTokens()) {
                    String cpElement = cpElements.nextToken();
                    int pos = cpElement.indexOf("/");
                    if (pos > 0) {
                        cpElement = localRepo + cpElement.substring(pos);
                    }
                    result.add(cpElement);
                }
                cpIn.close();
                if (debug) {
                    System.out.println("Classpath: " + result);
                }
            } catch (IOException e) {
                warn.accept("Cannot load EASy-Producer classpath file from resources: " + e.getMessage());    
            }
        } else {
            warn.accept("Cannot load classpath file from resources");    
        }
        return result;
    }
    
    /**
     * Creates a focused class loader for EASy-Producer. We assume that all dependencies are resolved.
     * 
     * @param parent the parent class loader
     * @return the class loader, may be <b>null</b> for none
     * @see #createEasyClasspath(ClassLoader)
     */
    public ClassLoader createEasyClassLoader(ClassLoader parent) {
        ClassLoader result = null;
        List<String> cp = createEasyClasspath(parent);
        if (null != cp) {
            try {
                List<URL> urls = new ArrayList<>();
                for (String c : cp) {
                    urls.add(new File(c).toURI().toURL());
                }
                result = new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
            } catch (MalformedURLException e) {
                warn.accept("Cannot create EASy-Producer classpath: " + e.getMessage());    
            }
        }
        return result;
    }
    
}
