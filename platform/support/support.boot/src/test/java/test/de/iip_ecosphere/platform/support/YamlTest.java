package test.de.iip_ecosphere.platform.support;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.yaml.Yaml;
import test.de.iip_ecosphere.platform.support.yaml.TestYaml;

/**
 * "Tests" the {@link Yaml} interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlTest {

    /**
     * Tests basic YAML functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testYaml() throws IOException {
        Yaml yaml = Yaml.getInstance();
        Assert.assertTrue(yaml instanceof TestYaml);
        Yaml.setInstance(yaml);
        
        yaml.loadAll(null, Object.class);
        yaml.dump("abc", null);
    }

}
