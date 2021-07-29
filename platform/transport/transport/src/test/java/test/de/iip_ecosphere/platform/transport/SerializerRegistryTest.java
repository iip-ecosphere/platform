package test.de.iip_ecosphere.platform.transport;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

import java.io.IOException;

import org.junit.Assert;

/**
 * Remaining serializer registry tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SerializerRegistryTest {

    /**
     * Tests setting/reading the registry name.
     * 
     * @throws IOException shall not occur  
     */
    @Test
    public void testName() throws IOException {
        final String name = "XXX";
        String old = SerializerRegistry.setName(name);
        Assert.assertEquals(name, SerializerRegistry.getName());
        SerializerRegistry.setName(old);
        SerializerRegistry.resetDefaults();
        
        Serializer<String> ser = SerializerRegistry.getSerializer(String.class);
        Assert.assertNotNull(ser);
        String test = "ABBA";
        byte[] data = ser.to(test);
        String tmp = ser.from(data);
        Assert.assertEquals(test, tmp);
    }
    
}
