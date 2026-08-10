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

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

/**
 * Extracts loading times from a {@code null-output} surefire test execution.
 * 
 * @author ChatGPT
 */
public class PluginLoadingTimeExtractor {

    private static final Pattern PLUGIN_PATTERN =
        Pattern.compile("Plugin\\s+(.+?)\\s+registered");

    private static final Pattern TIME_PATTERN =
        Pattern.compile("Loading took\\s+(\\d+)\\s+ms");

    /**
     * Performs the conversion.
     * 
     * @param args command line arguments, input file and output file
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: java PluginLoadingTimeExtractor <input-file> <output.csv>");
            System.exit(1);
        }

        System.out.println("Plugin Loading Time Extractor: " + args[0] + " " + args[1]);
        Path inputFile = Paths.get(args[0]);
        Path outputFile = Paths.get(args[1]);

        List<String> pendingPlugins = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
             BufferedWriter writer = Files.newBufferedWriter(outputFile)) {

            writer.write("plugin,time");
            writer.newLine();

            String line;

            while ((line = reader.readLine()) != null) {

                Matcher pluginMatcher = PLUGIN_PATTERN.matcher(line);

                if (pluginMatcher.find()) {
                    pendingPlugins.add(pluginMatcher.group(1));
                    continue;
                }

                Matcher timeMatcher = TIME_PATTERN.matcher(line);

                if (timeMatcher.find()) {
                    String loadingTime = timeMatcher.group(1);

                    for (String pluginName : pendingPlugins) {
                        writer.write(CsvUtils.csvEscape(pluginName));
                        writer.write(",");
                        writer.write(loadingTime);
                        writer.newLine();
                    }

                    pendingPlugins.clear();
                }
            }

            System.out.println("Plugin loading CSV written to: " + outputFile);

        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }


    
}