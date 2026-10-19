package test.de.iip_ecosphere.platform.configuration.easyProducer.opcua;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.iip_ecosphere.platform.configuration.easyProducer.opcua.parser.DomParser;

/**
 * Measures {@code DomParser.process} for one explicitly selected NodeSet in one JVM: first
 * {@code benchmark.warmup} warm-up iterations, then {@code benchmark.iterations} measured iterations.
 * Warm-up rows get round numbers 0, -1, ...; measured rows get 1..N. Every iteration is one CSV row.
 * Each (version, NodeSet) pair must be started in a fresh JVM by {@code run-benchmark.sh}.
 */
@RunWith(Parameterized.class)
public class DomParserBenchmarkIT {

    private static final File NODESET_DIR = new File("src/test/resources/NodeSets");
    private static final File OUT_DIR = new File("target/benchmark");
    private static final File COLLECTOR_FILE = new File("target/tmp/CollectedInformation.txt");
    private static final File CSV_FILE = new File(System.getProperty("benchmark.output",
            new File(OUT_DIR, "benchmark_results.csv").getPath()));
    private static final String VERSION = System.getProperty("benchmark.version", "unknown");
    private static final int WARMUP = Integer.getInteger("benchmark.warmup", 1);
    private static final int ITERATIONS = Integer.getInteger("benchmark.iterations", 5);

    private final File nodeSetFile;

    public DomParserBenchmarkIT(File nodeSetFile) {
        this.nodeSetFile = nodeSetFile;
    }

    /**
     * Selects exactly one NodeSet so that one Maven invocation produces the series of one NodeSet.
     *
     * @return the selected NodeSet
     */
    @Parameters(name = "{0}")
    public static List<Object[]> nodesets() {
        String selectedNodeSet = System.getProperty("nodeset");
        if (selectedNodeSet == null || selectedNodeSet.trim().isEmpty()) {
            throw new IllegalArgumentException("Set -Dnodeset=<file-name>; one NodeSet is required per JVM");
        }
        List<Object[]> params = new ArrayList<>();
        File[] files = NODESET_DIR.listFiles(file -> file.isFile()
                && file.getName().equals(selectedNodeSet));
        if (files != null) {
            for (File file : files) {
                params.add(new Object[] {file});
            }
        }
        if (params.isEmpty()) {
            throw new IllegalArgumentException("Unknown NodeSet: " + selectedNodeSet);
        }
        return params;
    }

