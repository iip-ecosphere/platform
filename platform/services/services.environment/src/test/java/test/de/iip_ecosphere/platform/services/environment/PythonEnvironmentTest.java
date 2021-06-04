package test.de.iip_ecosphere.platform.services.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import org.junit.Ignore;
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

    /**
     * Preliminary.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testPythonEnvironment() throws IOException, ExecutionException {
    }
    
    /**
     * Tests the Python implementation.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Ignore("waiting for Python")
    @Test
    public void testPythonEnvironment1() throws IOException, ExecutionException {
        ServerAddress vabServer = new ServerAddress(Schema.HTTP);
        ServerAddress aasServer = new ServerAddress(Schema.HTTP); 
        Endpoint aasServerBase = new Endpoint(aasServer, "");
        Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);

        Aas aas = AasCreator.createAas(vabServer);
              
        ProcessBuilder processBuilder = new ProcessBuilder("python", "src/main/python/__init__.py");
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();
        BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        while ((line = bfr.readLine()) != null) {
            System.out.println(line);
        }
        

/*        URL url = new URL("http://0.0.0.0:5000/api/AiTestAas/AiService/MyService1/RUNNING");
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setRequestMethod("GET");
        
        httpCon.disconnect();*/
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(aasServerBase)
            .addInMemoryRegistry(aasServerRegistry.getEndpoint())
            .deploy(aas)
            .createServer()
            .start();
            
        AbstractEnvironmentTest.testAas(aasServerRegistry);

        httpServer.stop(true);
        p.destroy();
    }

}
