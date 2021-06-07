package test.de.iip_ecosphere.platform.services.environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
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
     * Redirects an input stream to another stream (in parallel).
     * 
     * @param src the source stream
     * @param dest the destination stream
     */
    private static void redirectIO(final InputStream src, final PrintStream dest) {
        new Thread(new Runnable() {
            public void run() {
                Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    dest.println(sc.nextLine());
                }
                sc.close();
            }
        }).start();
    }
    
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
        //processBuilder.inheritIO(); // somehow does not work in Jenkins/Maven surefire testing
        python = processBuilder.start();
        redirectIO(python.getInputStream(), System.out);
        redirectIO(python.getErrorStream(), System.err);
        
        TimeUtils.sleep(1000); // works without on windows, but not on Jenkins/Linux
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

        MyService service = new MyService(); // pendent to Python service, used here as expected value(s)
        Aas aas = AasCreator.createAas(vabServer);
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(aasServerBase)
            .addInMemoryRegistry(aasServerRegistry.getEndpoint())
            .deploy(aas)
            .createServer()
            .start();
            
        AbstractEnvironmentTest.testAas(aasServerRegistry, service);

        httpServer.stop(true);
    }

}
