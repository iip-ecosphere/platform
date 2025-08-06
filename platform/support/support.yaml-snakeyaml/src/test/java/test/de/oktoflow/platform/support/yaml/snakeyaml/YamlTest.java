package test.de.oktoflow.platform.support.yaml.snakeyaml;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.IOUtils;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.yaml.Yaml;
import de.oktoflow.platform.support.yaml.snakeyaml.SnakeYaml;

/**
 * Tests {@link Yaml}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlTest {
    
    /**
     * Like {@link Function} but throws {@link IOException}.
     *
     * @param <P> the type of the input to the operations.
     * @param <R> the return type of the operations.
     * @since 2.7
     */
    @FunctionalInterface
    public interface IOFunction<P, R> {
        
        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         * @throws IOException if an I/O error occurs.
         */
        public R apply(final P param) throws IOException;
        
    }
    
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
        Assert.assertTrue(yaml instanceof SnakeYaml);
        
        Assert.assertNotNull(fromResource(in -> yaml.load(in)));
        Assert.assertNotNull(fromResource(in -> yaml.loadMapping(in)));
        Assert.assertNotNull(fromResource(in -> yaml.loadAs(in, Object.class)));
        Assert.assertNotNull(fromResource(in -> yaml.loadTolerantAs(in, Object.class)));
        String s = fromResource(in -> IOUtils.toString(in));
        Assert.assertNotNull(yaml.loadAs(s, Object.class));

        Assert.assertNotNull(yaml.dump(new Object()));
        yaml.dump(new Object(), new CharArrayWriter());
        yaml.dump(new Object(), Object.class, new CharArrayWriter());
    }

}
