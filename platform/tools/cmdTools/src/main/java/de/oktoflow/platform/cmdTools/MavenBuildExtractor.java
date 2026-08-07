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

/**
 * Combines {@link MavenTestTimeExtractor} and {@link PluginLoadingTimeExtractor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MavenBuildExtractor {
    
    private static final String LOG_FILE_NAME = System.getProperty("okto.maven.logFile", "mvn.log");
    private static final String LOG_OUT_FILE_NAME = System.getProperty("okto.maven.logCsvFile", "mvn.csv");
    private static final String PLUGIN_FILE_NAME = System.getProperty("okto.maven.pluginsFile", "null-output.txt");
    private static final String PLUGIN_OUT_FILE_NAME = System.getProperty("okto.maven.pluginsCsvFile", "plugins.csv");
    
    /**
     * Runs the extractors. Maven log shall be directly in the specified maven folder (default file name "mvn.log"); 
     * extracted CSV will be written to the specified output folder (default file name "mvn.csv"). Plugin load timing 
     * file shall be in "target/surefire-reports" in the specified maven folder (default file name "null-output.log"); 
     * extracted CSV will be written to the specified output folder (default file name "plugins.csv").
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
            MavenTestTimeExtractor.main(new String[] {logFile.getPath(), logOutFile.getPath()});
            
            File pluginFile = new File(inputFolder, "target/surefire-reports/" + PLUGIN_FILE_NAME);
            File pluginOutFile = new File(outputFolder, PLUGIN_OUT_FILE_NAME);
            PluginLoadingTimeExtractor.main(new String[] {pluginFile.getPath(), pluginOutFile.getPath()});
        } else {
            System.err.println("Input folder " + args[0] + " does not exist.");
            System.exit(1);
        }
    }

}
