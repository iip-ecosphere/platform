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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
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

    public static final String MD5_FILE = "python-compile.fpf";

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "python-compile.failOnError", defaultValue = "true")
    private boolean failOnError;

    @Parameter(property = "python-compile.skip", required = false, defaultValue = "false")
    private boolean skip;

    @Parameter(property = "python-compile.useHash", required = false, defaultValue = "true")
    private boolean useHash;

    @Parameter(property = "python-compile.hashDir", required = false, defaultValue = "")
    private String hashDir;

    @Parameter(property = "python-compile.ignoreText", required = false, 
        defaultValue = "imported but unused;is assigned to but never used;redefinition of unused")
    private String ignoreText;
    
    @Parameter(property = "python.binary", required = false, defaultValue = "")
    private String python;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            String pythonExecutable = PythonUtils.getPythonExecutable(python).toString();
            getLog().info("Using Python " + pythonExecutable);
    
            //search the site_packages of the python for pyflakes! Currently not doable on windows!
            
            File baseDir = project.getBasedir();
            List<File> pythonFiles = getAllPythonFiles(new File(baseDir, "/src/main/python/").getAbsolutePath(), true); 
            pythonFiles.addAll(getAllPythonFiles(new File(baseDir, "/src/test/python/").getAbsolutePath(), true));
            Map<String, String> md5Hashes = readHashFile();
            
            String output = "";
            String errorLine = "";
            boolean pyflakesExists = true;
            for (File f : checkHashes(pythonFiles, md5Hashes)) {
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
                        md5Hashes.remove(getHashFilePath(f));
                        writeHashFile(md5Hashes); // only if ok
                        throw new MojoExecutionException(errorLine);
                    }
                }
            }
            writeHashFile(md5Hashes); // only if ok
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

    /**
     * Reads the MD5 hash file.
     * 
     * @return the hash file
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> readHashFile() {
        Map<String, String> md5Hashes = new HashMap<>();
        File md5File = getHashFile();
        if (md5File.exists() && useHash) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(md5File))) {
                md5Hashes = (Map<String, String>) ois.readObject();
                getLog().info("Using hash file " + md5File);
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                getLog().warn("Cannot read existing fingerprint file '" + md5File.getName() + "': " + e.getMessage());
            }
        }
        return md5Hashes;
    }
    
    /**
     * Returns the file containing the MD5 hashes of known Python files.
     * 
     * @return the hash
     */
    public File getHashFile() {
        File result = null;
        if (hashDir != null && hashDir.length() > 0) {
            result = new File(hashDir, project.getArtifactId() + "-" + MD5_FILE);
        }
        if (null == result) {
            result = new File(project.getBuild().getDirectory(), MD5_FILE);
        }
        return result;
    }
    
    /**
     * Writes {@code md5Hashes} to the hash file.
     * 
     * @param md5Hashes the hashes to write
     */
    private void writeHashFile(Map<String, String> md5Hashes) {
        if (useHash) {
            File md5File = getHashFile();
            md5File.getParentFile().mkdirs();
            try (ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(md5File))) {
                ois.writeObject(md5Hashes);
                getLog().info("Wrote hash file " + md5File);
            } catch (IOException | ClassCastException e) {
                getLog().warn("Write fingerprint file '" + md5File + "': " + e.getMessage());
            }
        }
    }
    
    /**
     * Returns the file path to be used for MD5 hashing of {@code file}.
     * 
     * @param file the file
     * @return the file path
     */
    private String getHashFilePath(File file) {
        String path;
        try {
            path = file.getCanonicalPath();
        } catch (IOException e) {
            path = file.getAbsolutePath();
        }
        return path;
    }
    
    /**
     * Checks {@code files} for existing/known hashes.
     * 
     * @param files the files
     * @param md5Hashes the hashes, may be updated as a side effect
     * @return a subset of {@code files} to process
     */
    private List<File> checkHashes(List<File> files, Map<String, String> md5Hashes) {
        List<File> result = new ArrayList<>();
        for (File f: files) {
            if (f.exists()) {
                String path = getHashFilePath(f);
                String knownMd5 = md5Hashes.get(path);
                String md5 = null;
                try (InputStream is = Files.newInputStream(f.toPath())) {
                    md5 = DigestUtils.md5Hex(is);
                } catch (IOException e) {
                }
                if (md5 != null) {
                    md5Hashes.put(path, md5);
                }
                if (knownMd5 != null) {
                    if (!knownMd5.equals(md5)) {
                        result.add(f);
                    } else {
                        getLog().info("Skipping Python syntax check for " + f + " as unchanged.");
                    }
                } else {
                    result.add(f);
                }
            }
        }
        return result;
    }    

}
