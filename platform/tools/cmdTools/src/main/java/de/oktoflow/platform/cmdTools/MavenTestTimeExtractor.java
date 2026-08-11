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
import java.util.function.Function;
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
        
        System.out.println("Maven Test Time Extractor: " + args[0] + " " + args[1]);
        process(Paths.get(args[0]), Paths.get(args[1]), TEST_PATTERN, null, m -> m.group(1), m -> m.group(2));
    }
    
    // checkstyle: stop parameter number check

    /**
     * Processes the input file to the output file.
     * 
     * @param inputFile the input file
     * @param outputFile the output file
     * @param pattern the test output regex
     * @param skipPattern optional skip pattern, skip input lines until this pattern is found, may be <b>null</b>
     * @param elapsedTimeFn a function that returns the elapsed time from a matching matcher
     * @param testClassFn a function that returns the test class name from a matching matcher
     */
    private static void process(Path inputFile, Path outputFile, Pattern pattern, Pattern skipPattern, 
        Function<Matcher, String> elapsedTimeFn, Function<Matcher, String> testClassFn) {
        try (BufferedReader reader = Files.newBufferedReader(inputFile);
            BufferedWriter writer = Files.newBufferedWriter(outputFile)) {

            writer.write("testClass,elapsedTime");
            writer.newLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (skipPattern != null) {
                    Matcher matcher = skipPattern.matcher(line);
                    if (!matcher.matches()) {
                        continue;
                    }
                    skipPattern = null;
                }
               
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String elapsedTime = elapsedTimeFn.apply(matcher);
                    String testClass = testClassFn.apply(matcher).trim();
 
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

    // checkstyle: resume parameter number check

    /**
     * Alternative execution point, for env logs.
     * 
     * @param inputFile the env log input file
     * @param outputFile the output file
     */
    public static void readTail(File inputFile, File outputFile) {
        Pattern testPattern = Pattern.compile("\\s*(\\S+):\\s*([0-9.]+)\\s*ms$");
        Pattern skipPattern = Pattern.compile("Times:\\s*$");
        process(inputFile.toPath(), outputFile.toPath(), testPattern, skipPattern, m->m.group(2), m->m.group(1));
    }

}
