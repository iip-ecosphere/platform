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
    
            /*
             * This call just goes through some locations known to contain the python3
             * executable. i.e. "/usr/bin/python3" not perfect as the last option, the one
             * most likely for windows, will not return a path to look into the
             * side-packages! Also only working for as long windows user did not rename
             * python to something else to potentially run multiple version besides each
             * other
             */
            String pythonExecutable = PythonUtils.getPythonExecutable().toString();
    
            getLog().info("Using Python " + pythonExecutable);
    
            String output = "";
            String testFile = test == null ? "" : test; // do not modify original
            if (testFile.length() > 0) {
                if (!testFile.endsWith(".py")) {
                    testFile += ".py";
                }
                getLog().info("Seleted test file: " + testFile);
            }
            for (File f : pythonFiles) {
                if (testFile.length() == 0 || f.getName().startsWith(testFile)) {
                    getLog().info("Executing Python test: " + f.getName());
                    String[] cmd = {pythonExecutable.toString(), f.getName(), modelProject}; 
                    output += runPythonTest(cmd, new File(baseDir, "src/test/python/").getAbsolutePath());
                    testedFileCount++;
                }
            }
            if (output.length() > 0) {
                getLog().info(output);
            }
            if (output.contains("Traceback")) {
                throw new MojoExecutionException(output, null);
            }
        } else {
            getLog().info("Skipping Python test execution");
        }
    }
    
    /**
     * Running the syntax check for the python Files.
     * @param cmd  the command to run, shall run a python file
     * @param workingDirectory the directory of the python tests
     * @return The output to add to the other outputs
     */
    private static String runPythonTest(String[] cmd, String workingDirectory) {
        Process process;
        String output = "";
        try {
            process = Runtime.getRuntime().exec(cmd, null, new File(workingDirectory));
            output = PythonCompileMojo.readProcessOutput(process.getInputStream());
            output += PythonCompileMojo.readProcessOutput(process.getErrorStream());

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }

}
