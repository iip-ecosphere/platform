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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;

/**
 * Maven Mojo for executing Python programs.
 * 
 * @author Alexander Weber, SSE
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "exec-python")
public class PythonExecMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "python-exec.failOnError", defaultValue = "true")
    private boolean failOnError;
    
    @Parameter(property = "python-exec.skip", required = false, defaultValue = "false")
    private boolean skip;

    @Parameter(property = "python.binary", required = false, defaultValue = "")
    private String python;

    @Parameter(property = "python.pythonpath", required = false, defaultValue = "")
    private String pythonPath;

    @Parameter(property = "python-exec.file", required = true)
    private String pythonFile;

    @Parameter(property = "python-exec.directory", required = false, defaultValue = "src/test/python/")
    private String directory;

    @Parameter(property = "python-exec.args", required = false, defaultValue = "")
    private List<String> args;

    @Parameter(property = "python-exec.pythonArgs", required = false, defaultValue = "")
    private List<String> pythonArgs;

    /**
     * A specific <code>fileSet</code> rule to select files and directories.
     */
    @Parameter(required = false)
    private FileSet fileset;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            File baseDir = project.getBasedir();
    
            String pythonExecutable = PythonUtils.getPythonExecutable(python).toString();
    
            getLog().info("Using Python: " + pythonExecutable);
    
            String output = "";
            String[] envp = composeEnvironment();
            getLog().info("Executing Python: " + pythonFile);
            List<String> cmd = new ArrayList<>();
            cmd.add(pythonExecutable.toString());
            if (null != pythonArgs) {
                cmd.addAll(pythonArgs);
            }
            cmd.add(pythonFile);
            if (null != args) {
                cmd.addAll(args);
            }
            output += runPython(cmd.toArray(new String[cmd.size()]), new File(baseDir, directory).getAbsolutePath(), 
                envp);
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
            getLog().info("Skipping Python execution");
        }
    }
    
    /**
     * Composes the environment for the python exec call.
     * 
     * @return the environment, my be <b>null</b> for none
     */
    private String[] composeEnvironment() {
        String[] envp = null;
        
        List<String> tmp = new ArrayList<>();
        if (null != pythonPath && pythonPath.length() > 0) {
            tmp.add("PYTHONPATH=" + pythonPath); // whitespaces? quote does not work with Python
        }
        tmp.add("PRJ_HOME=" + (new File("").getAbsolutePath()));

        envp = tmp.toArray(new String[tmp.size()]);
        getLog().info("Using " + String.join(", ", envp));
        return envp;
    }
    
    /**
     * Running the python file.

     * @param cmd  the command to run, shall run a python file
     * @param workingDirectory the directory of the python tests
     * @return The output to add to the other outputs
     */
    private String runPython(String[] cmd, String workingDirectory, String[] envp) {
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

}
