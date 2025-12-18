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

package de.iip_ecosphere.platform.tools.maven.python;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * Maven Mojo for Python unit tests.
 * 
 * @author Alexander Weber, SSE
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "test-python", defaultPhase = LifecyclePhase.TEST)
public class PythonTestMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "python-test.failOnError", defaultValue = "true")
    private boolean failOnError;
    
    @Parameter(property = "python-test.modelProject", defaultValue = "../../../target/pySrc")
    private String modelProject;

    @Parameter(property = "skipTests", required = false, defaultValue = "false")
    private boolean surefireSkip;

    @Parameter(property = "maven.test.skip", required = false, defaultValue = "false")
    private boolean mavenSkip;

    @Parameter(property = "python-test.skip", required = false, defaultValue = "false")
    private boolean skip;

    @Parameter(property = "python-test.test", required = false, defaultValue = "")
    private String test;

    @Parameter(property = "python.binary", required = false, defaultValue = "")
    private String python;

    @Parameter(property = "python.pythonpath", required = false, defaultValue = "")
    private String pythonPath;

    @Parameter(property = "python.pythonArgs", required = false, defaultValue = "")
    private String pythonArgs;

    /**
     * A specific <code>fileSet</code> rule to select files and directories.
     */
    @Parameter(required = false)
    private FileSet fileset;
    
    private int testedFileCount = 0;
    
    /**
     * Modify the test file. [testing]
     * 
     * @param test the test file
     */
    public void setTest(String test) {
        this.test = test;
    }

    /**
     * Returns the number of tested files.
     * 
     * @param reset shall the number be reset
     * @return the number of tested files
     */
    public int getTestedFileCount(boolean reset) {
        int result = testedFileCount;
        if (reset) {
            testedFileCount = 0;
        }
        return result;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip && !surefireSkip && !mavenSkip) {
            File baseDir = project.getBasedir();
            List<File> pythonFiles;
            if (null != fileset) {
                pythonFiles = new ArrayList<>();
                FileSetManager fileSetManager = new FileSetManager();
                String[] includedFiles = fileSetManager.getIncludedFiles(fileset);
                for (String f : includedFiles) {
                    if (f.endsWith(".py")) {
                        pythonFiles.add(new File(f));
                    }
                }
            } else {
                pythonFiles = PythonCompileMojo.getAllPythonFiles(
                    new File(baseDir, "/src/test/python/").getAbsolutePath(), false);
            }
    
            String pythonExecutable = PythonUtils.getPythonExecutable(python).toString();
    
            getLog().info("Using Python: " + pythonExecutable);
    
            String output = "";
            String testFile = test == null ? "" : test; // do not modify original
            if (testFile.length() > 0) {
                if (!testFile.endsWith(".py")) {
                    testFile += ".py";
                }
                getLog().info("Seleted test file: " + testFile);
            }
            String[] envp = composeEnvironment();
            for (File f : pythonFiles) {
                if (testFile.length() == 0 || f.getName().startsWith(testFile)) {
                    getLog().info("Executing Python test: " + f.getName());
                    String[] cmd = {pythonExecutable.toString(), f.getName(), modelProject}; 
                    cmd = PythonUtils.insertArgs(cmd, 1, pythonArgs);
                    output += runPythonTest(cmd, new File(baseDir, "src/test/python/").getAbsolutePath(), envp);
                    testedFileCount++;
                }
            }
            if (output.length() > 0) {
                getLog().info(output);
            }
            File win = new File("src/test/python/%SystemDrive%"); // whyever this is created, side effect of envp?
            if (win.exists() && win.isDirectory()) {
                FileUtils.deleteQuietly(win);
            }
            if (output.contains("Traceback")) {
                throw new MojoExecutionException(output, null);
            }
        } else {
            getLog().info("Skipping Python test execution");
        }
    }
    
    /**
     * Composes the environment for the python exec call.
     * 
     * @return the environment, my be <b>null</b> for none
     */
    private String[] composeEnvironment() {
        String[] envp = null;
        if (null == pythonPath || pythonPath.length() == 0) {
            List<String> paths = new ArrayList<>();
            add(paths, "./target/pyEnv/iip"); // old style
            add(paths, "./target/pyEnv"); // old style
            add(paths, "./target/pySrc/iip"); // old style
            add(paths, "./target/pySrc"); // all the other directories
            // target/gen/hm23/ApplicationInterfaces
            File f = new File("target/gen");
            if (f.exists()) {
                File[] sub = f.listFiles();
                if (null != sub) {
                    for (File s: sub) {
                        File sai = new File(s, "ApplicationInterfaces");
                        if (s.isDirectory() && sai.isDirectory()) {
                            add(paths, "./src/gen/" + s.getName() + "ApplicationInterfaces");
                        }
                    }
                }
            }            
            f = new File("src/main/python");
            if (f.exists()) {
                File[] sub = f.listFiles();
                if (null != sub) {
                    for (File s: sub) {
                        if (s.isDirectory() && !s.getName().equals("__pycache__")) {
                            add(paths, "./src/main/python/" + s.getName());
                        }
                    }
                }
                add(paths, "./src/main/python");
            }
            
            // "src/main/python/services"
            pythonPath = String.join(File.pathSeparator, paths);
        }
        envp = new String[] {"PYTHONPATH=" + pythonPath, // whitespaces? quote does not work with Python
            "PRJ_HOME=" + (new File("").getAbsolutePath())}; 
        getLog().info("Using " + String.join(", ", envp));
        return envp;
    }
    
    /**
     * Running the syntax check for the python Files.
     * @param cmd  the command to run, shall run a python file
     * @param workingDirectory the directory of the python tests
     * @return The output to add to the other outputs
     */
    private String runPythonTest(String[] cmd, String workingDirectory, String[] envp) {
        Process process;
        String output = "";
        try {
            process = Runtime.getRuntime().exec(cmd, envp, new File(workingDirectory));
            output = PythonCompileMojo.readProcessOutput(process.getInputStream());
            output += PythonCompileMojo.readProcessOutput(process.getErrorStream());

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * Adds a path given with slashes to {@code paths} as OS-specific path.
     * 
     * @param paths the paths list to be modified
     * @param path the path to be added
     */
    private static void add(List<String> paths, String path) {
        File f = new File(path);
        try {
            f = f.getCanonicalFile();
        } catch (IOException e) {
        }
        if (f.exists()) {
            paths.add(f.getAbsolutePath());
        }
    }

}
