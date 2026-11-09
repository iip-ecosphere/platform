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

package de.oktoflow.platform.cmdTools.modules;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Inspects a (workspace) directory for split classpaths, i.e., classpaths that occur and are populated in multiple 
 * projects.
 * 
 * @author ChatGPT
 * @author Holger Eichelberger, SSE
 */
public class SplitPackageInspector {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile(
        "\\bpackage\\s+([\\p{L}_$][\\p{L}\\p{N}_$]*"
        + "(?:\\.[\\p{L}_$][\\p{L}\\p{N}_$]*)*)\\s*;");

    private static final Set<String> EXCLUDED_DIRS = Set.of(
        "target", "bin", ".git", ".svn", ".settings",
        ".metadata", "node_modules", ".gradle", "build", 
        "testdata",         // EASy-Producer
        "gen", "examples"); // oktoflow, do not look into examples (for now)

    private static final Set<String> DEFAULT_SOURCE_ROOTS = Set.of(
        "src/main/java", "src", "src-gen", "src/gen/java");
    
    private static final Set<String> EXCLUDED_PATHS = Set.of(
        "tests/resources",
        "src/test/resources",
        "src/test/expected",
        "src/test/reference"
    );    

    private record Occurrence(Path project, Path sourceFile) { }

    /**
     * Searches for split classpaths.
     * 
     * @param args the directory to start searching at and optionally further options
     * @throws IOException if file access fails
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java SplitPackageInspector <directory> [--include-tests]");
            System.exit(1);
        }

        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        boolean includeTests = args.length == 2 && "--include-tests".equals(args[1]);

        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Not a directory: " + root);
        }

        Map<String, List<Occurrence>> packages = new TreeMap<>();
        System.out.println("Searching for projects...");        
        List<Path> projects = findProjects(root);
        System.out.printf("Found %d projects.%n", projects.size());
        int current = 0;
        for (Path project : projects) {
            current++;
            System.out.printf("[%d/%d] Scanning %s%n", current, projects.size(), root.relativize(project));            
            Set<Path> javaFiles = new TreeSet<>();
            for (Path sourceRoot : findSourceRoots(project, includeTests)) {
                try (Stream<Path> files = Files.walk(sourceRoot)) {
                    files.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> !isExcludedPath(project, p))
                        .forEach(javaFiles::add);
                }
            }
            System.out.printf("    %d Java files%n", javaFiles.size());
            for (Path file : javaFiles) {
                String packageName = readPackage(file);

                if (packageName != null) {
                    packages.computeIfAbsent(
                        packageName, k -> new ArrayList<>())
                        .add(new Occurrence(project, file));
                }
            }            
        }
        printResults(root, packages);
    }

    /**
     * Recursively finds Maven and Eclipse projects.
     *
     * @param root the directory to search
     * @return the discovered project directories
     * @throws IOException if directory traversal fails
     */
    private static List<Path> findProjects(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                .filter(Files::isDirectory)
                .filter(p -> !isExcluded(root, p))
                .filter(SplitPackageInspector::isProject)
                .map(p -> emit(p))
                .sorted()
                .toList();
        }
    }
    
    /**
     * Emit and return {@code path}.
     * 
     * @param path the path to emit
     * @return the emitted path
     */
    private static Path emit(Path path) {
        System.out.println(" - " + path);
        return path;
    }

    /**
     * Returns whether {@code dir} represents a project.
     * 
     * @param dir the directory
     * @return {@code true} for a project, {@code false} else
     */
    private static boolean isProject(Path dir) {
        return Files.isRegularFile(dir.resolve("pom.xml"))
            || Files.isRegularFile(dir.resolve(".project"))
            || Files.isRegularFile(dir.resolve("build.properties"))
            || Files.isRegularFile(
                dir.resolve("META-INF/MANIFEST.MF"));
    }
    
    /**
     * Determines whether a path belongs to an excluded directory tree.
     *
     * @param project the root directory of the project
     * @param path the path to inspect
     * @return {@code true} if the path is excluded, {@code false} otherwise
     */
    private static boolean isExcludedPath(Path project, Path path) {
        Path relative = project.relativize(path).normalize();
        for (String excluded : EXCLUDED_PATHS) {
            if (relative.startsWith(Path.of(excluded))) {
                return true;
            }
        }
        return false;
    }    

    /**
     * Returns whether the given {@code path} in {@code root} is excluded.
     * 
     * @param root the root folder
     * @param path the path in {@code root}
     * @return {@code true} for excluded, {@code false} else
     */
    private static boolean isExcluded(Path root, Path path) {
        Path relative = root.relativize(path);
        for (Path part : relative) {
            if (EXCLUDED_DIRS.contains(part.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines the source directories of a project.
     *
     * @param project the project directory
     * @param includeTests whether test sources shall be included
     * @return the source root directories
     * @throws IOException if source root discovery fails
     */
    private static Set<Path> findSourceRoots(Path project, boolean includeTests) throws IOException {
        Set<Path> result = new LinkedHashSet<>();
        for (String source : DEFAULT_SOURCE_ROOTS) {
            addSourceRoot(project, source, result);
        }
        if (includeTests) {
            addSourceRoot(project, "src/test/java", result);
            addSourceRoot(project, "test", result);
            addSourceRoot(project, "tests", result);
        }
        // Additional source roots from Eclipse .classpath
        Path classpath = project.resolve(".classpath");
        if (Files.isRegularFile(classpath)) {
            readEclipseClasspath(project, classpath, result,
                includeTests);
        }
        // Additional source roots from Tycho build.properties
        Path buildProperties = project.resolve("build.properties");
        if (Files.isRegularFile(buildProperties)) {
            readBuildProperties(project, buildProperties, result);
        }
        return result;
    }

    /**
     * Adds a source root.
     * 
     * @param project the project path
     * @param relative the relative directory in {@code root}
     * @param result the paths to modify as a side effect
     */
    private static void addSourceRoot(Path project, String relative, Set<Path> result) {
        Path source = project.resolve(relative).normalize();
        if (source.startsWith(project) && Files.isDirectory(source)) {
            result.add(source);
        }
    }

    /**
     * Extracts source directories from an Eclipse classpath file.
     * 
     * @param project the project path/root
     * @param classpath the classpath file
     * @param result the paths to modify as side effect
     * @param includeTests whether tests shall be included
     */
    private static void readEclipseClasspath(Path project, Path classpath,
        Set<Path> result, boolean includeTests) throws IOException {
        String xml = Files.readString(classpath, StandardCharsets.UTF_8);
        Pattern pattern = Pattern.compile("<classpathentry\\b[^>]*\\bkind=\"src\"[^>]*\\bpath=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            String source = matcher.group(1);
            if (!includeTests && isTestPath(source)) {
                continue;
            }
            addSourceRoot(project, source, result);
        }
    }

    /**
     * Extracts source directories from Tycho build.properties.
     * 
     * @param project the project path/root
     * @param properties the properties file
     * @param result the paths to modify as side effect
     */
    private static void readBuildProperties(Path project, Path propertiesFile, Set<Path> result) throws IOException {
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(propertiesFile, StandardCharsets.ISO_8859_1)) {
            properties.load(reader);
        }
        for (String key : properties.stringPropertyNames()) {
            if (key.equals("source..") || key.startsWith("source.")) {
                for (String source : properties.getProperty(key).split(",")) {
                    addSourceRoot(project, source.trim(), result);
                }
            }
        }
    }

    /**
     * Returns whether {@code path} is a test path.
     * 
     * @param path the path to check
     * @return {@code true} for test path, {@code false} else
     */
    private static boolean isTestPath(String path) {
        String normalized = path.replace('\\', '/').toLowerCase();
        boolean result = normalized.equals("test");
        result |= normalized.equals("tests");
        result |= normalized.startsWith("test/");
        result |= normalized.startsWith("tests/");
        result |= normalized.contains("/test/");
        result |= normalized.contains("/tests/");
        return result;
    }

    /**
     * Reads the declared package name from a Java source file.
     *
     * @param file the Java source file
     * @return the package name, or {@code null} if none is found
     */
    private static String readPackage(Path file) {
        String source;
        try {
            source = Files.readString(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            try {
                source = Files.readString(file, Charset.forName("windows-1252"));
            } catch (IOException ex) {
                System.err.println("Cannot read " + file + ": " + ex.getMessage());
                return null;
            }
        } catch (IOException e) {
            System.err.println("Cannot read " + file + ": " + e.getMessage());
            return null;
        }
        source = source.replaceAll("(?s)/\\*.*?\\*/|//[^\\r\\n]*", " ");

        Matcher matcher = PACKAGE_PATTERN.matcher(source);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Prints packages that occur in multiple projects.
     * 
     * @param root the root folder
     * @param packages the packages found and their occurrences (modified as a side effect)
     */
    private static void printResults(Path root, Map<String, List<Occurrence>> packages) {
        int splitPackages = 0;
        for (var entry : packages.entrySet()) {
            Map<Path, List<Path>> byProject = new TreeMap<>();
            for (Occurrence occurrence : entry.getValue()) {
                byProject.computeIfAbsent(occurrence.project(), k -> new ArrayList<>())
                    .add(occurrence.sourceFile());
            }
            if (byProject.size() <= 1) {
                continue;
            }
            splitPackages++;
            System.out.printf("%nPACKAGE: %s (%d projects)%n", entry.getKey(), byProject.size());
            for (var projectEntry : byProject.entrySet()) {
                Path project = projectEntry.getKey();
                System.out.printf("  %-50s %d Java files%n", root.relativize(project), projectEntry.getValue().size());
                for (Path file : projectEntry.getValue()) {
                    System.out.printf("    %s%n", project.relativize(file));
                }
            }
        }
        System.out.printf("%nFound %d split packages.%n", splitPackages);
    }
    
}
