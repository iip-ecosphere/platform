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
 * Runs one cold DomParser measurement for one explicitly selected NodeSet.
 * Each measurement must be started in a fresh JVM by {@code run-benchmark.sh}.
 */
@RunWith(Parameterized.class)
public class DomParserBenchmarkIT {

    private static final File NODESET_DIR = new File("src/test/resources/NodeSets");
    private static final File OUT_DIR = new File("target/benchmark");
    private static final File COLLECTOR_FILE = new File("target/tmp/CollectedInformation.txt");
    private static final File CSV_FILE = new File(System.getProperty("benchmark.output",
            new File(OUT_DIR, "benchmark_results.csv").getPath()));
    private static final String VERSION = System.getProperty("benchmark.version", "unknown");
    private static final int ROUND = Integer.getInteger("benchmark.round", 0);

    private final File nodeSetFile;

    public DomParserBenchmarkIT(File nodeSetFile) {
        this.nodeSetFile = nodeSetFile;
    }

    /**
     * Selects exactly one NodeSet so that one Maven invocation produces one independent measurement.
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
     * Creates clean output files for this single measurement.
     *
     * @throws IOException if the output files cannot be prepared
     */
    @BeforeClass
    public static void setup() throws IOException {
        Files.createDirectories(OUT_DIR.toPath());
        Files.createDirectories(COLLECTOR_FILE.getParentFile().toPath());
        Files.deleteIfExists(COLLECTOR_FILE.toPath());
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

    /**
     * Executes one complete generation process.
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

        int ivmlLines = 0;
        String outputSha256 = "";
        int unknownTypes = 0;
        int rootObjectType = 0;
        int fieldVariableType = 0;
        int enumType = 0;
        int objectTypeType = 0;
        double ioRatio = 0;
        if (failure == null && outFile.exists()) {
            byte[] ivmlBytes = Files.readAllBytes(outFile.toPath());
            String ivmlContent = new String(ivmlBytes, StandardCharsets.UTF_8);
            ivmlLines = ivmlContent.split("\n").length;
            outputSha256 = sha256(ivmlBytes);
            unknownTypes = countOccurrences(ivmlContent, "opcUnknownDataType");
            rootObjectType = countOccurrences(ivmlContent, "UARootObjectType");
            fieldVariableType = countOccurrences(ivmlContent, "UAFieldVariableType");
            enumType = countOccurrences(ivmlContent, "UAEnumType");
            objectTypeType = countOccurrences(ivmlContent, "UAObjectTypeType");
            ioRatio = inputLines > 0
                    ? Math.round((double) ivmlLines / inputLines * 100.0) / 100.0
                    : 0;
        }

        String[] row = {
            nodeSetFile.getName(),
            VERSION,
            String.valueOf(ROUND),
            String.valueOf(fileSizeKB),
            String.valueOf(uaObjectTypeCountIn),
            String.valueOf(totalMs),
            String.valueOf(ivmlLines),
            outputSha256,
            String.valueOf(unknownTypes),
            String.valueOf(rootObjectType),
            String.valueOf(fieldVariableType),
            String.valueOf(enumType),
            String.valueOf(objectTypeType),
            String.valueOf(inputLines),
            String.valueOf(ioRatio),
            checkRequiredModels,
            checkRedundancy
        };
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, true))) {
            writer.println(String.join(",", row));
        }

        if (failure != null) {
            throw new AssertionError("Benchmark failed for " + nodeSetFile.getName(), failure);
        }
        System.out.printf("%-70s | total=%4d ms | ivml=%5d lines%n",
                nodeSetFile.getName(), totalMs, ivmlLines);
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