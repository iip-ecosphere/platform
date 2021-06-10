package test.de.iip_ecosphere.platform.services.environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

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
     * Creates and starts a Python process.
     * 
     * @param dir the home dir where to find the script/run it within
     * @param script the Python script to run
     * @param args the command line arguments for the script
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createPythonProcess(File dir, String script, String... args) throws IOException {
        String pythonPath = "python";
        // this is not nice, but at the moment it is rather difficult to pass an option via ANT to Maven to Surefire
        File jenkinsPath = new File("/var/lib/jenkins/python/active/bin/python3");
        if (jenkinsPath.exists()) {
            pythonPath = jenkinsPath.toString();
        }
        System.out.println("Using Python: " + pythonPath);
        List<String> tmp = new ArrayList<String>();
        tmp.add(pythonPath);
        tmp.add(script);
        for (String a : args) {
            tmp.add(a);
        }
        
        ProcessBuilder processBuilder = new ProcessBuilder(tmp);
        processBuilder.directory(dir);
        //processBuilder.inheritIO(); // somehow does not work in Jenkins/Maven surefire testing
        Process python = processBuilder.start();
        redirectIO(python.getInputStream(), System.out);
        redirectIO(python.getErrorStream(), System.err);
        return python;
    }

    /**
     * Creates and starts a Python process with home directory "./src/test/python".
     * 
     * @param script the Python script to run
     * @param args the command line arguments for the script
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createPythonProcess(String script, String... args) throws IOException {
        return createPythonProcess(new File("./src/test/python"), script, args);
    }

    /**
     * Tests the Python implementation.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testPythonEnvironment() throws IOException, ExecutionException {
        testPythonEnvironment(AasFactory.DEFAULT_PROTOCOL);
    }
    
    /**
     * Tests the Python implementation.
     * 
     * @param protocol the AAS implementation protocol (see {@link AasFactory#getProtocols()}
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    private void testPythonEnvironment(String protocol) throws IOException, ExecutionException {
        ServerAddress vabServer = new ServerAddress(Schema.HTTP); // ephemeral
        List<String> args = new ArrayList<String>();
        args.add("--port");
        args.add(String.valueOf(vabServer.getPort()));
        if (protocol.length() > 0) {
            args.add("--protocol");
            args.add(protocol);
        }
        String[] tmp = new String[args.size()];
        Process python = createPythonProcess("__init__.py", args.toArray(tmp));
        // add protocol
        TimeUtils.sleep(1000); // works without on Windows, but not on Jenkins/Linux

        
        ServerAddress aasServer = new ServerAddress(Schema.HTTP); 
        Endpoint aasServerBase = new Endpoint(aasServer, "");
        Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);

        MyService service = new MyService(); // pendent to Python service, used here as expected value(s)
        Aas aas = AasCreator.createAas(vabServer, service, protocol);
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(aasServerBase)
            .addInMemoryRegistry(aasServerRegistry.getEndpoint())
            .deploy(aas)
            .createServer()
            .start();
            
        AbstractEnvironmentTest.testAas(aasServerRegistry, service);

        httpServer.stop(true);
        python.destroy();
    }

}
