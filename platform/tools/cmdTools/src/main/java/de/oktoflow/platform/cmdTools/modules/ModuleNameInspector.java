package de.oktoflow.platform.cmdTools.modules;

import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Inspects Maven projects and their generated JAR files to determine Java module names.
 * Scans the immediate subdirectories of a specified root directory for Maven projects,
 * reads their source manifests, and examines JAR files in their {@code target} directories.
 * Determines effective module names using {@link java.lang.module.ModuleFinder},
 * distinguishing between explicit modules, manifest-defined automatic modules,
 * and automatic modules whose names are derived from JAR filenames.
 * Supports both standard Maven and Maven Tycho projects, including OSGi bundle
 * information provided through the {@code Bundle-SymbolicName} manifest attribute.
 * Outputs the collected information as a tab-separated table to standard output.
 *
 * @author ChatGPT
 * @author Holger Eichelberger
 */
public class ModuleNameInspector {
    
    private static final String JAR_SUFFIX = ".jar";
    private static List<OutputRow> rows = new ArrayList<>();
    private static final String ROW_FORMAT = "%-85s %-55s %-55s %-75s %-55s %-55s %-12s%n";   
    private static final String NONE = "-";
    
    /**
     * Entry point for inspecting Maven projects and their Java module names.
     * Scans the immediate subdirectories of the specified root directory for
     * Maven projects and outputs a tab-separated table containing project names,
     * manifest information, generated JAR files, and their effective module names.
     *
     * @param args command-line arguments; the first argument specifies the root
     *             directory containing the Maven projects
     * @throws IOException if an I/O error occurs while accessing the project
     *                     directories or their contents
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println(
                "Usage: java MavenModuleInspector <projects-directory>");
            System.exit(1);
        }

        Path root = Path.of(args[0]);
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Not a directory: " + root);
        }

        try (Stream<Path> projects = Files.walk(root)) {
            for (Path project : projects
                    .filter(Files::isDirectory)
                    .filter(p -> Files.isRegularFile(p.resolve("pom.xml")))
                    .sorted()
                    .toList()) {
                inspectProject(root, project);
            }
        }   

        System.out.println();
        System.out.printf(ROW_FORMAT, "Project", "Source Automatic-Module-Name", "Bundle-SymbolicName",
            "JAR", "JAR Automatic-Module-Name", "Effective Module Name", "Module Type");
        rows.forEach(r -> r.print());
        System.out.println();
        System.out.println(rows.size() + " module candidates found.");

        // further analyses
        SortedSet<String> duplicated = new TreeSet<>();
        Set<String> known = new HashSet<>();
        rows.forEach(r -> checkDuplicated(r, known, duplicated));
        if (!duplicated.isEmpty()) {
            System.out.println();
            System.out.println("WARN: duplicated effective module names found in " + duplicated);
        }
        List<String> illegalRows = rows.stream()
            .filter(r -> !r.effectiveModule.equals(NONE))
            .filter(r -> ModuleUtils.containsJavaKeywords(r.effectiveModule))
            .map(r -> r.effectiveModule)
            .collect(Collectors.toList());
        if (!illegalRows.isEmpty()) {
            System.out.println("WARN: illegal module names found in " + illegalRows);
        }        
    }
    
    /**
     * Checks whether {@code row} represents a duplicated module.
     * 
     * @param row the output row
     * @param known the known modules during iteration
     * @param duplicated the (sorted) duplicated modules
     */
    private static void checkDuplicated(OutputRow row, Set<String> known, Set<String> duplicated) {
        if (!row.effectiveModule.equals(NONE)) {
            if (!known.contains(row.effectiveModule)) {
                known.add(row.effectiveModule);
            } else {
                duplicated.add(row.effectiveModule);
            }
        }
    }

    /**
     * Inspects a Maven project for module-related information.
     * Reads the project's source manifest, if present, and examines all JAR files
     * in the {@code target} directory to determine their automatic or explicit
     * Java module names. Prints the collected information as table rows.
     *
     * @param root the root folder where this program was called with
     * @param project the path to the Maven project directory
     * @throws IOException if an I/O error occurs while accessing the project
     *                     directory or listing its JAR files
     */
    private static void inspectProject(Path root, Path project) throws IOException {
        // Only inspect Maven projects
        if (!Files.isRegularFile(project.resolve("pom.xml"))) {
            return;
        }

        String projectName = root.relativize(project).toString();
        System.out.println("Inspecting " + projectName);
        Path sourceManifest = project.resolve("META-INF/MANIFEST.MF");
        ManifestInfo source = readManifest(sourceManifest);
        inspect(project.resolve("target"), projectName, source); // pure maven
        inspect(project.resolve("bin"), projectName, source); // Eclipse style
    }