    /**
     * Creates clean output files for this series.
     *
     * @throws IOException if the output files cannot be prepared
     */
    @BeforeClass
    public static void setup() throws IOException {
        Files.createDirectories(OUT_DIR.toPath());
        Files.createDirectories(COLLECTOR_FILE.getParentFile().toPath());
        File csvParent = CSV_FILE.getAbsoluteFile().getParentFile();
        if (csvParent != null) {
            Files.createDirectories(csvParent.toPath());
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, false))) {
            writer.println("NodeSet,Version,Round,FileSizeKB,UAObjectTypeCountIn,"
                    + "TotalMs,IvmlLines,OutputSha256,UnknownDataTypes,"
                    + "IvmlElements_RootObjectType,IvmlElements_FieldVariableType,"
                    + "IvmlElements_EnumType,IvmlElements_ObjectTypeType,"
                    + "InputLines,InputOutputRatio,CheckRequiredModels,CheckRedundancy");
        }
        DomParser.setDefaultVerbose(false);
    }

    /**
     * Prints the result location.
     */
    @AfterClass
    public static void writeResults() {
        System.out.println("\nBenchmark complete. Results: " + CSV_FILE.getAbsolutePath());
    }

    /** Output statistics that do not change between iterations. */
    private static final class Metrics {
        int ivmlLines;
        int unknownTypes;
        int rootObjectType;
        int fieldVariableType;
        int enumType;
        int objectTypeType;
        double ioRatio;
    }

    /**
     * Executes warm-up and measured iterations of the complete generation process.
     *
     * @throws IOException if benchmark input or output cannot be read or written
     */
    @Test
    public void benchmark() throws IOException {
        String name = nodeSetFile.getName().replace(".xml", "").replace(".XML", "");
        File outFile = new File(OUT_DIR, name + ".ivml");
        long fileSizeKB = nodeSetFile.length() / 1024;
        String xmlContent = Files.readString(nodeSetFile.toPath(), StandardCharsets.UTF_8);
        int uaObjectTypeCountIn = countOccurrences(xmlContent, "<UAObjectType ");
        int inputLines = xmlContent.split("\n").length;

        Metrics metrics = new Metrics();
        boolean metricsDone = false;
        int total = WARMUP + ITERATIONS;
        for (int i = 0; i < total; i++) {
            int round = i - WARMUP + 1; // warm-up iterations are 0, -1, ...; measured ones start at 1

            // every iteration starts from the same file state as a fresh JVM would
            Files.deleteIfExists(COLLECTOR_FILE.toPath());
            Files.deleteIfExists(outFile.toPath());

            long totalMs = -1;
            RuntimeException failure = null;
            String checkRequiredModels = "OK";
            String checkRedundancy = "OK";
            try {
                DomParser.setUsingIvmlFolder(OUT_DIR.getPath());
                long start = System.nanoTime();
                DomParser.process(nodeSetFile, name, outFile, false);
                totalMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            } catch (RuntimeException e) {
                failure = e;
                if (e.getMessage() != null && e.getMessage().contains("checkRequiredModels")) {
                    checkRequiredModels = "FAIL: " + sanitize(e.getMessage());
                } else if (e.getMessage() != null && e.getMessage().contains("checkRedundancy")) {
                    checkRedundancy = "FAIL: " + sanitize(e.getMessage());
                } else {
                    checkRequiredModels = "FAIL: " + e.getClass().getSimpleName();
                }
            }

            String outputSha256 = "";
            if (failure == null && outFile.exists()) {
                byte[] ivmlBytes = Files.readAllBytes(outFile.toPath());
                outputSha256 = sha256(ivmlBytes);
                if (!metricsDone) {
                    String ivmlContent = new String(ivmlBytes, StandardCharsets.UTF_8);
                    metrics.ivmlLines = ivmlContent.split("\n").length;
                    metrics.unknownTypes = countOccurrences(ivmlContent, "opcUnknownDataType");
                    metrics.rootObjectType = countOccurrences(ivmlContent, "UARootObjectType");
                    metrics.fieldVariableType = countOccurrences(ivmlContent, "UAFieldVariableType");
                    metrics.enumType = countOccurrences(ivmlContent, "UAEnumType");
                    metrics.objectTypeType = countOccurrences(ivmlContent, "UAObjectTypeType");
                    metrics.ioRatio = inputLines > 0
                            ? Math.round((double) metrics.ivmlLines / inputLines * 100.0) / 100.0
                            : 0;
                    metricsDone = true;
                }
            }

            String[] row = {
                nodeSetFile.getName(),
                VERSION,
                String.valueOf(round),
                String.valueOf(fileSizeKB),
                String.valueOf(uaObjectTypeCountIn),
                String.valueOf(totalMs),
                String.valueOf(metrics.ivmlLines),
                outputSha256,
                String.valueOf(metrics.unknownTypes),
                String.valueOf(metrics.rootObjectType),
                String.valueOf(metrics.fieldVariableType),
                String.valueOf(metrics.enumType),
                String.valueOf(metrics.objectTypeType),
                String.valueOf(inputLines),
                String.valueOf(metrics.ioRatio),
                checkRequiredModels,
                checkRedundancy
            };
            try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, true))) {
                writer.println(String.join(",", row));
            }

            if (failure != null) {
                throw new AssertionError("Benchmark failed for " + nodeSetFile.getName()
                        + " in iteration " + round, failure);
            }
            System.out.printf("%-70s | %-7s round=%2d | total=%6d ms | ivml=%5d lines%n",
                    nodeSetFile.getName(), round <= 0 ? "warm-up" : "measure", round, totalMs,
                    metrics.ivmlLines);
        }
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String sanitize(String text) {
        return text.replace(",", ";").replace("\r", " ").replace("\n", " ");
    }

    private static int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}
