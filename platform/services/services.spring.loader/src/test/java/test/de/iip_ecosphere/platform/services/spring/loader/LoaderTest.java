package test.de.iip_ecosphere.platform.services.spring.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.ZipUtils;
import org.junit.Assert;
import org.junit.Test;

public class LoaderTest {
    
    /**
     * Deletes the test file in temp.
     * 
     * @param name the file name 
     * @return the test file object
     * @throws IOException if deleting fails
     */
    private File deleteTestFile(String name) throws IOException {
        File tmpFile  = new File(FileUtils.getTempDirectory(), name + ".test");
        FileUtils.deleteQuietly(tmpFile);
        return tmpFile;
    }
    
    /**
     * Tests the loader with original Zip packaging.
     * 
     * @throws IOException if I/O fails
     */
    @Test
    public void testLoader_originalZipPackaging() throws IOException, ExecutionException, InterruptedException {
        System.out.println("Original Zip Packaging");
        File file = new File("target/test/test-spring-orig.zip");
        testZip(file, "AppStart-orig");
    }

    /**
     * Tests the loader with split Zip packaging (plugin).
     * 
     * @throws IOException if I/O fails
     */
    @Test
    public void testLoader_splitZipPackaging() throws IOException, ExecutionException, InterruptedException {
        System.out.println("Split Zip Packaging");
        File file = new File("target/test/test-spring.zip");
        testZip(file, "AppStart-split");
    }
    
    /**
     * Tests the loader with original Spring packaging.
     * 
     * @throws IOException if I/O fails
     */
    @Test
    public void testLoader_originalSpringPackaging() throws IOException, ExecutionException, InterruptedException {
        System.out.println("Original Spring Packaging");
        File file = new File("target/test/test-spring-orig.jar");
        testSpring(file, "AppStart-spring-orig");
    }
    
    /**
     * Tests the loader with split Spring packaging (plugin).
     * 
     * @throws IOException if I/O fails
     */
    @Test
    public void testLoader_splitSpringPackaging() throws IOException {
        System.out.println("Split Spring Packaging");
        File file = new File("target/test/test-spring.jar");
        testSpring(file, "AppStart-spring-split");
    }
    
    /**
     * Tests ZIP file packaging by extracting and starting using the main classpath.
     * 
     * @throws IOException if I/O fails
     */
    private void testZip(File zip, String testName) throws IOException {
        File testFile = deleteTestFile(testName);
        
        File extracted = new File("target/extracted");
        FileUtils.deleteQuietly(extracted);
        extracted.mkdirs();        
        ZipUtils.extractZip(new FileInputStream(zip), extracted.toPath());
        if (OsUtils.isWindows()) {
            File cl = new File(extracted, "classpath");
            FileUtils.writeStringToFile(cl, FileUtils.readFileToString(cl).replace("/", "\\").replace(":", ";"));
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        cmd.add("-cp");
        cmd.add("@classpath");
        cmd.add("-Dokto.loader.app=" + extracted.getAbsolutePath()); // may exist or not
        cmd.add("de.iip_ecosphere.platform.services.spring.loader.AppStarter");
        cmd.add(testFile.getAbsolutePath());
        Process proc = new ProcessBuilder(cmd)
            .directory(extracted.getAbsoluteFile())
            .inheritIO()
            .start();
        try {
            proc = proc.onExit().get();
        } catch (ExecutionException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("Process not successful", 0, proc.exitValue());
        Assert.assertTrue("No test output file " + testFile, testFile.exists());
        System.out.println();
    }

    /**
     * Tests Spring packaging by starting as JAR.
     * 
     * @throws IOException if I/O fails
     */
    private void testSpring(File jar, String appName) throws IOException {
        File testFile = deleteTestFile(appName);
        List<String> cmd = new ArrayList<>();
        cmd.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        cmd.add("-jar");
        cmd.add(jar.toString());
        cmd.add(testFile.getAbsolutePath());
        Process proc = new ProcessBuilder(cmd)
            .directory(new File("").getAbsoluteFile())
            .inheritIO()
            .start();
        try {
            proc.onExit().get();
        } catch (ExecutionException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("Process not successful", 0, proc.exitValue());
        Assert.assertTrue("No test output file " + testFile, testFile.exists());
        System.out.println();
    }

}