    /**
     * Inspects the given {@code path} for JARs.
     * 
     * @param path the path
     * @param projectName the project name
     * @param source the source manifest
     * @throws IOException if I/O issues occur
     */
    private static void inspect(Path path, String projectName, ManifestInfo source) throws IOException {
        if (!Files.isDirectory(path)) {
            //rows.add(new OutputRow(projectName, source));
            return;
        }
        List<Path> jars;
        try (Stream<Path> files = Files.list(path)) {
            jars = files.filter(Files::isRegularFile)
            .filter(p -> isRelevantJar(p))
            .sorted(Comparator.comparing(p -> p.getFileName().toString()))
            .toList();
        }

        if (jars.isEmpty()) {
            //rows.add(new OutputRow(projectName, source));
            return;
        }

        for (Path jar : jars) {
            inspectJar(projectName, source, jar);
        }
    }
    
    /**
     * Returns whether {@code path} represents a relevant (main) JAR.
     * 
     * @param path the path
     * @return {@code true} for relevant, {@code false} else
     */
    private static boolean isRelevantJar(Path path) {
        boolean result = false;
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(JAR_SUFFIX)) {
            String nameNoSuffix = name.substring(0, name.length() - JAR_SUFFIX.length());
            result = !nameNoSuffix.endsWith("-sources") && !nameNoSuffix.endsWith("-javadoc") 
                && !nameNoSuffix.endsWith("-tests") && !nameNoSuffix.endsWith("-jar-with-dependencies");
        }
        return result;
    }

    /**
     * Represents an output row.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class OutputRow {
        
        private String project;
        private ManifestInfo source;
        private String jar;
        private String jarAutomatic;
        private String effectiveModule;
        private String type;
        
        /**
         * Creates an instance.
         *
         * @param project the name of the Maven project
         * @param source the module-related information from the project's source manifest
         */
        private OutputRow(String project, ManifestInfo source) {
            this(project, source, NONE, NONE, NONE, NONE);
        }
        
        // checkstyle: stop parameter number check

        /**
         * Creates an instance.
         *
         * @param project the name of the Maven project
         * @param source the module-related information from the project's source manifest
         * @param jar the name of the generated JAR file
         * @param jarAutomatic the automatic module name declared in the JAR manifest
         * @param effectiveModule the effective Java module name of the JAR file
         * @param type the module type, indicating whether the module name is explicit,
         *             manifest-defined, filename-derived, or could not be determined
         */
        private OutputRow(String project, ManifestInfo source, String jar, String jarAutomatic, 
            String effectiveModule, String type) {
            this.project = project;
            this.source = source;
            this.jar = jar;
            this.jarAutomatic = jarAutomatic;
            this.effectiveModule = effectiveModule;
            this.type = type;
        }

        // checkstyle: resume parameter number check

        /**
         * Prints a tab-separated table row containing module-related information
         * for a Maven project and one of its generated JAR files.
         * Includes the project's source manifest attributes, the JAR filename,
         * the automatic module name declared in the JAR manifest, the effective
         * Java module name, and the module type.
         */
        private void print() {
            String automaticModule = source.automaticModuleName();
            String bundleSymbolic = source.bundleSymbolicName();
            String jarAuto = jarAutomatic;
            if (!bundleSymbolic.equals(automaticModule)) {
                bundleSymbolic += "*";
            }
            if (!jarAuto.equals(automaticModule)) {
                jarAuto += "*";
            }
            System.out.printf(ROW_FORMAT, project, automaticModule, bundleSymbolic, jar, jarAuto, 
                effectiveModule, type);
        }

    }
    
    // checkstyle: stop exception type check

    /**
     * Inspects a generated JAR file for Java module information.
     * Reads the JAR's manifest and determines its effective module name using
     * {@link java.lang.module.ModuleFinder}. Distinguishes between explicit modules,
     * automatic modules named through the manifest, and automatic modules whose
     * names are derived from the JAR filename. Prints the collected information
     * as a tab-separated table row.
     *
     * @param projectName the name of the Maven project containing the JAR file
     * @param source the module-related information from the project's source manifest
     * @param jar the path to the JAR file to inspect
     */
    private static void inspectJar(String projectName, ManifestInfo source, Path jar) {
        ManifestInfo jarManifest = readManifest(jar, true);

        String moduleName = "-";
        String moduleType = "-";
        try {
            ModuleDescriptor descriptor = ModuleFinder
                .of(jar)
                .findAll()
                .stream()
                .map(ref -> ref.descriptor())
                .findFirst()
                .orElse(null);

            if (descriptor != null) {
                moduleName = descriptor.name();
                if (!descriptor.isAutomatic()) {
                    moduleType = "explicit";
                } else if (!jarManifest.automaticModuleName()
                        .equals("-")) {
                    moduleType = "manifest";
                } else {
                    moduleType = "filename";
                }
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (null != e.getCause()) {
                message += "; " + e.getCause().getMessage();
            }
            moduleType = "ERROR: " + message;
        }

        rows.add(new OutputRow(projectName, source, jar.getFileName().toString(), jarManifest.automaticModuleName(),
            moduleName, moduleType));
    }

    // checkstyle: resume exception type check

    /**
     * Reads module-related information from a manifest file.
     * Extracts the {@code Automatic-Module-Name} and {@code Bundle-SymbolicName}
     * attributes, if present. If the file does not exist or cannot be read,
     * returns a {@link ManifestInfo} instance containing default values.
     *
     * @param path the path to the manifest file
     * @return the extracted manifest information, or default values if the
     *         manifest is missing or cannot be read
     */
    private static ManifestInfo readManifest(Path path) {
        if (!Files.isRegularFile(path)) {
            return new ManifestInfo("-", "-");
        }
        try (var in = Files.newInputStream(path)) {
            return extractManifest(new Manifest(in));
        } catch (IOException e) {
            return new ManifestInfo("-", "-");
        }
    }

    /**
     * Reads module-related information from the manifest embedded in a JAR file.
     * Extracts the {@code Automatic-Module-Name} and {@code Bundle-SymbolicName}
     * attributes, if present. If the JAR file cannot be read or does not contain
     * a manifest, returns a {@link ManifestInfo} instance containing default values.
     *
     * @param jar the path to the JAR file to inspect
     * @param fromJar indicates that the manifest is read from a JAR file;
     *                currently unused
     * @return the extracted manifest information, or default values if the
     *         manifest is missing or cannot be read
     */
    private static ManifestInfo readManifest(Path jar, boolean fromJar) {
        try (JarFile jf = new JarFile(jar.toFile())) {
            return extractManifest(jf.getManifest());
        } catch (IOException e) {
            return new ManifestInfo("-", "-");
        }
    }

    /**
     * Extracts module-related information from a JAR manifest.
     * Retrieves the {@code Automatic-Module-Name} and {@code Bundle-SymbolicName}
     * attributes, if present. Removes any OSGi directives or parameters from
     * the bundle symbolic name. Missing attributes are represented by {@code "-"}.
     * If the manifest is {@code null}, returns a {@link ManifestInfo} instance
     * containing default values.
     *
     * @param manifest the manifest to extract information from, may be {@code null}
     * @return the extracted module-related manifest information, using default
     *         values for missing attributes or a missing manifest
     */
    private static ManifestInfo extractManifest(Manifest manifest) {
        if (manifest == null) {
            return new ManifestInfo("-", "-");
        }

        Attributes attrs = manifest.getMainAttributes();
        String automatic = attrs.getValue("Automatic-Module-Name");
        String symbolic = attrs.getValue("Bundle-SymbolicName");
        // OSGi symbolic names may contain directives
        if (symbolic != null) {
            symbolic = symbolic.split(";", 2)[0].trim();
        }
        return new ManifestInfo(
            automatic == null ? "-" : automatic,
            symbolic == null ? "-" : symbolic);
    }
    
    /**
     * Holds module-related information extracted from a JAR manifest.
     * Contains the automatic Java module name and the OSGi bundle symbolic name.
     * Missing or unavailable attributes are represented by {@code "-"}.
     *
     * @param automaticModuleName the automatic Java module name declared by the
     *                            {@code Automatic-Module-Name} manifest attribute
     * @param bundleSymbolicName the OSGi bundle symbolic name declared by the
     *                           {@code Bundle-SymbolicName} manifest attribute,
     *                           without additional directives or parameters
     */
    private record ManifestInfo(
        String automaticModuleName,
        String bundleSymbolicName) {
    }
    
}

