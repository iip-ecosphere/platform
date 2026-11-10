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
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Builds an inverted index of directly declared Maven dependencies across a workspace.
 * Only POMs discovered beneath the supplied root are eligible as local parents.
 */
public class MavenDependencyInspector {

    private static final Set<String> EXCLUDED_DIRS = Set.of(
        "target", "bin", ".git", ".svn", ".settings", ".metadata",
        "node_modules", ".gradle", "build", 
        "testdata", // EASy-Producer
        "gen", "examples"); // oktoflow
    
    private static final Set<String> EXCLUDED_GROUPIDS = Set.of("de.iip-ecosphere.platform", 
        "net.ssehub.easy", "org.codehaus.mojo", "org.apache.maven.plugins");

    // Add workspace-relative excluded directory trees here.
    private static final Set<String> EXCLUDED_PATHS = Set.of();
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    private record ProjectModel(Path pom, Model model) { }

    private record DependencyKey(String groupId, String artifactId)
            implements Comparable<DependencyKey> {
        @Override
        public int compareTo(DependencyKey other) {
            int c = groupId.compareTo(other.groupId);
            return c != 0 ? c : artifactId.compareTo(other.artifactId);
        }
    }

    private record Usage(String project, String declaration, String profile,
         String version, String scope, String type,
         String classifier, String optional) { }

