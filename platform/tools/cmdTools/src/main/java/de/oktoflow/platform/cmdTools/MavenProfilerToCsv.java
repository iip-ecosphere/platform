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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts one or more Maven Profiler JSON reports into a CSV file that can be
 * imported into spreadsheet applications such as Microsoft Excel or LibreOffice
 * Calc.
 *
 * <p>The converter accepts either a single JSON report or a directory
 * containing multiple reports. In the latter case, all {@code *.json} files are
 * processed recursively and combined into a single CSV file.</p>
 *
 * <p>The generated CSV contains one row per executed Maven mojo. Build-level and
 * project-level information is repeated for each mojo execution to simplify
 * filtering, sorting and the creation of pivot tables.</p>
 *
 * <p>The output uses UTF-8 encoding with a BOM and semicolons as separators,
 * which provides good compatibility with Excel in locales where commas are used
 * as decimal separators.</p>
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * java profiler.MavenProfilerToCsv \
 *     target/profiler \
 *     target/profiler-results.csv
 * }</pre>
 *
 * @author ChatGPT, javadoc copied into as output window too small
 */
public final class MavenProfilerToCsv {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final char SEPARATOR = ';';

    /**
     * Prevents external creation.
     */
    private MavenProfilerToCsv() {
    }

    /**
     * Converts one or more Maven Profiler JSON reports into a CSV file.
     *
     * <p>The first argument specifies either a single JSON report or a directory
     * containing JSON reports. The second argument specifies the CSV file to be
     * created.</p>
     *
     * @param args
     *     the command line arguments:
     *     <ol>
     *       <li>the JSON report file or directory</li>
     *       <li>the output CSV file</li>
     *     </ol>
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(
                "Usage: java profiler.MavenProfilerToCsv "
                    + "<report.json|report-directory> <output.csv>"
            );
            System.exit(2);
        }

        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);

        try {
            List<Path> reports = findReports(input);

            if (reports.isEmpty()) {
                throw new IOException("No JSON reports found under " + input);
            }

            Path parent = output.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(
                    output,
                    StandardCharsets.UTF_8)) {

                // UTF-8 BOM helps Excel recognize the encoding correctly.
                writer.write('\uFEFF');

                writeRow(
                    writer,
                    "source_file",
                    "build_name",
                    "profile_name",
                    "build_time_ms",
                    "goals",
                    "date",
                    "project",
                    "project_time_ms",
                    "mojo",
                    "mojo_time_ms"
                );

                for (Path report : reports) {
                    convertReport(report, writer);
                }
            }

            System.out.println(
                "Converted " + reports.size() + " report(s) to " + output
            );

        } catch (IOException exception) { // ChatGPT tends towards generic exceptions, not our convention...
            System.err.println("Conversion failed: " + exception.getMessage());
            exception.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * Determines the profiler report files to be converted.
     *
     * @param input
     *     the input file or directory
     * @return the JSON report files to process
     * @throws IOException
     *     if the input does not exist or the directory cannot be traversed
     */
    private static List<Path> findReports(Path input) throws IOException {
        if (Files.isRegularFile(input)) {
            return List.of(input);
        }

        if (!Files.isDirectory(input)) {
            throw new IOException("Input does not exist: " + input);
        }

        try (Stream<Path> stream = Files.walk(input)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName()
                    .toString()
                    .toLowerCase()
                    .endsWith(".json"))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        }
    }

    /**
     * Converts a single Maven Profiler JSON report into CSV rows.
     *
     * @param report
     *     the profiler report to convert
     * @param writer
     *     the destination CSV writer
     * @throws IOException
     *     if the report cannot be read or the CSV cannot be written
     */
    private static void convertReport(Path report, BufferedWriter writer) throws IOException {
        JsonNode root = JSON.readTree(report.toFile());

        String sourceFile = report.toAbsolutePath().normalize().toString();
        String buildName = text(root, "name");
        String profileName = text(root, "profile_name");
        String buildTime = milliseconds(root.path("time"));
        String goals = text(root, "goals");
        String date = text(root, "date");

        JsonNode projects = root.path("projects");

        if (!projects.isArray() || projects.isEmpty()) {
            writeRow(
                writer,
                sourceFile,
                buildName,
                profileName,
                buildTime,
                goals,
                date,
                "",
                "",
                "",
                ""
            );
            return;
        }

        for (JsonNode project : projects) {
            String projectName = firstText(project, "project", "name");
            String projectTime = milliseconds(project.path("time"));

            JsonNode mojos = firstNode(project, "mojos", "mojosWithTime");

            if (mojos == null || !mojos.isArray() || mojos.isEmpty()) {
                writeRow(
                    writer,
                    sourceFile,
                    buildName,
                    profileName,
                    buildTime,
                    goals,
                    date,
                    projectName,
                    projectTime,
                    "",
                    ""
                );
                continue;
            }

            List<JsonNode> sortedMojos = new ArrayList<>();
            mojos.forEach(m -> sortedMojos.add(m));
            Collections.sort(sortedMojos, (m1, m2) 
                -> firstText(m1, "mojo", "entry").compareTo(firstText(m2, "mojo", "entry")));
            
            for (JsonNode mojo : sortedMojos) {
                String mojoName = firstText(mojo, "mojo", "entry");
                String mojoTime = milliseconds(mojo.path("time"));

                writeRow(
                    writer,
                    sourceFile,
                    buildName,
                    profileName,
                    buildTime,
                    goals,
                    date,
                    projectName,
                    projectTime,
                    mojoName,
                    mojoTime
                );
            }
        }
    }

    /**
     * Returns the first existing child node for the given field names.
     *
     * <p>This method supports different JSON schema versions by trying multiple
     * alternative field names.</p>
     *
     * @param parent
     *     the parent JSON node
     * @param fieldNames
     *     the candidate field names in lookup order
     * @return the first matching node or {@code null} if none exists
     */
    private static JsonNode firstNode(JsonNode parent, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = parent.get(fieldName);
            if (value != null && !value.isNull()) {
                return value;
            }
        }
        return null;
    }

    /**
     * Returns the textual value of the first existing field.
     *
     * @param parent
     *     the parent JSON node
     * @param fieldNames
     *     the candidate field names in lookup order
     * @return the field value or the empty string if no matching field exists
     */
    private static String firstText(JsonNode parent, String... fieldNames) {
        JsonNode node = firstNode(parent, fieldNames);
        return node == null ? "" : node.asText("");
    }

    /**
     * Returns the textual value of a field.
     *
     * @param parent
     *     the parent JSON node
     * @param fieldName
     *     the field name
     * @return the field value or the empty string if the field is missing
     */
    private static String text(JsonNode parent, String fieldName) {
        JsonNode value = parent.get(fieldName);
        return value == null || value.isNull() ? "" : value.asText("");
    }

    /**
     * Converts a profiler time value into milliseconds.
     *
     * <p>Supported input formats include numeric JSON values as well as textual
     * representations such as {@code "30706 ms"} or {@code "1.25 s"}.</p>
     *
     * <p>If an unknown format is encountered, the original value is returned
     * unchanged so that timing information is not lost.</p>
     *
     * @param value
     *     the JSON value representing a duration
     * @return the duration in milliseconds or the original value if it cannot be
     *     interpreted
     */
    private static String milliseconds(JsonNode value) {
        if (value == null || value.isNull()) {
            return "";
        }

        if (value.isNumber()) {
            return value.asText();
        }

        String text = value.asText("").trim();
        if (text.isEmpty()) {
            return "";
        }

        try {
            if (text.endsWith(" ms")) {
                return normalizeNumber(
                    text.substring(0, text.length() - 3).trim()
                );
            }

            if (text.endsWith(" s")) {
                double seconds = Double.parseDouble(
                    text.substring(0, text.length() - 2).trim()
                );
                return Long.toString(Math.round(seconds * 1000.0));
            }

            return normalizeNumber(text);

        } catch (NumberFormatException exception) {
            // Preserve unknown formats instead of losing the value.
            return text;
        }
    }

    /**
     * Formats a numeric string by removing an unnecessary fractional part.
     *
     * <p>For example, {@code "42.0"} becomes {@code "42"} while
     * {@code "42.5"} remains unchanged.</p>
     *
     * @param value
     *     the numeric string
     * @return the normalized representation
     */    
    private static String normalizeNumber(String value) {
        double number = Double.parseDouble(value);

        if (number == Math.rint(number)) {
            return Long.toString((long) number);
        }

        return Double.toString(number);
    }

    /**
     * Writes a single CSV row.
     *
     * @param writer
     *     the destination writer
     * @param values
     *     the column values
     * @throws IOException
     *     if writing fails
     */
    private static void writeRow(BufferedWriter writer, String... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                writer.write(SEPARATOR);
            }
            writer.write(csv(values[i]));
        }
        writer.newLine();
    }

    /**
     * Escapes a value according to RFC&nbsp;4180 CSV rules.
     *
     * <p>Values containing separators, quotation marks or line breaks are enclosed
     * in quotation marks. Embedded quotation marks are escaped by duplication.</p>
     *
     * @param value
     *     the value to escape
     * @return the escaped CSV representation
     */
    private static String csv(String value) {
        if (value == null) {
            return "";
        }

        boolean quote = value.indexOf(SEPARATOR) >= 0
            || value.indexOf('"') >= 0
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0;

        if (!quote) {
            return value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    
}
