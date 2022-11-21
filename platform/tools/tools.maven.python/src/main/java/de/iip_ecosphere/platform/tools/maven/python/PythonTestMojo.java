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

import de.iip_ecosphere.platform.services.environment.PythonUtils;

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
    
    @Parameter(property = "python-test.modelProject", defaultValue = "src/test/python")
    private String modelProject;

    /**
     * A specific <code>fileSet</code> rule to select files and directories.
     */
    @Parameter(required = false)
    private FileSet fileset;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

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
        for (File f : pythonFiles) {
            getLog().info("Executing Python test: " + f.getName());
            String[] cmd = {pythonExecutable.toString(), f.getName(), modelProject}; 
            output += runPythonTest(cmd, new File(baseDir, "src/test/python/").getAbsolutePath());
        }
        getLog().info(output);
        if (output.contains("Traceback")) {
            throw new MojoExecutionException(output, null);
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
