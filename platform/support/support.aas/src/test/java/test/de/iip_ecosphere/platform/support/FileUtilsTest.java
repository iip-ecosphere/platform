package test.de.iip_ecosphere.platform.support;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.FileUtils;

/**
 * Tests {@link FileUtils}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FileUtilsTest {

    
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
    public void testCreateTmpFolder() {
        String tmp = FileUtils.getTempDirectoryPath();
        File created = FileUtils.createTmpFolder("support.test");
        Assert.assertTrue(created.toString().startsWith(tmp));
        Assert.assertTrue(created.exists());
        Assert.assertTrue(created.canRead());
        Assert.assertTrue(created.canWrite());
        FileUtils.deleteQuietly(created);
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
        
        FileUtils.listFiles(f, g -> true, g -> {
            fileCount.incrementAndGet();
        });

        Assert.assertEquals(10, fileCount.get());
        fileCount.set(0);

        FileUtils.listFiles(f, g -> !g.getName().equals("services"), g -> {
            fileCount.incrementAndGet();
        });

        Assert.assertEquals(4, fileCount.get());
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
     * Tests the base64 functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testBase64() throws IOException {
        File src = new File("src/test/resources/Logo.jpg");
        File tgt = new File(org.apache.commons.io.FileUtils.getTempDirectory(), "base64.tst");
        tgt.delete();
        String enc = FileUtils.fileToBase64(src);
        FileUtils.base64ToFile(enc, tgt);
        Assert.assertTrue(org.apache.commons.io.FileUtils.contentEquals(src, tgt));
        tgt.delete();
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
    public void testDeleteOnExit() throws IOException {
        File f = File.createTempFile("iip-test", null);
        FileUtils.deleteOnExit(f);
    }

}
