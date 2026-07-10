package test.de.iip_ecosphere.platform.support;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * Tests static map access functions.
     */
    @Test
    public void testMapAccess() {
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> db1 = new HashMap<>();
        db1.put("host", "localhost");
        db1.put("port", 5432);
        map1.put("database", db1);
        map1.put("timeout", 30);
        
        Assert.assertEquals(30, Yaml.getIntValue(map1, "timeout", 0));
        Assert.assertEquals("localhost", Yaml.getStringValue(map1, "database.host", ""));
        Assert.assertEquals(5432, Yaml.getLongValue(map1, "database.port", -1));
        Assert.assertEquals(-1, Yaml.getLongValue(map1, "database.port1", -1));
    }

}
