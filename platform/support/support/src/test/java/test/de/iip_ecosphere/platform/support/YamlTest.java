package test.de.iip_ecosphere.platform.support;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.IOUtils;
import de.iip_ecosphere.platform.support.function.IOFunction;
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
        return fromResource("nameplate.yml", func);
    }

    /**
     * Applies {@code func} to a test resource and returns the result.
     * 
     * @param resource the name of the resource
     * @param func the function to apply
     * @return the result from function
     * @throws IOException shall not occur
     */
    private static <T> T fromResource(String resource, IOFunction<InputStream, T> func) throws IOException {
        InputStream in = ResourceLoader.getResourceAsStream(resource);
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
        Iterator<Object> iter = fromResource(in -> yaml.loadAll(in, Object.class));
        Assert.assertTrue(iter.hasNext());
        Assert.assertNotNull(iter.next());
        iter = fromResource("nameplate-path.yml", in -> yaml.loadAll(in, "outer", Object.class));
        Assert.assertTrue(iter.hasNext());
        Assert.assertNotNull(iter.next());
        String s = fromResource(in -> IOUtils.toString(in));
        Assert.assertNotNull(yaml.loadAs(s, Object.class));

        Assert.assertNotNull(yaml.dump(new Object()));
        yaml.dump(new Object(), new CharArrayWriter());
        yaml.dump(new Object(), Object.class, new CharArrayWriter());
    }

}
