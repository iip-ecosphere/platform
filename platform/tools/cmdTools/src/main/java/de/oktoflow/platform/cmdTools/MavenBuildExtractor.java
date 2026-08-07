/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.cmdTools;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * Combines {@link MavenTestTimeExtractor} and {@link PluginLoadingTimeExtractor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MavenBuildExtractor {
    
    private static final String PREFIX_INDICATOR = ".*";
    private static final String LOG_FILE_NAME = System.getProperty("okto.maven.logFile", "mvn.log");
    private static final String LOG_OUT_FILE_NAME = System.getProperty("okto.maven.logCsvFile", "mvn.csv");
    private static final String OUT_FILE_SUFFIX = System.getProperty("okto.maven.outFileSuffix", "");
    private static final String PLUGIN_FILE_NAME = System.getProperty("okto.maven.pluginsFile", "null-output.txt");
    private static final String PLUGIN_OUT_FILE_NAME = System.getProperty("okto.maven.pluginsCsvFile", "plugins.csv");
    private static final String BASYY_FILE_NAME = System.getProperty("okto.maven.basyxFile", PREFIX_INDICATOR 
        + "BaSyxTest-output.txt");
    private static final String BASYX_OUT_FILE_NAME = System.getProperty("okto.maven.basyxCsvFile", "basyx.csv");
    
    /**
     * Runs the extractors. Maven log shall be directly in the specified maven folder (default file name "mvn.log"); 
     * extracted CSV will be written to the specified output folder (default file name "mvn.csv"). Plugin load timing 
     * file shall be in "target/surefire-reports" in the specified maven folder (default file name "null-output.log"); 
     * extracted CSV will be written to the specified output folder (default file name "plugins.csv"). Basyx test log 
     * output file shall be in "target/surefire-reports" in the specified maven folder (default file name ending with 
     * "BaSyxTest-output.txt"); extracted CSV will be written to the specified output folder (default 
     * file name "basyx.csv").
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java MavenBuildExtractor <maven-folder> <output-folder>");
            System.exit(1);
        }
        
        File inputFolder = new File(args[0]);
        File outputFolder = new File(args[1]);
        
        if (inputFolder.isDirectory()) {
            if (outputFolder.isDirectory()) {
                outputFolder.mkdirs();
            }

            File logFile = new File(inputFolder, LOG_FILE_NAME);
            File logOutFile = new File(outputFolder, LOG_OUT_FILE_NAME);
            logOutFile = appendSuffix(logOutFile, OUT_FILE_SUFFIX);
            MavenTestTimeExtractor.main(new String[] {logFile.getPath(), logOutFile.getPath()});

            final String surefireReportsPath = "target/surefire-reports/";
            
            File pluginFile = new File(inputFolder, surefireReportsPath + PLUGIN_FILE_NAME);
            if (pluginFile.exists()) { // non-plugin execution
                File pluginOutFile = new File(outputFolder, PLUGIN_OUT_FILE_NAME);
                pluginOutFile = appendSuffix(pluginOutFile, OUT_FILE_SUFFIX);
                PluginLoadingTimeExtractor.main(new String[] {pluginFile.getPath(), pluginOutFile.getPath()});
            }

            File basyxFile = null;
            String basyxFileName = BASYY_FILE_NAME;
            if (basyxFileName.startsWith(PREFIX_INDICATOR)) {
                File surefireFolder = new File(inputFolder, surefireReportsPath);
                File[] surefireFiles = surefireFolder.listFiles();
                String name = basyxFileName.substring(PREFIX_INDICATOR.length());
                if (surefireFiles != null) {
                    Optional<File> found = Arrays.stream(surefireFiles)
                        .filter(f -> f.getName().endsWith(name))
                        .findFirst();
                    if (found.isPresent()) {
                        basyxFile = found.get();
                    }
                }
            } else {
                basyxFile = new File(inputFolder, surefireReportsPath + basyxFileName);
            }
            if (basyxFile != null && basyxFile.exists()) { // applied wherever
                File basyxOutFile = new File(outputFolder, BASYX_OUT_FILE_NAME);
                basyxOutFile = appendSuffix(basyxOutFile, OUT_FILE_SUFFIX);
                BaSyxTestCaseTimeExtractor.main(new String[] {basyxFile.getPath(), basyxOutFile.getPath()});
            }
        } else {
            System.err.println("Input folder " + args[0] + " does not exist.");
            System.exit(1);
        }
    }

    /**
     * Appends {@code suffix} to the file name but before a possible extension (after last dot).
     * 
     * @param file the file
     * @param suffix the suffix, may be <b>null</b> or empty
     * @return the modified file object
     */
    public static File appendSuffix(File file, String suffix) {
        File result = file;
        if (suffix != null && suffix.length() > 0) {
            String name = file.getName();
    
            int dot = name.lastIndexOf('.');
    
            String newName;
            if (dot > 0) {
                String baseName = name.substring(0, dot);
                String extension = name.substring(dot);
                newName = baseName + suffix + extension;
            } else {
                newName = name + suffix;
            }

            result = new File(file.getParentFile(), newName);
        }
        return result;
    }

}
