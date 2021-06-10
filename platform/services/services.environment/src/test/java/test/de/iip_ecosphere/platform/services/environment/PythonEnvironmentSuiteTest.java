package test.de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import org.junit.Test;
import org.junit.Assert;

/**
 * Integration test for the Python environment.
 * 
 * @author Sakshi Singh, SSE
 */
public class PythonEnvironmentSuiteTest extends AbstractEnvironmentTest {

    /**
     * Runs the the Python test suite.
     * 
     * @throws IOException shall not occur
     * @throws InterruptedException shall not occur
     */
    @Test
    public void testPythonTestSuite() throws IOException, InterruptedException {
        Process python = PythonEnvironmentTest.createPythonProcess("TestSuite.py");
        int exit = python.waitFor();
        Assert.assertEquals(0, exit);
    }

}
