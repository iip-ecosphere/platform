package test.de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup.Service;

public class NameplateSetupTest {

    /**
     * Tests {@link NameplateSetup}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testNameplateSetup() throws IOException {
        NameplateSetup init = NameplateSetup.obtainNameplateSetup();
        Aas aas = init.createAas("urn:::AAS:::a1234#", "a1234");
        NameplateSetup setup = NameplateSetup.readFromAas(aas);
        Assert.assertNotNull(setup);
        // TODO further asserts
        Assert.assertNotNull(setup.getServices());
        Map<String, Service> services = NameplateSetup.getServicesAsMap(setup.getServices());
        Assert.assertEquals(2, services.size());
        Service s = services.get("opcua");
        Assert.assertNotNull(s);
        Assert.assertEquals("opcua", s.getKey());
        Assert.assertEquals(4840, s.getPort());
        s = services.get("mqtt");
        Assert.assertNotNull(s);
        Assert.assertEquals("mqtt", s.getKey());
        Assert.assertEquals(1883, s.getPort());
    }

}