    /**
     * Runs the inspector.
     * 
     * @param args the directory to inspect
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java MavenDependencyInspector <root-directory> [--short]");
            System.exit(1);
        }
        boolean shortForm = args.length == 2 && "--short".equals(args[1]);

        Path root = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Not a directory: " + root);
        }

        System.out.println("Searching for projects...");        
        List<Path> projects = findProjects(root);
        System.out.printf("Found %d Maven projects.%n", projects.size());
        Map<Path, ProjectModel> models = new HashMap<>();
        int count = 0;
        for (Path project : projects) {
            Path pom = project.resolve("pom.xml").toAbsolutePath().normalize();
            System.out.printf("[%d/%d] Reading %s%n", ++count,
                projects.size(), root.relativize(project));
            try (Reader reader = Files.newBufferedReader(pom, StandardCharsets.UTF_8)) {
                Model model = new MavenXpp3Reader().read(reader);
                models.put(pom, new ProjectModel(pom, model));
            } catch (IOException | XmlPullParserException e) {
                System.err.printf("Cannot read POM %s: %s%n", pom, e.getMessage());
            }
        }

        Map<DependencyKey, Set<Usage>> index = new TreeMap<>();
        for (Path project : projects) {
            ProjectModel model = models.get(project.resolve("pom.xml").toAbsolutePath().normalize());
            if (model != null) {
                inspectProject(root, model, models, index);
            }
        }
        printResults(index, shortForm);
    }

    /** 
     * Finds Maven project directories recursively, skipping excluded paths. 
     * 
     * @param root the root path to search projects within
     */
    private static List<Path> findProjects(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isDirectory)
                .filter(path -> !isExcluded(root, path))
                .filter(path -> Files.isRegularFile(path.resolve("pom.xml")))
                .map(p -> ModuleUtils.emit(p))
                .sorted().toList();
        }
    }

    /**
     * Determines whether a directory is excluded from scanning.
     *
     * @param root the workspace root
     * @param path the directory to check
     * @return {@code true} if the directory is excluded
     */
    private static boolean isExcluded(Path root, Path path) {
        Path relative = root.relativize(path);
        for (Path component : relative) {
            if (EXCLUDED_DIRS.contains(component.toString())) {
                return true;
            }
        }
        for (String excluded : EXCLUDED_PATHS) {
            if (relative.startsWith(Path.of(excluded))) {
                return true;
            }
        }
        return false;
    }

    /** 
     * Reads declarations already parsed from a project model. 
     * 
     * @param root the root path
     * @param project the project model
     * @param models the models found so far
     * @param index the index collected so far
     */
    private static void inspectProject(Path root, ProjectModel project,
        Map<Path, ProjectModel> models, Map<DependencyKey, Set<Usage>> index) {
        Model model = project.model();
        String name = root.relativize(project.pom().getParent()).toString();
        collectDependencies(name, "dependency", "-", model.getDependencies(),
            project, models, index);
        if (model.getDependencyManagement() != null) {
            collectDependencies(name, "dependencyManagement", "-",
                model.getDependencyManagement().getDependencies(), project, models, index);
        }
        for (Profile profile : model.getProfiles()) {
            String profileName = value(profile.getId());
            collectDependencies(name, "dependency", profileName,
                profile.getDependencies(), project, models, index);
            if (profile.getDependencyManagement() != null) {
                collectDependencies(name, "dependencyManagement", profileName,
                    profile.getDependencyManagement().getDependencies(), project, models, index);
            }
        }
    }

    // checkstyle: stop parameter number check

    /** 
     * Collects directly declared dependencies under their group/artifact key. 
     * 
     * @param project the project name
     * @param declaration the declaration type
     * @param profile the profile identifier, or {@code "-"}
     * @param declared the declared dependencies 
     * @param projectModel the project Maven model
     * @param models the models collected so far
     * @param index the dependency index to populate
     */
    private static void collectDependencies(String project, String declaration, String profile, 
        List<Dependency> declared, ProjectModel projectModel, Map<Path, ProjectModel> models,
        Map<DependencyKey, Set<Usage>> index) {
        if (declared == null) {
            return;
        }
        for (Dependency dependency : declared) {
            DependencyKey key = new DependencyKey(
                value(resolveProperties(dependency.getGroupId(), projectModel, models)),
                value(resolveProperties(dependency.getArtifactId(), projectModel, models)));
            Usage usage = new Usage(project, declaration, profile,
                value(unbox(resolveProperties(dependency.getVersion(), projectModel, models))),
                value(resolveProperties(dependency.getScope(), projectModel, models)),
                value(resolveProperties(dependency.getType(), projectModel, models)),
                value(resolveProperties(dependency.getClassifier(), projectModel, models)),
                value(resolveProperties(dependency.getOptional(), projectModel, models)));
            index.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(usage);
        }
    }
    
    /**
     * Removes version specifiers.
     * 
     * @param version the version
     * @return the unboxed version
     */
    private static String unbox(String version) {
        if (null != version && version.length() > 2 && version.startsWith("[") && version.endsWith("]")) {
            version = version.substring(1, version.length() - 1);
        }
        return version;
    }

    // checkstyle: resume parameter number check

    /** 
     * Resolves one level of ${...} references; unknown references remain intact. 
     * 
     * @param value the value containing potential property references
     * @param project the project in which the value is used
     * @param models the available Maven project models
     * @return the value with known property references replaced
     */
    private static String resolveProperties(String value, ProjectModel project,
            Map<Path, ProjectModel> models) {
        if (value == null) {
            return null;
        }
        Matcher matcher = PROPERTY_PATTERN.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String replacement = findProperty(matcher.group(1), project, models,
                new HashSet<>());
            matcher.appendReplacement(result, Matcher.quoteReplacement(
                replacement == null ? matcher.group(0) : replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** 
     * Finds a property in the child first, then in locally discovered parents. 
     * 
     * @param name the property name
     * @param project the project to start searching from
     * @param models the available Maven project models
     * @param visited the POM paths already visited
     * @return the property value, or {@code null} if unavailable
     */
    private static String findProperty(String name, ProjectModel project,
        Map<Path, ProjectModel> models, Set<Path> visited) {
        if (!visited.add(project.pom())) {
            return null;
        }
        Model model = project.model();
        String found = switch (name) {
        case "project.version", "pom.version" ->
            model.getVersion() != null ? model.getVersion()
                : model.getParent() != null ? model.getParent().getVersion() : null;
        case "project.groupId", "pom.groupId" ->
            model.getGroupId() != null ? model.getGroupId()
                : model.getParent() != null ? model.getParent().getGroupId() : null;
        case "project.artifactId", "pom.artifactId" -> model.getArtifactId();
        default -> model.getProperties().getProperty(name);
        };
        if (found != null) {
            return found;
        }
        ProjectModel parent = findParent(project, models);
        return parent == null ? null : findProperty(name, parent, models, visited);
    }

    /** 
     * Finds a parent only among scanned POMs, by relative path or coordinates. 
     * 
     * @param project the project whose parent shall be located
     * @param models the available Maven project models
     * @return the parent project model, or {@code null} if no
     *         matching parent is available
     */
    private static ProjectModel findParent(ProjectModel project, Map<Path, ProjectModel> models) {
        Parent parent = project.model().getParent();
        if (parent == null) {
            return null;
        }

        String relative = parent.getRelativePath();
        if (relative == null) {
            relative = "../pom.xml";
        }
        if (!relative.isEmpty()) {
            Path candidate = project.pom().getParent().resolve(relative)
                .toAbsolutePath().normalize();
            if (Files.isDirectory(candidate)) {
                candidate = candidate.resolve("pom.xml");
            }
            ProjectModel local = models.get(candidate);
            if (local != null && matchesParent(parent, local.model())) {
                return local;
            }
        }

        // Coordinate fallback is deliberately limited to POMs in this scan.
        List<ProjectModel> matches = models.values().stream()
            .filter(candidate -> !candidate.pom().equals(project.pom()))
            .filter(candidate -> matchesParent(parent, candidate.model()))
            .sorted(Comparator.comparing(p -> p.pom().toString()))
            .toList();
        // Ambiguous coordinates are not resolved arbitrarily.
        return matches.size() == 1 ? matches.get(0) : null;
    }

    /** 
     * Checks literal Maven coordinates, with group/version inherited from a parent declaration. 
     * 
     * @param expected the expected parent
     * @param candidate the candidate parent
     */
    private static boolean matchesParent(Parent expected, Model candidate) {
        String group = candidate.getGroupId() != null ? candidate.getGroupId()
            : candidate.getParent() != null ? candidate.getParent().getGroupId() : null;
        String version = candidate.getVersion() != null ? candidate.getVersion()
            : candidate.getParent() != null ? candidate.getParent().getVersion() : null;
        return Objects.equals(expected.getGroupId(), group)
            && Objects.equals(expected.getArtifactId(), candidate.getArtifactId())
            && Objects.equals(expected.getVersion(), version);
    }
    
    /**
     * Returns whether the specified dependency shall not be printed.
     * 
     * @param dependency the dependency
     * @return {@code true} for excluded/no print
     */
    private static final boolean isExcluded(DependencyKey dependency) {
        boolean excluded = EXCLUDED_GROUPIDS.contains(dependency.groupId);
        excluded |= dependency.artifactId.endsWith("-bom");
        excluded |= dependency.artifactId.endsWith("-dependencies");
        excluded |= dependency.artifactId.endsWith("-parent");
        return excluded;
    }

    /**
     * Prints dependencies and their declaring projects.
     *
     * @param index the collected dependency index
     */
    private static void printResults(Map<DependencyKey, Set<Usage>> index, boolean shortForm) {
        int count = 0;
        for (var entry : index.entrySet()) {
            DependencyKey key = entry.getKey();
            if (!isExcluded(key)) {
                List<Usage> usages = entry.getValue().stream()
                    .sorted(Comparator.comparing(Usage::project)
                        .thenComparing(Usage::declaration)
                        .thenComparing(Usage::profile)
                        .thenComparing(Usage::version)
                        .thenComparing(Usage::scope))
                    .toList();
                if (shortForm) {
                    Set<String> versions = new TreeSet<>();
                    for (Usage usage : usages) {
                        versions.add(usage.version);
                    }
                    List<String> versions2 = new ArrayList<>(versions);
                    System.out.printf("%s,%s,%s%n", key.groupId(), key.artifactId(), 
                        versions2.get(versions2.size() - 1));
                } else {
                    System.out.printf("%nDEPENDENCY: %s:%s%n", key.groupId(), key.artifactId());
                    for (Usage usage : usages) {
                        System.out.printf("  %-60s | version=%-25s | type=%-10s | profile=%s%n",
                            usage.project(), usage.version(), usage.scope(), usage.type(),
                            usage.classifier(), usage.optional(), usage.declaration(), usage.profile());
                    }
                }
                count++;
            }
        }
        System.out.printf("%nFound %d distinct dependencies.%n", count);
    }

    /**
     * Replaces missing values with a placeholder.
     *
     * @param value the value to format
     * @return the value or {@code "-"} if missing
     */
    private static String value(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
    
}
