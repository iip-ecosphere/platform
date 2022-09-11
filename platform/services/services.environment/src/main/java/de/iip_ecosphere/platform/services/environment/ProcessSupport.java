/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.AbstractProcessService.RunnableWithStop;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Process execution support functions. Process scripts or binaries shall be packaged using a Maven assembly 
 * descriptor into a ZIP file in the "root" of the Jar/Service artifact (fallback for testing can be defined, 
 * e.g., src/main/python/...). Here, the name is free, but shall not collide with the default process artifacts 
 * of generated services.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ProcessSupport {

    /**
     * A simple default customizer always requesting to inherit the process IO (standard in/out/err). May particularly
     * be helpful for debugging.
     */
    public static final Consumer<ProcessBuilder> INHERIT_IO = p -> p.inheritIO();
    
    /**
     * Holds the script context.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ScriptOwner {
        
        private boolean initialized = false;
        private File pythonFolder = null;
        private String tmpFolderName;
        private String testFallbackPath;
        private String zipFileName;
        private String resultFile;
        private Consumer<ProcessBuilder> processCustomizer;

        /**
         * Creates an instance.
         * 
         * @param tmpFolderName the temp folder name where to copy the script/extract the ZIP to.
         * @param testFallbackPath the test fallback path in the local project taking precedence during testing
         * @param zipFileName the ZIP file name to read from classpath
         */
        public ScriptOwner(String tmpFolderName, String testFallbackPath, String zipFileName) {
            this(tmpFolderName, testFallbackPath, zipFileName, null);
        }
        
        /**
         * Creates an instance.
         * 
         * @param tmpFolderName the temp folder name where to copy the script/extract the ZIP to.
         * @param testFallbackPath the test fallback path in the local project taking precedence during testing
         * @param zipFileName the ZIP file name to read from classpath
         * @param resultFile file to read result from, e.g., short lived processes, if {@code null} use standard in
         */
        public ScriptOwner(String tmpFolderName, String testFallbackPath, String zipFileName, String resultFile) {
            this.tmpFolderName = tmpFolderName;
            this.testFallbackPath = testFallbackPath;
            this.zipFileName = zipFileName;
            this.resultFile = resultFile;
        }

        /**
         * Returns whether the holder/folder is initialized.
         * 
         * @return whether it is initialized
         */
        public boolean isInitialized() {
            return initialized;
        }
        
        /**
         * Defines whether the holder/folder is initialized.
         * 
         * @param initialized whether it is initialized
         */
        void setInitialized(boolean initialized) {
            this.initialized = initialized;
        }

        /**
         * Returns the folder where the extracted script is located in.
         * 
         * @return the folder
         */
        public File getPythonFolder() {
            return pythonFolder;
        }
        
        /**
         * Changes the folder where the extracted script is located in.
         * 
         * @param pythonFolder the folder
         */
        void setPythonFolder(File pythonFolder) {
            this.pythonFolder = pythonFolder;
        }
        
        /**
         * Returns the temporary folder name to extract the scripts into.
         * 
         * @return the temporary folder name
         */
        public String getTmpFolderName() {
            return tmpFolderName; 
        }
        
        /**
         * Returns the fallback path to read the script during tests from the project folders.
         * 
         * @return the fallback path
         */
        public String getTestFallbackPath() {
            return testFallbackPath;
        }
        
        /**
         * Returns the ZIP file name containing the script.
         * 
         * @return the ZIP file name as to be read as resource
         */
        public String getZipFileName() {
            return zipFileName;
        }
        
        /**
         * Returns the result file.
         * 
         * @return the result file
         */
        public String getResultFile() {
            return resultFile;
        }
        
        /**
         * Adds an optional process customizer.
         * 
         * @param customizer the customizer
         * @return <b>thi</b>
         */
        public ScriptOwner withProcessCustomizer(Consumer<ProcessBuilder> customizer) {
            this.processCustomizer = customizer;
            return this;
        }
        
        /**
         * Returns the optional process customizer.
         * 
         * @return the customizer, may be <b>null</b>
         */
        public Consumer<ProcessBuilder> getProcessCustomizer() {
            return processCustomizer;
        }

    }
    
    /**
     * Call python scripts, waiting for and killing finally. Scripts shall be located in a Maven packaged
     * ZIP represented by {@code owner} (including unpacking state). 
     * 
     * @param owner the script owner and data instance
     * @param script the script file name to execute in the context of {@code owner}
     * @param cmdResult a consumer for the result, may be <b>null</b> for none
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     */
    public static void callPython(ScriptOwner owner, String script, Consumer<String> cmdResult, String...args) {
        if (!owner.isInitialized() && null == owner.getPythonFolder()) {
            // are we running local/in development environment
            owner.setPythonFolder(new File(owner.getTestFallbackPath()));
            if (!owner.getPythonFolder().exists()) {
                // load from maven packaged JAR via class loader
                InputStream in = ResourceLoader.getResourceAsStream(ProcessSupport.class, owner.getZipFileName());
                if (null == in) {
                    LoggerFactory.getLogger(ProcessSupport.class).error(
                        "Cannot find python scripts, neither local nor in ZIP on classpath");
                } else {
                    File tmp = FileUtils.createTmpFolder(owner.getTmpFolderName());
                    try {
                        JarUtils.extractZip(in, tmp.toPath());
                        owner.setPythonFolder(tmp);
                    } catch (IOException e) {
                        LoggerFactory.getLogger(ProcessSupport.class).error(
                            "Cannot extract python scripts: {}", e.getMessage());
                    }
                }
            } else {
                owner.setInitialized(true);
            }
        }
        if (owner.getPythonFolder() != null) {
            callPythonWaitForAndKill(owner.getPythonFolder(), script, cmdResult, owner.getResultFile(), 
                owner.getProcessCustomizer(), args);
        }
    }

    /**
     * Creates and starts a Python process.
     * 
     * @param dir the home dir where to find the script/run it within
     * @param script the script to execute
     * @param procCustomizer allows to customize the internal process builder, may be <b>null</b> for none
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createPythonProcess(File dir, String script, Consumer<ProcessBuilder> procCustomizer, 
        String... args) throws IOException {
        String pythonPath = PythonUtils.getPythonExecutable().toString();
        LoggerFactory.getLogger(ProcessSupport.class).info("Using Python: {}", pythonPath);
        List<String> tmp = new ArrayList<String>();
        tmp.add(pythonPath);
        tmp.add(script);
        for (String a : args) {
            tmp.add(a);
        }
        
        LoggerFactory.getLogger(ProcessSupport.class).info("Cmd line: {} in {}", tmp, dir);
        ProcessBuilder processBuilder = new ProcessBuilder(tmp);
        processBuilder.directory(dir);
        if (null != procCustomizer) {
            procCustomizer.accept(processBuilder);
        }
        Process python = processBuilder.start();
        return python;
    }
    
    /**
     * Wait for a (scripted) process until it is really dead.
     * 
     * @param proc the process to wait for
     * @param script the script name for logging
     * @param cmdResult a consumer for the result, may be <b>null</b> for none
     * @param resultFile file to read result from, e.g., short lived processes, if {@code null} use standard in
     * @return the process status, <code>-1</code> if the process was not executed
     * @see AbstractProcessService#redirectIO(InputStream, PrintStream)
     */
    public static int waitForAndKill(Process proc, String script, Consumer<String> cmdResult, String resultFile) {
        int procResult = -1;
        RunnableWithStop redirect = null;
        try {
            ByteArrayOutputStream res = null;
            if (null != cmdResult && null == resultFile) {
                res = new ByteArrayOutputStream();
                redirect = AbstractProcessService.redirectIO(proc.getInputStream(), new PrintStream(res));
            }
            procResult = proc.waitFor();
            AbstractProcessService.waitAndDestroy(proc, 200);
            if (null != res) {
                cmdResult.accept(res.toString());
            } else if (resultFile != null) {
                cmdResult.accept(org.apache.commons.io.FileUtils.readFileToString(
                    new File(resultFile), StandardCharsets.UTF_8));
            }
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(ProcessSupport.class).error(
                "Waiting for script {} interrupted: {}", script, e.getMessage());
        } catch (IOException e) {
            LoggerFactory.getLogger(ProcessSupport.class).error(
                "Reading for script {} results: {}", script, e.getMessage());
        } finally {
            if (null != redirect) {
                redirect.stop();
            }
        }
        return procResult;
    }
    
    // checkstyle: stop parameter number check
    
    /**
     * Call python, wait for ending and kill it if needed. Catch all exceptions.
     * 
     * @param dir the home dir where to find the script/run it within
     * @param script the script to execute
     * @param cmdResult a consumer for the result, may be <b>null</b> for none
     * @param resultFile file to read result from, e.g., short lived processes, if {@code null} use standard in
     * @param cust optional process customizer, may be <b>null</b> for none
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     * @return the process status, <code>-1</code> if the process was not executed
     * @see #createPythonProcess(File, String, Consumer, String...)
     * @see #waitForAndKill(Process, String, Consumer, String)
     */
    public static int callPythonWaitForAndKill(File dir, String script, Consumer<String> cmdResult, String resultFile, 
        Consumer<ProcessBuilder> cust, String... args) {
        try {
            Consumer<ProcessBuilder> customizer = cust;
            if (null == cust && null == cmdResult) { // just for convenience
                customizer = pb -> pb.inheritIO();
            }
            return waitForAndKill(createPythonProcess(dir, script, customizer, args), script, cmdResult, resultFile);
        } catch (IOException e) {
            LoggerFactory.getLogger(ProcessSupport.class).error(
                "Cannot execute python script {}: {}", script, e.getMessage());
            return -1;
        } 
    }

    // checkstyle: resume parameter number check

}
