package test.de.iip_ecosphere.platform.support;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.IOFunction;
import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.yaml.Yaml;

/**
 * Tests {@link Yaml}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlTest {

    /**
     * Applies {@code func} to a test resource and returns the result.
     * 
     * @param func the function to apply
     * @return the result from function
     * @throws IOException shall not occur
     */
    private static <T> T fromResource(IOFunction<InputStream, T> func) throws IOException {
        InputStream in = ResourceLoader.getResourceAsStream("nameplate.yml");
        T result = func.apply(in);
        in.close();
        return result;
    }

    /**
     * Tests basic YAML functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testYaml() throws IOException {
        Yaml yaml = Yaml.getInstance();
        Yaml.setInstance(yaml);
        
        Assert.assertNotNull(fromResource(in -> yaml.load(in)));
        Assert.assertNotNull(fromResource(in -> yaml.loadMapping(in)));
        Assert.assertNotNull(fromResource(in -> yaml.loadAs(in, Object.class)));
        Assert.assertNotNull(fromResource(in -> yaml.loadTolerantAs(in, Object.class)));
        String s = fromResource(in -> IOUtils.toString(in, Charset.defaultCharset()));
        Assert.assertNotNull(yaml.loadAs(s, Object.class));

        Assert.assertNotNull(yaml.dump(new Object()));
        yaml.dump(new Object(), new CharArrayWriter());
        yaml.dump(new Object(), Object.class, new CharArrayWriter());
    }

}
