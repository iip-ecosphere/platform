package test.de.iip_ecosphere.platform.support;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Tests {@link FileUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FileUtilsTest {
    
    private static int numberTestFiles = 29;
    private static int numberTestNotServiceFiles = 22;

    /**
     * Sets the number of test files to be found.
     * 
     * @param number the number
     */
    protected static void setNumberTestFiles(int number) {
        numberTestFiles = number;
    }

    /**
     * Sets the number of not service files to be found.
     * 
     * @param number the number
     */
    protected static void setNumberTestNotServiceFiles(int number) {
        numberTestNotServiceFiles = number;
    }

    /**
     * Tests the temporary directory access.
     */
    @Test
    public void testTempDir() {
        Assert.assertNotNull(FileUtils.getTempDirectoryPath());
        Assert.assertNotNull(FileUtils.getTempDirectory());
    }

    /**
     * Tests creating a temporary folder.
     */
    @Test
    public void testCreateTmpFolder() throws IOException {
        String tmp = FileUtils.getTempDirectoryPath();
        File created = FileUtils.createTmpFolder("support.test");
        Assert.assertTrue(created.toString().startsWith(tmp));
        Assert.assertTrue(created.exists());
        Assert.assertTrue(created.canRead());
        Assert.assertTrue(created.canWrite());
        FileUtils.deleteQuietly(created);
        
        // remaining delete quietly
        FileUtils.deleteQuietly(null);
        File f = File.createTempFile("support.boot", "tmp");
        FileUtils.deleteQuietly(f);
    }
    
    /**
     * Tests closing quietly.
     */
    @Test 
    public void testCloseQuietly() {
        FileUtils.closeQuietly(null);
        Closeable cl = new Closeable() {

            @Override
            public void close() throws IOException {
                throw new IOException();
            }
            
        };
        FileUtils.closeQuietly(cl);
    }
    
    /**
     * Tests {@link FileUtils#listFiles(File, java.util.function.Predicate, java.util.function.Consumer)}.
     */
    @Test
    public void testListFiles() {
        File f = new File("src/test/resources");
        AtomicInteger fileCount = new AtomicInteger();
        Consumer<File> fileCountUpdater = g -> { fileCount.incrementAndGet(); };
        
        FileUtils.listFiles(f, g -> true, fileCountUpdater);
        
        Assert.assertEquals(numberTestFiles, fileCount.get());
        fileCount.set(0);

        FileUtils.listFiles(f, g -> !g.getName().equals("services"), fileCountUpdater);

        Assert.assertEquals(numberTestNotServiceFiles, fileCount.get());
        
        f = FileUtils.createTmpFolder("support-boot-test");
        fileCount.set(0);
        FileUtils.listFiles(f, g -> true, fileCountUpdater);
        Assert.assertEquals(0, fileCount.get());
        f.delete();
    }
    
    /**
     * Tests file resolution methods.
     */
    @Test
    public void testResolution() {
        Assert.assertNotNull(FileUtils.getResolvedFile(new File(".")));
        Assert.assertNotNull(FileUtils.getResolvedPath(new File("."), ""));
        Assert.assertNotNull(FileUtils.getResolvedPath(new File("."), "test.txt"));
    }

    /**
     * Tests sanitizing file names.
     */
    @Test
    public void testSanitize() {
        Assert.assertEquals("a", FileUtils.sanitizeFileName("a"));
        Assert.assertEquals("a", FileUtils.sanitizeFileName("a", false));
        Assert.assertTrue(FileUtils.sanitizeFileName("a", true).startsWith("a"));
    }

    /**
     * Tests {@link FileUtils#deleteOnExit(File)}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDeleteOnExit() throws IOException {
        File f = File.createTempFile("iip-test", null);
        FileUtils.deleteOnExit(f);
    }
    
    /**
     * Tests {@link FileUtils#getSystemRoot()}.
     */
    @Test
    public void testSystemRoot() {
        File f = FileUtils.getSystemRoot();
        Assert.assertNotNull(f);
        Assert.assertTrue(f.toString().length() > 0);
    }

    /**
     * Tests {@link FileUtils#createTmpFolder(String, boolean)}.
     */
    @Test
    public void testTemp() throws IOException {
        File f = FileUtils.createTmpFolder("support-boot-test", true);
        Assert.assertNotNull(f);
        Assert.assertTrue(f.exists());
        f = FileUtils.createTmpFolder("support-boot-test", true);
        Assert.assertNotNull(f);
        Assert.assertTrue(f.exists());
    }

    /**
     * Tests properties returned by {@link FileUtils}.
     */
    @Test
    public void testProperties() {
        Assert.assertNotNull(FileUtils.getUserDirectory()); // implemented
        Assert.assertNotNull(FileUtils.getUserDirectoryPath());
        Assert.assertTrue(FileUtils.getUserDirectoryPath().length() > 0); // implemented
    }
    
    /**
     * Tests the base64 methods.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testBase64() throws IOException {
        String text = "abc-txt0123";
        File f = File.createTempFile("support-boot-test", "tmp");
        FileUtils.base64ToFile(text, f);
        String text2 = FileUtils.fileToBase64(f);
        Assert.assertEquals(text, text2);
        f.delete();
    }

    /**
     * Tests delegated (but not implemented) plugin operations.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testDelegated() throws IOException {
        FileUtils.forceDelete(null);
        FileUtils.findFile(FileUtils.getTempDirectory(), "notThere");
        FileUtils.getSystemRoot();
        File f = File.createTempFile("support-boot-test", "tmp");
        FileUtils.contentEquals(f, f);
        FileUtils.write(f, "abc-txt");
        FileUtils.write(f, "abc-txt", Charset.defaultCharset());
        FileUtils.writeStringToFile(f, "abc-txt");
        FileUtils.writeStringToFile(f, "abc-txt", Charset.defaultCharset());
        FileUtils.readFileToString(f, Charset.defaultCharset());
        FileUtils.readFileToString(f);
        FileUtils.copyInputStreamToFile(ResourceLoader.getResourceAsStream("identityStore.yml"), f);
        byte[] data = FileUtils.readFileToByteArray(f);
        FileUtils.writeByteArrayToFile(f, data);
        FileUtils.writeByteArrayToFile(f, data, true);
        f.delete();

        f = FileUtils.createTmpFolder("support-boot");
        File f2 = FileUtils.createTmpFolder("support-boot1");
        FileUtils.copyFile(f, f2);
        FileUtils.copyDirectory(f, f2);
        FileUtils.copyDirectory(f, f2, null);
        FileUtils.copyDirectory(f, f2, null, true);
        FileUtils.deleteDirectory(f);
        f.delete(); // just to be sure
    }
    
    /**
     * Tests {@link FileUtils#getFolderSize(File)}.
     */
    @Test
    public void folderSizeTest() {
        Assert.assertEquals(0, FileUtils.getFolderSize(null));
        Assert.assertTrue(FileUtils.getFolderSize(new File(".")) > 0);
    }

}
