package test.de.iip_ecosphere.platform.transport;

import org.junit.Test;

import de.iip_ecosphere.platform.transport.serialization.ByteArraySerializer;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;
import de.iip_ecosphere.platform.transport.serialization.StringSerializer;

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

    /**
     * Tests {@link StringSerializer}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void defaultStringSerializerTest() throws IOException {
        StringSerializer s1 = new StringSerializer();
        final String test = "a1b2c3!";
        Assert.assertEquals(test, s1.from(s1.to(test)));
        Assert.assertEquals(String.class, s1.getType());
        Assert.assertEquals(test, s1.clone(test));
    }

    /**
     * Tests {@link ByteArraySerializer}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void defaultByteArraySerializerTest() throws IOException {
        ByteArraySerializer s1 = new ByteArraySerializer();
        final byte[] test = "a1b2c3!".getBytes();
        Assert.assertArrayEquals(test, s1.from(s1.to(test)));
        Assert.assertEquals(byte[].class, s1.getType());
        Assert.assertArrayEquals(test, s1.clone(test));
    }

}
