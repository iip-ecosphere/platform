/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.cmdTools.jenkins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * Extracts Maven and Ant total times from historical Jenkins Freestyle builds. 
 * 
 * @author ChatGPT
 */
public final class JenkinsMavenTimings {
    
    private static final Pattern MAVEN = Pattern.compile("\\[INFO\\]\\s+Total time:\\s*(.+?)\\s*$");
    private static final Pattern ANT = Pattern.compile(
        "^\\s*(?:(?:\\d{4}-\\d{2}-\\d{2}\\s+)?\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,3})?\\s+)?Total time:\\s*(.+?)\\s*$");
    private static final Pattern ANT_START = Pattern.compile(
        "^\\s*(?:(?:\\d{4}-\\d{2}-\\d{2}\\s+)?\\d{2}:\\d{2}:\\d{2}(?:\\.\\d{1,3})?\\s+)?Buildfile:\\s+.+$");
    private static final Pattern ANT_UNIT = Pattern.compile("(\\d+)\\s+(day|hour|minute|second)s?");
    private static final Pattern SECONDS = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*s");
    private static final Pattern MINUTES = Pattern.compile("(\\d+):([0-9]+(?:\\.[0-9]+)?)\\s*min");
    private static final Pattern HOURS = Pattern.compile("(?:(\\d+):)?(\\d+):(\\d+(?:\\.[0-9]+)?)\\s*h");
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter DATE 
        = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Pattern STAMP_LINE 
        = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) (.*)$");

    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final String auth;

    /**
     * Creates an instance.
     */
    private JenkinsMavenTimings() {
        String user = System.getenv("JENKINS_API_USER");
        String token = System.getenv("JENKINS_API_TOKEN");
        auth = user != null && token != null && !user.isEmpty() && !token.isEmpty()
            ? "Basic " + Base64.getEncoder().encodeToString((user + ":" + token).getBytes(StandardCharsets.UTF_8)) 
            : null;
    }

    /**
     * Creates an (authorized) HTTP request.
     * 
     * @param url the URL to request
     * @return the HTTP request
     */
    private HttpRequest request(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).GET();
        if (auth != null) {
            builder.header("Authorization", auth);
        }
        return builder.build();
    }

    /**
     * Creates a Jenkins HTML tree query.
     * 
     * @param tree the tree specification
     * @return the query
     */
    private static String treeQuery(String tree) {
        return "?tree=" + URLEncoder.encode(tree, StandardCharsets.UTF_8);
    }

    /**
     * Fetches data from {@code url}.
     * 
     * @param url the URL to query
     * @return the result
     * @throws IOException if I/O issues occur
     * @throws InterruptedException if timeouts occur
     */
    private String fetch(String url) throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request(url), 
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        return response.body();
    }

    /**
     * Stores the readings/timings.
     * 
     * @author ChatGPT
     */
    private static final class Timings {
        private Double mavenSeconds;
        private Double antSeconds;
        private Instant firstLog;
        private Instant lastLog;
        private Instant antEnd;
        private Instant antStart;
    }

    /**
     * Determines the last timings.
     * 
     * @param url the URL to query
     * @param timestamped whether timestamped readings shall be queried/expected, i.e., log lines that start with a 
     * timestamp as produced by the Jenkins plugin "secondary timestamper".
     * 
     * @return the time readings
     * @throws IOException if I/O issues occur during the a request
     * @throws InterruptedException if timeouts occur during the a request
     */
    private Timings lastTimings(String url, boolean timestamped) throws IOException, InterruptedException {
        HttpResponse<InputStream> response = client.send(request(url), HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            response.body().close();
            throw new IOException("HTTP " + response.statusCode() + " for " + url);
        }
        Timings timings = new Timings();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Instant currentStamp = null;
                if (timestamped) {
                    Matcher stamp = STAMP_LINE.matcher(line);
                    if (stamp.matches()) {
                        Instant instant = LocalDateTime.parse(stamp.group(1), STAMP)
                            .atZone(ZoneId.systemDefault()).toInstant();
                        currentStamp = instant;
                        if (timings.firstLog == null) {
                            timings.firstLog = instant;
                        }
                        timings.lastLog = instant;
                        line = stamp.group(2);
                    }
                }
                if (timestamped && timings.antStart == null && currentStamp != null
                        && ANT_START.matcher(line).matches()) {
                    timings.antStart = currentStamp;
                }
                if (!line.contains("Total time:")) {
                    continue;
                }
                Matcher maven = MAVEN.matcher(line);
                if (maven.find()) {
                    Double parsed = parseTime(maven.group(1));
                    if (parsed != null) {
                        timings.mavenSeconds = parsed;
                    }
                }
                Matcher ant = ANT.matcher(line);
                if (ant.matches()) {
                    Double parsed = parseAntSeconds(ant.group(1));
                    if (parsed != null) {
                        timings.antSeconds = parsed;
                        if (timestamped) {
                            timings.antEnd = currentStamp;
                        }
                    }
                }
            }
        }
        return timings;
    }

    /**
     * Calculates the seconds between {@code start} and {@code end}.
     * 
     * @param start the start (may be <b>null</b>)
     * @param end the end (may be <b>null</b>)
     * @return the seconds between (may be <b>null</b> if one of the inputs was <b>null</b>)
     */
    private static Double secondsBetween(Instant start, Instant end) {
        return start == null || end == null ? null : Duration.between(start, end).toMillis() / 1000.0;
    }

    /**
     * Creates a timestamp.
     * 
     * @param value the value to turn into a time stamp, may be <b>null</b>
     * @return the timestamp, may be <b>null</b> if {@code instant} was null
     */
    private static String stamp(Instant value) {
        return value == null ? "" : STAMP.withZone(ZoneId.systemDefault()).format(value);
    }

    /**
     * Parses the given {@code value} in ANT format.
     * 
     * @param value the seconds in ANT format
     * @return the seconds as value, may be <b>null</b> for none
     */
    private static Double parseAntSeconds(String value) {
        Matcher matcher = ANT_UNIT.matcher(value);
        double seconds = 0;
        boolean found = false;
        while (matcher.find()) {
            found = true;
            long amount = Long.parseLong(matcher.group(1));
            switch (matcher.group(2)) {
            case "day": 
                seconds += amount * 86400; 
                break;
            case "hour": 
                seconds += amount * 3600; 
                break;
            case "minute": 
                seconds += amount * 60; 
                break;
            case "second": 
                seconds += amount; 
                break;
            default: break;
            }
        }
        return found ? seconds : null;
    }

    /**
     * Parses time in various formats, as given by the matchers defined in this class.
     * 
     * @param value the value to parse from
     * @return the parsed value
     */
    private static Double parseTime(String value) {
        Matcher m = SECONDS.matcher(value);
        if (m.matches()) {
            return Double.parseDouble(m.group(1));
        }
        m = MINUTES.matcher(value);
        if (m.matches()) {
            return 60 * Double.parseDouble(m.group(1)) + Double.parseDouble(m.group(2));
        }
        m = HOURS.matcher(value);
        if (m.matches()) {
            return 3600 * Double.parseDouble(m.group(1) == null ? "0" : m.group(1))
                + 60 * Double.parseDouble(m.group(2)) + Double.parseDouble(m.group(3));
        }
        return null;
    }

    /**
     * Unescapes the given string. Jenkins URLs and job names normally contain only simple JSON escapes.
     * 
     * @param text the string
     * @return the unescaped string
     */
    private static String unescape(String text) {
        return text.replace("\\/", "/").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    /**
     * Turns {@code value} into a CSV value.
     * 
     * @param value the value
     * @return the CSV cvalue
     */
    private static String csv(Object value) {
        String s = value == null ? "" : value.toString();
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    /**
     * Turns a number into an US-formatted number (CSV, Excel) with three significant digits.
     * 
     * @param value the value
     * @return the formatted number
     */
    private static String number(Double value) {
        return value == null ? "" : String.format(Locale.US, "%.3f", value);
    }

    /**
     * Reads command line options.
     * 
     * @param args the command line arguments
     * @return the options
     */
    private static Map<String, String> options(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 >= args.length || !args[i].startsWith("--")) {
                throw new IllegalArgumentException("Expected --option value pairs");
            }
            options.put(args[i], args[i + 1]);
        }
        return options;
    }
    
    /**
     * Reads the timings and creates a respective object.
     * 
     * @param timestamps whether timestamps shall be considered at all
     * @param job the job names
     * @param build the build identifiers
     * @return the timings
     * @throws IOException if I/O issues occur during the a request
     * @throws InterruptedException if timeouts occur during the a request
     */
    private Timings readTimings(boolean timestamps, String[] job, String[] build) 
        throws IOException, InterruptedException {
        Timings timings;
        if (timestamps) {
            String timeQuery = "?time=" + URLEncoder.encode("yyyy-MM-dd HH:mm:ss.SSS", 
                StandardCharsets.UTF_8) + "&appendLog";
            try {
                timings = lastTimings(build[1] + "timestamps/" + timeQuery, true);
                if (timings.firstLog == null) {
                    System.err.println("No Timestamper data for " + job[0] + " #" + build[0]
                            + "; falling back to ordinary console log");
                    timings = lastTimings(build[1] + "consoleText", false);
                }
            } catch (IOException e) {
                System.err.println("Timestamper unavailable for " + job[0] + " #" + build[0]
                        + ": " + e.getMessage() + "; falling back to console log");
                timings = lastTimings(build[1] + "consoleText", false);
            }
        } else {
            timings = lastTimings(build[1] + "consoleText", false);
        }
        return timings;
    }

    /**
     * Turns build/job to a CSV row.
     * 
     * @param timestamps 
     * @param job the job names
     * @param build the build identifiers
     * @param extractedAt the time of execution of this program/data extraction
     * @return the row or <b>null</b> if the data cannot be extracted, e.g., the job is currently building
     * @throws IOException if I/O issues occur during the a request
     * @throws InterruptedException if timeouts occur during the a request
     */
    private String toCsv(boolean timestamps, String[] job, String[] build, String extractedAt) 
        throws IOException, InterruptedException {
        String info = fetch(build[1] + "api/json" + treeQuery("building,duration,timestamp,result"));
        if (info.contains("\"building\":true")) {
            return null;
        }
        Matcher duration = Pattern.compile("\"duration\":(\\d+)").matcher(info);
        Matcher timestamp = Pattern.compile("\"timestamp\":(\\d+)").matcher(info);
        Matcher result = Pattern.compile("\"result\":\"([^\"]+)\"").matcher(info);
        if (!duration.find() || !timestamp.find()) {
            throw new IOException("Missing build metadata");
        }
        double jenkinsSeconds = Long.parseLong(duration.group(1)) / 1000.0;
        Timings timings = readTimings(timestamps, job, build);
        Double jenkinsAntDifference = timings.antSeconds == null ? null 
            : jenkinsSeconds - timings.antSeconds;
        Double antMavenDifference = timings.antSeconds == null || timings.mavenSeconds == null
                ? null : timings.antSeconds - timings.mavenSeconds;
        String date = DATE.format(Instant.ofEpochMilli(Long.parseLong(timestamp.group(1))));
        String row = String.join(";", csv(job[0]), build[0], csv(date), csv(extractedAt),
                csv(result.find() ? result.group(1) : ""), number(jenkinsSeconds),
                number(timings.antSeconds), number(timings.mavenSeconds),
                number(jenkinsAntDifference), number(antMavenDifference));
        if (timestamps) {
            Double before = secondsBetween(timings.firstLog, timings.antStart);
            Double observed = secondsBetween(timings.antStart, timings.antEnd);
            Double after = secondsBetween(timings.antEnd, timings.lastLog);
            Double span = secondsBetween(timings.firstLog, timings.lastLog);
            row += ";" + String.join(";", csv(stamp(timings.firstLog)),
                csv(stamp(timings.lastLog)), csv(stamp(timings.antStart)), csv(stamp(timings.antEnd)),
                number(before), number(observed), number(after), number(span));
        }
        return row;
    }    
    
    /**
     * Processes jobs to build identifications.
     * 
     * @param job the jobs to process
     * @param limit the maximum number of builds to extract
     * @throws IOException if I/O issues occur during the a request
     * @throws InterruptedException if timeouts occur during the a request
     */
    private List<String[]> processJob(String[] job, int limit) throws IOException, InterruptedException {
        System.err.println("Processing " + job[0]);
        List<String[]> builds = new ArrayList<>();
        if (limit == 1) {
            // Use the latest completed build directly; no build-list parsing needed.
            String buildUrl = job[1] + "lastCompletedBuild/";
            try {
                String info = fetch(buildUrl + "api/json" + treeQuery("number,url"));
                Matcher numberMatch = Pattern.compile("number[^:]*:\\s*(\\d+)").matcher(info);
                if (numberMatch.find()) {
                    builds.add(new String[]{numberMatch.group(1), buildUrl});
                } else {
                    System.err.println("No completed build number for " + job[0]);
                }
            } catch (IOException e) {
                System.err.println("Skipping " + job[0] + ": " + e.getMessage());
            }
        } else {
            String buildList = fetch(job[1] + "api/json" + treeQuery("builds[number,url]{0," + limit + "}"));
            Matcher objects = Pattern.compile("\\{[^{}]*\\}").matcher(buildList);
            while (objects.find()) {
                String obj = objects.group();
                Matcher numberMatch = Pattern.compile("number[^:]*:\\s*(\\d+)").matcher(obj);
                Matcher urlMatch = Pattern.compile("url[^:]*:\s*[^a-zA-Z0-9]*(https?[^\" ]+)").matcher(obj);
                if (numberMatch.find() && urlMatch.find()) {
                    builds.add(new String[]{numberMatch.group(1), unescape(urlMatch.group(1))});
                }
            }
        }
        return builds;
    }

    // checkstyle: stop exception type check
    
    /**
     * Runs the extraction.
     * 
     * @param args the command line arguments
     * @throws Exception if any issue occurs
     */
    private void run(String[] args) throws Exception {
        Map<String, String> opts = options(args);
        String base = opts.get("--url");
        String prefix = opts.get("--prefix");
        if (base == null || prefix == null) {
            System.err.println("Usage: java JenkinsMavenTimings --url URL --prefix PREFIX [--builds 1] [--output FILE] "
                + "[--timestamps true|false]");
            System.exit(2);
        }
        if (!base.endsWith("/")) {
            base += "/";
        }
        int limit = Integer.parseInt(opts.getOrDefault("--builds", "1"));
        String timestampOption = opts.getOrDefault("--timestamps", "false");
        if (!timestampOption.equalsIgnoreCase("true") && !timestampOption.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException("--timestamps must be true or false");
        }
        boolean timestamps = Boolean.parseBoolean(timestampOption);
        if (limit < 1) {
            throw new IllegalArgumentException("--builds must be positive");
        }
        String extractedAt = DATE.format(Instant.now());
        String defaultOutput = "maven-timings-" + FILE_DATE.format(LocalDateTime.now()) + ".csv";
        Path output = Path.of(opts.getOrDefault("--output", defaultOutput));
        String jobsJson = fetch(base + "api/json" + treeQuery("jobs[name,url,_class]"));
        // Restrict job discovery to Freestyle projects using the class field.
        Pattern jobsPattern = Pattern.compile("\\{[^{}]*\\}");
        List<String[]> jobs = new ArrayList<>();
        Matcher jm = jobsPattern.matcher(jobsJson);
        while (jm.find()) {
            String object = jm.group();
            if (!object.contains("hudson.model.FreeStyleProject")) {
                continue;
            }
            Matcher name = Pattern.compile("\\\"name\\\":\\\"([^\\\"]+)\\\"").matcher(object);
            Matcher url = Pattern.compile("\\\"url\\\":\\\"([^\\\"]+)\\\"").matcher(object);
            if (name.find() && url.find() && unescape(name.group(1)).startsWith(prefix)) {
                jobs.add(new String[]{unescape(name.group(1)), unescape(url.group(1))});
            }
        }
        jobs.sort((a, b) -> a[0].compareTo(b[0]));
        int count = 0;
        try (PrintWriter csv = new PrintWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {
            csv.println("Job;Build;Date;ExtractedAt;Result;JenkinsSeconds;AntSeconds;MavenSeconds;"
                + "JenkinsAntDifference;AntMavenDifference"
                + (timestamps ? ";LogFirst;LogLast;AntStart;AntEnd;LogBeforeAnt;AntObservedSeconds;"
                    + "LogAfterAnt;LogSpanSeconds" : ""));
            for (String[] job : jobs) {
                List<String[]> builds = processJob(job, limit);
                for (String[] build : builds) {
                    try {
                        String row = toCsv(timestamps, job, build, extractedAt);
                        if (null == row) {
                            continue;
                        }
                        csv.println(row);
                        count++;
                    } catch (IOException e) {
                        System.err.println("Skipping " + job[0] + " #" + build[0] + ": " + e.getMessage());
                    }
                }
            }
        }
        System.err.println("Wrote " + count + " builds from " + jobs.size() + " jobs to " + output);
    }

    /**
     * Executes the program.
     * 
     * @param args the commmand line arguments
     * @throws Exception if the programs fails in execution
     */
    public static void main(String[] args) throws Exception {
        new JenkinsMavenTimings().run(args);
    }
    
    // checkstyle: resume exception type check
    
}
