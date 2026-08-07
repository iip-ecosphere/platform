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
 * Extracts Maven surefire test timings to CSV.
 * 
 * @author ChatGPT
 */
public class MavenTestTimeExtractor {

    private static final Pattern TEST_PATTERN = Pattern.compile(
        "Tests run:\\s*\\d+," 
        + "\\s*Failures:\\s*\\d+," 
        + "\\s*Errors:\\s*\\d+," 
        + "\\s*Skipped:\\s*\\d+," 
        + "\\s*Time elapsed:\\s*([0-9.]+)\\s*s\\s*-\\s*in\\s+(.+)$"
    );

    /**
     * Extracts maven surefire test times.
     * 
     * @param args the first file is the maven log file, the second the output CSV to be created
     */
    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java MavenTestTimeExtractor <maven-log> <output.csv>");
            System.exit(1);
        }

        Path inputFile = Paths.get(args[0]);
        Path outputFile = Paths.get(args[1]);

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
             BufferedWriter writer = Files.newBufferedWriter(outputFile)) {

            writer.write("testClass,elapsedTime");
            writer.newLine();

            String line;

            while ((line = reader.readLine()) != null) {

                Matcher matcher = TEST_PATTERN.matcher(line);

                if (matcher.find()) {
                    String elapsedTime = matcher.group(1);
                    String testClass = matcher.group(2).trim();

                    writer.write(CsvUtils.csvEscape(testClass));
                    writer.write(",");
                    writer.write(elapsedTime);
                    writer.newLine();
                }
            }

            System.out.println("Maven test time CSV written to: " + outputFile);

        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
