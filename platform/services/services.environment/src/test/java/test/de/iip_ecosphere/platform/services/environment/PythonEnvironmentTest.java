package test.de.iip_ecosphere.platform.services.environment;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

/**
 * Integration test for the Python environment.
 * 
 * @author Sakshi Singh, SSE
 */
public class PythonEnvironmentTest extends AbstractEnvironmentTest {

    private static Process python;
    private static ServerAddress vabServer;
    
    /**
     * Operations before all tests. Startup Python.
     * 
     * @throws IOException shall not occur
     */
    @BeforeClass
    public static void setup() throws IOException {
        vabServer = new ServerAddress(Schema.HTTP); // ephemeral
        String pythonPath = "python";
        // this is not nice, but at the moment it is rather difficult to pass an option via ANT to Maven to Surefire
        File jenkinsPath = new File("/var/lib/jenkins/python/active/python");
        if (jenkinsPath.exists()) {
            pythonPath = jenkinsPath.toString();
        }
        System.out.println("Using Python: " + pythonPath);
        ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, "__init__.py", "--port", 
            String.valueOf(vabServer.getPort()));
        processBuilder.directory(new File("./src/test/python"));
        processBuilder.inheritIO();
        python = processBuilder.start();
    }
    
    /**
     * Operations after all tests. Kill Python.
     */
    @AfterClass
    public static void shutdown() {
        if (null != python) {
            python.destroy();
            python = null;
        }
    }
    
    /**
     * Tests the Python implementation.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testPythonEnvironment() throws IOException, ExecutionException {
        ServerAddress aasServer = new ServerAddress(Schema.HTTP); 
        Endpoint aasServerBase = new Endpoint(aasServer, "");
        Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);

        Aas aas = AasCreator.createAas(vabServer);
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(aasServerBase)
            .addInMemoryRegistry(aasServerRegistry.getEndpoint())
            .deploy(aas)
            .createServer()
            .start();
            
        AbstractEnvironmentTest.testAas(aasServerRegistry);

        httpServer.stop(true);
    }

}
