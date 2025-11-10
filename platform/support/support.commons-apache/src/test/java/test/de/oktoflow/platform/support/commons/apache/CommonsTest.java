package test.de.oktoflow.platform.support.commons.apache;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.commons.Commons;
import de.oktoflow.platform.support.commons.apache.ApacheCommons;

/**
 * Tests {@link Commons}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CommonsTest {

    /**
     * Tests basic Commons functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testCommons() throws IOException {
        // just the very basic
        Commons commons = Commons.getInstance();
        Assert.assertTrue(commons instanceof ApacheCommons);
        Commons.setInstance(commons);
    }
    
    // remaining reused, see AllTests
    
}
