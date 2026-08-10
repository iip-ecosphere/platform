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
import java.util.regex.*;

/**
 * Extracts sub-test excecution information from test log.
 * 
 * @author ChatGPT
 */
public class BaSyxTestCaseTimeExtractor {

    private static final Pattern TEST_PATTERN = Pattern.compile(
        "TIME:\\s*(\\S+)\\s*protocols\\s*(\\S+)\\s*hasAuth:\\s*(\\S+)\\s*ssl:\\s*(\\S+)\\s*(\\d+)(.*)$" 
    );
    
    /**
     * Extracts maven surefire test times.
     * 
     * @param args the first file is the maven log file, the second the output CSV to be created
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java BaSyxTestCaseTimeExtractor <maven-log> <output.csv>");
            System.exit(1);
        }

        System.out.println("BaSyx Test Case Time Extractor: " + args[0] + " " + args[1]);
        Path inputFile = Paths.get(args[0]);
        Path outputFile = Paths.get(args[1]);

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
             BufferedWriter writer = Files.newBufferedWriter(outputFile)) {

            writer.write("case,protocols,hasAuth,ssl,time");
            writer.newLine();

            String line;

            while ((line = reader.readLine()) != null) {

                Matcher matcher = TEST_PATTERN.matcher(line);

                if (matcher.find()) {
                    String caseName = matcher.group(1);
                    String protocols = matcher.group(2);
                    String hasAuthentication = matcher.group(3);
                    String hasSSL = matcher.group(4);
                    String elapsedTime = matcher.group(5);

                    writer.write(CsvUtils.csvEscape(caseName));
                    writer.write(",");
                    writer.write(CsvUtils.csvEscape(protocols));
                    writer.write(",");
                    writer.write(CsvUtils.csvEscape(hasAuthentication));
                    writer.write(",");
                    writer.write(CsvUtils.csvEscape(hasSSL));
                    writer.write(",");
                    writer.write(elapsedTime);
                    writer.newLine();
                }
            }

            System.out.println("Basyx CSV written to: " + outputFile);

        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
}