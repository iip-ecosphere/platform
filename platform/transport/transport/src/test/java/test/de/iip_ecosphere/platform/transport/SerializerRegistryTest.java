package test.de.iip_ecosphere.platform.transport;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import org.junit.Assert;

/**
 * Remaining serializer registry tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SerializerRegistryTest {

    /**
     * Tests setting/reading the registry name.
     */
    @Test
    public void testName() {
        final String name = "XXX";
        String old = SerializerRegistry.setName(name);
        Assert.assertEquals(name, SerializerRegistry.getName());
        SerializerRegistry.setName(old);
    }
    
}
