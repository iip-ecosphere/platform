package test.de.iip_ecosphere.platform.support;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

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
     * Tests creating a temporary folder.
     */
    @Test
    public void testCreateTmpFolder() {
        String tmp = System.getProperty("java.io.tmpdir");
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

}
