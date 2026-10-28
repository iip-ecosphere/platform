package test.de.oktoflow.platform.tools.lib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

import de.oktoflow.platform.tools.lib.HashUtils;
import org.junit.Assert;
import org.junit.Test;

public class HashUtilsTest {

    /**
     * Tests turning an IOException to a message.
     */
    @Test
    public void testToMessage() {
        assertText(HashUtils.toMessage(new IOException("msg")));
    }
    
    /**
     * Asserts that {@code txt} is not <b>null</b> and not empty.
     * 
     * @param txt the text to assert
     */
    private static void assertText(String txt) {
        Assert.assertNotNull(txt);
        Assert.assertTrue(txt.length() > 0);
    }
    
    /**
     * Tests {@link HashUtils#getMD5(File, boolean, Consumer)}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testGetMd5() throws IOException {
        Consumer<String> log = s -> System.out.println(s);
        File idx = new File("src/test/resources/index.idx");
        assertText(HashUtils.getMD5(idx, false, log));
        assertText(HashUtils.getMD5(idx, true, log)); // no change
        File jar = new File("src/test/resources/index.jar");
        assertText(HashUtils.getMD5(jar, false, log)); // no change
        assertText(HashUtils.getMD5(jar, true, log)); // no change
        File zip = new File(System.getProperty(System.getProperty("java.io.tmpdir")), "md5.zip");
        zip.delete();
        Files.copy(new File("src/test/resources/index.jar").toPath(), zip.toPath(), 
            StandardCopyOption.REPLACE_EXISTING);
        assertText(HashUtils.getMD5(zip, true, log)); // no change
        assertText(HashUtils.getMD5(zip, false, log)); // no change
        zip.delete();
    }
    
}
