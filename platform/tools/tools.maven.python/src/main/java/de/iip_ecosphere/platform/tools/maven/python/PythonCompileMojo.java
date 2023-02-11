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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Python compiler Mojo plugin.
 * 
 * @author Alexander Weber, SSE
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "compile-python", defaultPhase = LifecyclePhase.COMPILE)
public class PythonCompileMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "python-compile.failOnError", defaultValue = "true")
    private boolean failOnError;

    @Parameter(property = "python-compile.skip", required = false, defaultValue = "false")
    private boolean skip;
    
    @Parameter(property = "python-compile.ignoreText", required = false, 
        defaultValue = "imported but unused;is assigned to but never used;redefinition of unused")
    private String ignoreText;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
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
    
            //search the site_packages of the python for pyflakes! Currently not doable on windows!
            
            File baseDir = project.getBasedir();
            List<File> pythonFiles = getAllPythonFiles(new File(baseDir, "/src/main/python/").getAbsolutePath(), true); 
            pythonFiles.addAll(getAllPythonFiles(new File(baseDir, "/src/test/python/").getAbsolutePath(), true));
            
            String output = "";
            String errorLine = "";
            boolean pyflakesExists = true;
            for (File f : pythonFiles) {
                getLog().info("Testing Python syntax: " + f.getAbsolutePath());
                if (pyflakesExists) {
                    String[] cmd = {pythonExecutable, "-m", "pyflakes",  f.getAbsolutePath()}; 
                    output += runPythonTest(cmd);
                    if (output.contains("No module named")) {
                        pyflakesExists = !output.contains("pyflakes");
                    }
    
                } 
                if (!pyflakesExists) {
                    String[] cmd = {pythonExecutable, "-m", "py_compile", f.getAbsolutePath()};
                    output += runPythonTest(cmd);
                }
                String[] ignore = null == ignoreText ? new String[0] : ignoreText.split(";");
                if (output.length() > 0) {
                    boolean failure = false;
                    String[] outputs = output.split("\n");
                    String filteredOutput = "";
                    for (String line : outputs) {
                        boolean addLine = true;
                        // Unused import are not supposed to fail the build
                        // are there pyflake options to disable those warnings
                        if (!line.contains("import") && !line.contains("redefinition") 
                            && !line.contains("but never used")) {
                            failure = true;
                            errorLine = line;
                        }
                        // flakes8 shall have options to switch individual warnings on/off
                        if (isContained(line, ignore)) {
                            addLine = false;
                        }
                        if (addLine) {
                            if (filteredOutput.length() > 0) {
                                filteredOutput = filteredOutput + "\n";
                            }
                            filteredOutput += line;
                        }
                    }
                    if (filteredOutput.length() > 0) {
                        getLog().info(filteredOutput);
                    }
                    if (failure && failOnError) {
                        throw new MojoExecutionException(errorLine);
                    }
                }
            }
        } else {
            getLog().info("Skipping Python compiler execution");
        }
    }
    
    /**
     * Returns whether at least one of the {@code substrings} are contained in {@code text}.
     * 
     * @param text the text to search for
     * @param substrings the substrings
     * @return {@code true} if at least one of {@code substrings} is contained in {@code text}
     */
    private static boolean isContained(String text, String[] substrings) {
        boolean contained = false;
        for (int i = 0; !contained && i < substrings.length; i++) {
            contained = text.contains(substrings[i]);
        }
        return contained;
    }
    
    /**
     * Running the syntax check for the python Files.
     * @param cmd  the command to run, either utilising pyflaks or py_compile
     * @return The output to add to the other outputs
     */
    public static String runPythonTest(String[] cmd) {
        Process process;
        String output = "";
        try {
            process = Runtime.getRuntime().exec(cmd);
            output = readProcessOutput(process.getInputStream());
            output += readProcessOutput(process.getErrorStream());
            // only test if error is due to missing pyflakes!
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return output;
    }
    
    /**
     * Shall take in input stream of processes to collect console output.
     * 
     * @param stream Process to be observed.
     * @return The read Lines from the process.
     * @throws IOException If the reading of the lines does fail.
     */
    static String readProcessOutput(InputStream stream) throws IOException {
        StringBuffer output = new StringBuffer();
        String line = "";
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        while ((line = bufferedReader.readLine()) != null) {
            output.append(line);
            output.append("\n");
        }
        return output.toString();
    }
    
    /**
     * Give a list of files in a directory.
     * 
     * @param directory the path to the directory as String.
     * @param recurse if all nested directories shall be considered or if only the files in the top-level 
     *     directory shall be returned
     * @return list of files in directory
     */
    static List<File> getAllPythonFiles(String directory, boolean recurse) {
        List<File> pythonFiles = new ArrayList<File>();
        File file = new File(directory);
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (null != files) {
                    for (File f : files) {
                        if (f.isDirectory() && recurse) {
                            pythonFiles.addAll(getAllPythonFiles(f.getAbsolutePath(), true));  
                        } else if (!f.isDirectory() && f.getAbsolutePath().endsWith(".py")) {
                            pythonFiles.add(f);
                        }
                    }
                }
            } else {
                pythonFiles.add(file);
            }
        }
        return pythonFiles;
    }

}
