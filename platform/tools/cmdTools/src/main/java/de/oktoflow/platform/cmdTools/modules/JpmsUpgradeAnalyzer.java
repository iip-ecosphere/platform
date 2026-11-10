package de.oktoflow.platform.cmdTools.modules;

import java.io.*;
import java.lang.module.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.jar.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Finds the closest explicitly modular JPMS release on Maven Central (Java 17+). */
public final class JpmsUpgradeAnalyzer {
    private static final String CENTRAL = "https://repo.maven.apache.org/maven2/";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20))
        .followRedirects(HttpClient.Redirect.NORMAL).build();
    private record Inspection(String status, String name, String source, String note) {
    }
    private record Options(Path input, Path output, Path cache, long delayMs, int maxCandidates) {
    }
    private static long lastRequest;
    private static long delayMs;
    private static final Map<String, List<String>> METADATA_CACHE = new HashMap<>();

    // checkstyle: stop exception type check
    
    /**
     * The main program.
     * 
     * @param arg the command line arguments
     * @throws Exception if anything goes wrong
     */
    public static void main(String[] arg) throws Exception {
        Options opt = options(arg);
        if (null == opt) {
            return;
        }
        delayMs = opt.delayMs();
        List<List<String>> input = readCsv(opt.input());
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Empty CSV");
        }
        List<String> header = input.get(0);
        int gi = column(header, "groupId");
        int ai = column(header, "artifactId");
        int vi = column(header, "version");
        // Group by coordinates, preserving all original versions in one output record.
        Map<String, LinkedHashSet<String>> versions = new LinkedHashMap<>();
        for (int i = 1; i < input.size(); i++) {
            List<String> row = input.get(i);
            String g = get(row, gi).trim();
            String a = get(row, ai).trim();
            String v = get(row, vi).trim();
            if (!g.isEmpty() && !a.isEmpty()) {
                versions.computeIfAbsent(g + ":" + a, k -> new LinkedHashSet<>()).add(v);
            }
        }
        List<String> columns = List.of("groupId", "artifactId", "currentVersions", "baselineVersion",
            "recommendedVersion", "jpmsStatus", "moduleName", "moduleNameSource",
            "latestAvailableVersion", "candidatesInspected", "searchStatus", "notes");
        Files.createDirectories(opt.cache());
        try (BufferedWriter out = Files.newBufferedWriter(opt.output(), StandardCharsets.UTF_8)) {
            writeCsv(out, columns);
            int done = 0;
            for (var entry : versions.entrySet()) {
                String[] coords = entry.getKey().split(":", 2);
                String group = coords[0];
                String artifact = coords[1];
                System.out.printf("[%d/%d] %s%n", ++done, versions.size(), entry.getKey());
                List<String> row = analyze(group, artifact, entry.getValue(), opt);
                writeCsv(out, row);
                out.flush(); // Keep partial results if interrupted.
            }
        }
        System.err.println("Wrote " + opt.output());
    }

    // checkstyle: stop method length check

    /**
     * Analyzes the known information.
     * 
     * @param group the group
     * @param artifact the artifact
     * @param current the versions
     * @param opt the options
     * @return the metadata
     */
    private static List<String> analyze(String group, String artifact, Set<String> current, Options opt) {
        String baseline = "";
        String recommended = ""; 
        String status = ""; 
        String name = "";
        String source = "";
        String latest = "";
        String search = "";
        String notes = "";
        int inspected = 0;
        try {
            List<String> all = metadata(group, artifact, opt.cache());
            if (all.isEmpty()) {
                return result(group, artifact, current, "", "", "", "", "", "", 0,
                    "METADATA_UNAVAILABLE", "No Maven Central metadata (possibly private artifact)");
            }
            latest = all.get(all.size() - 1);
            int start = -1;
            for (String v : current) {
                int idx = all.indexOf(v);
                if (idx > start) { 
                    start = idx; 
                    baseline = v; 
                }
            }
            if (start < 0) {
                // Without a matching baseline, we cannot guarantee a minimal upgrade.
                return result(group, artifact, current, "", "", "", "", "", latest, 0,
                    "BASELINE_NOT_FOUND", "None of the listed versions occurs in Maven Central metadata");
            }
            Inspection fallback = null;
            String fallbackVersion = "";
            boolean capped = false;
            for (int i = start; i < all.size(); i++) {
                if (opt.maxCandidates() > 0 && inspected >= opt.maxCandidates()) { 
                    capped = true; 
                    break; 
                }
                String v = all.get(i);
                inspected++;
                Inspection found = inspectVersion(group, artifact, v, opt.cache());
                if (found.status().equals("POM_ONLY")) {
                    status = "POM_ONLY"; search = "POM_ONLY";
                    notes = "Artifact packaging is pom; no runtime JPMS module";
                    break;
                }
                if (found.status().equals("EXPLICIT")) {
                    recommended = v; status = found.status(); name = found.name(); source = found.source();
                    search = "FOUND_EXPLICIT"; notes = found.note();
                    break;
                }
                if (fallback == null && (found.status().equals("AUTOMATIC_MANIFEST") 
                    || found.status().equals("AUTOMATIC_FILENAME"))) {
                    fallback = found; fallbackVersion = v;
                } else if (fallback != null && fallback.status().equals("AUTOMATIC_FILENAME") 
                    && found.status().equals("AUTOMATIC_MANIFEST")) {
                    fallback = found; fallbackVersion = v;
                }
                if (found.status().equals("HTTP_ERROR") || found.status().equals("ERROR")) {
                    notes = "Inspection incomplete at " + v + ": " + found.note();
                    search = "INCOMPLETE";
                    break;
                }
            }
            if (search.isEmpty()) {
                if (fallback != null) {
                    recommended = fallbackVersion; 
                    status = fallback.status(); 
                    name = fallback.name(); 
                    source = fallback.source();
                    search = capped ? "PARTIAL_FALLBACK" : "NO_EXPLICIT_FOUND";
                    notes = capped ? "Candidate limit reached; explicit release may exist later" 
                        : "No explicit descriptor found in searched releases";
                } else {
                    status = "NO_JAR"; search = capped ? "PARTIAL" : "NO_MODULE_FOUND";
                    notes = capped ? "Candidate limit reached" : "No inspectable module JAR found";
                }
            } else if (search.equals("INCOMPLETE") && fallback != null) {
                recommended = fallbackVersion; 
                status = fallback.status(); 
                name = fallback.name(); 
                source = fallback.source();
            }
        } catch (Exception e) {
            search = "ERROR"; notes = e.getClass().getSimpleName() + ": " + e.getMessage();
        }
        return result(group, artifact, current, baseline, recommended, status, name, source, latest, 
            inspected, search, notes);
    }
    
    // checkstyle: resume method length check
    // checkstyle: stop parameter number check

    /**
     * Composes a result as metadata.
     * 
     * @param group the group id
     * @param artifact the artifact id
     * @param current the current versions
     * @param baseline the baseline versions
     * @param recommended the recommended version
     * @param status the JPMS status
     * @param name the module name
     * @param source the module name source
     * @param latest the latest available version
     * @param inspected the number of inspected candidates
     * @param search the search status
     * @param notes analysis notes
     * @return the composed list of metadata
     */
    private static List<String> result(String group, String artifact, Set<String> current, String baseline,
        String recommended, String status, String name, String source, String latest,
        int inspected, String search, String notes) {
        return List.of(group, artifact, String.join("; ", current), baseline, recommended, status, name,
            source, latest, Integer.toString(inspected), search, notes);
    }

    // checkstyle: resume parameter number check

    /**
     * Metadata data from the cache.
     * 
     * @param group the group id
     * @param artifact the artifact id
     * @param cache the cache path
     * @return the metadata
     * @throws Exception if reading fails
     */
    private static List<String> metadata(String group, String artifact, Path cache) throws Exception {
        String key = group + ":" + artifact;
        if (METADATA_CACHE.containsKey(key)) {
            return METADATA_CACHE.get(key);
        }
        String dir = group.replace('.', '/') + "/" + artifact + "/";
        Path xml = cache.resolve(dir + "maven-metadata.xml");
        // Metadata is refreshed on each run, but JARs are cached permanently.
        int code = download(dir + "maven-metadata.xml", xml, true);
        if (code == 404) {
            return List.of();
        }
        Document doc = parseXml(xml);
        NodeList nodes = doc.getElementsByTagName("version");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            values.add(nodes.item(i).getTextContent().trim());
        }
        METADATA_CACHE.put(key, values);
        return values;
    }

    /**
     * Inspects the version.
     * 
     * @param group the group id
     * @param artifact the artifact id
     * @param version the version
     * @param cache the cache path
     * @return the inspection object
     */
    private static Inspection inspectVersion(String group, String artifact, String version, Path cache) {
        String dir = group.replace('.', '/') + "/" + artifact + "/" + version + "/";
        String stem = artifact + "-" + version;
        Path jar = cache.resolve(dir + stem + ".jar");
        Path pom = cache.resolve(dir + stem + ".pom");
        try {
            // Fetch POM first to avoid repeatedly requesting nonexistent JARs for aggregators.
            int pomCode = download(dir + stem + ".pom", pom, false);
            if (pomCode == 200 && "pom".equals(packaging(pom))) {
                return new Inspection("POM_ONLY", "", "POM", "");
            }
            int jarCode = download(dir + stem + ".jar", jar, false);
            if (jarCode == 404) {
                return new Inspection("NO_JAR", "", "", "JAR not published");
            }
            return inspectJar(jar);
        } catch (Exception e) {
            return new Inspection("ERROR", "", "", e.getMessage() == null ? e.toString() : e.getMessage());
        }
    }

    /**
     * Inspects the jar.
     * 
     * @param jar the JAR file
     * @return the inspection results
     * @throws Exception if the JAR cannot be inspected
     */
    private static Inspection inspectJar(Path jar) throws Exception {
        boolean root = false;
        boolean versioned = false;
        boolean multiRelease = false;
        String manifestName = null;
        try (JarFile jf = new JarFile(jar.toFile())) {
            root = jf.getEntry("module-info.class") != null;
            Manifest mf = jf.getManifest();
            if (mf != null) {
                manifestName = mf.getMainAttributes().getValue("Automatic-Module-Name");
                multiRelease = "true".equalsIgnoreCase(mf.getMainAttributes().getValue("Multi-Release"));
            }
            for (Enumeration<JarEntry> en = jf.entries(); en.hasMoreElements();) {
                if (en.nextElement().getName().matches("META-INF/versions/[0-9]+/module-info\\.class")) {
                    versioned = true; break;
                }
            }
        }
        try {
            ModuleDescriptor descriptor = ModuleFinder.of(jar).findAll().stream()
                .map(ModuleReference::descriptor).findFirst().orElse(null);
            if (descriptor == null) {
                return new Inspection("NO_MODULE", "", "", "No descriptor found");
            }
            if (!descriptor.isAutomatic()) {
                return new Inspection("EXPLICIT", descriptor.name(), 
                    root ? "MODULE_INFO" : "MULTI_RELEASE_MODULE_INFO", "");
            }
            if (manifestName != null && !manifestName.isBlank()) {
                return new Inspection("AUTOMATIC_MANIFEST", descriptor.name(), "MANIFEST", "");
            }
            return new Inspection("AUTOMATIC_FILENAME", descriptor.name(), "FILENAME",
                versioned && !multiRelease ? "Versioned descriptor not activated by manifest" : "");
        } catch (FindException e) {
            return new Inspection("INVALID_MODULE", "", "", e.getMessage());
        }
    }

    /**
     * Returns the POM packaging.
     * 
     * @param pom the packaging taken from the XML in {@code pom}.
     * @return the packaging
     */
    private static String packaging(Path pom) {
        try {
            Document doc = parseXml(pom);
            Node root = doc.getDocumentElement();
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node n = children.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE && "packaging".equals(n.getLocalName())) {
                    return n.getTextContent().trim();
                }
            }
        } catch (Exception ignored) {
        }
        return "jar";
    }

    /**
     * Parses the XML in {@code path}.
     * 
     * @param path the path
     * @return the XML document
     * @throws Exception if the document cannot be parsed
     */
    private static Document parseXml(Path path) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return f.newDocumentBuilder().parse(path.toFile());
    }

    /** Serialized HTTP requests with global delay and bounded exponential retry on 429/5xx. */
    private static int download(String remote, Path destination, boolean refresh) throws Exception {
        if (!refresh && Files.isRegularFile(destination)) {
            return 200;
        }
        for (int attempt = 0; attempt < 5; attempt++) {
            long wait = lastRequest + delayMs - System.currentTimeMillis();
            if (wait > 0) {
                Thread.sleep(wait);
            }
            lastRequest = System.currentTimeMillis();
            HttpRequest request = HttpRequest.newBuilder(URI.create(CENTRAL + remote))
                .header("User-Agent", "JpmsUpgradeAnalyzer/1.0 (sequential Maven artifact inspection)")
                .timeout(Duration.ofSeconds(90)).GET().build();
            HttpResponse<Path> response;
            Path tmp = Files.createTempFile("jpms-fetch-", ".tmp");
            try {
                response = HTTP.send(request, HttpResponse.BodyHandlers.ofFile(tmp));
                int code = response.statusCode();
                if (code == 200) {
                    Files.createDirectories(destination.getParent());
                    Files.move(tmp, destination, StandardCopyOption.REPLACE_EXISTING);
                    return 200;
                }
                if (code == 404) {
                    return 404;
                }
                if (isFailImmediately(code)) {
                    throw new IOException("HTTP " + code + " for " + remote);
                }
                System.err.printf("  HTTP %d; retry %d/5%n", code, attempt + 1);
            } finally { 
                Files.deleteIfExists(tmp); 
            }
            Thread.sleep(Math.min(30000L, 2000L << attempt));
        }
        throw new IOException("Maven Central retry limit exceeded for " + remote);
    }

    // checkstyle: resume exception type check

    /**
     * Returns whether the HTTP return {@code code} indicates that we should fail immediately.
     * 
     * @param code the HTTP return code
     * @return {@code true} for immediate retry, {@code false} else
     */
    private static boolean isFailImmediately(int code) {
        boolean retry = code != 429;
        retry &= code != 500;
        retry &= code != 502;
        retry &= code != 503;
        retry &= code != 504;
        return retry;
    }
    
    /**
     * Determines the command line options.
     * 
     * @param args the command line argument
     * @return the options
     */
    private static Options options(String[] args) {
        if (args.length < 2) {
            System.err.println(
                "Usage: java JpmsUpgradeAnalyzer <input.csv> <output.csv> [--cache <dir>] "
                    + "[--delay-ms <ms>] [--max-candidates <n>]");
            return null;
        }
        Path cache = Path.of(System.getProperty("java.io.tmpdir"), ".cache", "jpms-upgrade-analyzer");
        long delay = 750;
        int max = 0;
        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
            case "--cache" -> cache = Path.of(args[++i]);
            case "--delay-ms" -> delay = Long.parseLong(args[++i]);
            case "--max-candidates" -> max = Integer.parseInt(args[++i]);
            default -> throw new IllegalArgumentException("Unknown option " + args[i]);
            }
        }
        if (delay < 0 || max < 0) {
            throw new IllegalArgumentException("Negative option value");
        }
        return new Options(Path.of(args[0]), Path.of(args[1]), cache, delay, max);
    }

    /**
     * Returns a column index for the given headline/title/name.
     * 
     * @param rows the rows
     * @param name the name of the column to search
     * @return the row column index
     * @throws IllegalArgumentException if the name does not lead to an index
     */
    private static int column(List<String> rows, String name) {
        int idx = rows.indexOf(name);
        if (idx < 0) {
            throw new IllegalArgumentException("Missing column " + name);
        }
        return idx;
    }
    /**
     * Gets a CSV cell from a row.
     * 
     * @param row the row
     * @param col the column index
     * @return the value of the cell, may be empty if columns are exceeded
     */
    private static String get(List<String> row, int col) { 
        return col < row.size() ? row.get(col) : ""; 
    }
    
    /**
     * Reads the input CSV.
     * 
     * @param path the file path
     * @return the rows/cells as nested list
     * @throws IOException if the file cannot be accessed
     */
    private static List<List<String>> readCsv(Path path) throws IOException {
        String data = Files.readString(path, StandardCharsets.UTF_8);
        if (data.startsWith("\uFEFF")) {
            data = data.substring(1);
        }
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < data.length() && data.charAt(i + 1) == '"') { 
                    field.append('"'); 
                    i++; 
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) { 
                row.add(field.toString()); 
                field.setLength(0); 
            } else if ((c == '\n' || c == '\r') && !quoted) {
                if (c == '\r' && i + 1 < data.length() && data.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(field.toString()); field.setLength(0); rows.add(row); row = new ArrayList<>();
            } else {
                field.append(c);
            }
        }
        if (quoted) {
            throw new IOException("Unterminated CSV quote");
        }
        if (!row.isEmpty() || field.length() > 0) { 
            row.add(field.toString()); 
            rows.add(row); 
        }
        return rows;
    }

    /**
     * Writes a row to CSV. For excel, open an empty workbook, get data from file.
     * 
     * @param out the output writer
     * @param row the row to write
     * @throws IOException if writing fails
     */
    private static void writeCsv(Writer out, List<String> row) throws IOException {
        for (int i = 0; i < row.size(); i++) {
            if (i > 0) { 
                out.write(',');
            }
            String value = row.get(i);
            if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
                out.write('"' + value.replace("\"", "\"\"") + '"');
            } else {
                out.write(value);
            }
        }
        out.write('\n');
    }

}

