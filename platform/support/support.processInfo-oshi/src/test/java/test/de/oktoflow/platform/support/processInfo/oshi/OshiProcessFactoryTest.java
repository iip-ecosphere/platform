package test.de.oktoflow.platform.support.processInfo.oshi;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory;
import de.iip_ecosphere.platform.support.processInfo.ProcessInfoFactory.ProcessInfo;
import de.iip_ecosphere.platform.support.rest.Rest;
import de.oktoflow.platform.support.processInfo.oshi.OshiProcessInfoFactory;

/**
 * Tests {@link Rest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OshiProcessFactoryTest {

    /**
     * Tests basic REST functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testRest() throws IOException {
        ProcessInfoFactory pif = ProcessInfoFactory.getInstance();
        Assert.assertTrue(pif instanceof OshiProcessInfoFactory);
        
        // if we would create a process here...
        Assert.assertNotNull(pif.create(null));
        Assert.assertTrue(pif.getProcessId() > 1);
        Assert.assertTrue(pif.getProcessId(null) < 0);

        ProcessInfo pi = pif.create(pif.getProcessId());
        Assert.assertNotNull(pi);
        Assert.assertTrue(pi.getVirtualSize() > 0);
    }

}
