package test.de.iip_ecosphere.platform.services.environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.Ignore;
import org.junit.Test;

import de.iip_ecosphere.platform.support.PythonUtils;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import org.junit.Assert;

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
     * @param cons optional consumer to analyze received lines, may be <b>null</b> for none
     */
    private static void redirectIO(final InputStream src, final PrintStream dest, Consumer<String> cons) {
        new Thread(new Runnable() {
            public void run() {
                Scanner sc = new Scanner(src);
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    dest.println(line);
                    if (null != cons) {
                        cons.accept(line);
                    }
                }
                sc.close();
            }
        }).start();
    }

    /**
     * Creates and starts a Python process.
     * 
     * @param dir the home dir where to find the script/run it within
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createPythonProcess(File dir, String... args) throws IOException {
        return createPythonProcess(dir, null, null, args);
    }

    /**
     * Creates and starts a Python process.
     * 
     * @param dir the home dir where to find the script/run it within
     * @param stdCons optional consumer on standard out, may be <b>null</b>
     * @param errCons optional consumer on standard error, may be <b>null</b>
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createPythonProcess(File dir, Consumer<String> stdCons, Consumer<String> errCons, 
        String... args) throws IOException {
        String pythonPath = PythonUtils.getPythonExecutable().toString();
        System.out.println("Using Python: " + pythonPath);
        List<String> tmp = new ArrayList<String>();
        tmp.add(pythonPath);
        for (String a : args) {
            tmp.add(a);
        }
        
        System.out.println("Cmd line: " + tmp);
        ProcessBuilder processBuilder = new ProcessBuilder(tmp);        
        processBuilder.directory(dir);
        //processBuilder.inheritIO(); // somehow does not work in Jenkins/Maven surefire testing
        Process python = processBuilder.start();
        redirectIO(python.getInputStream(), System.out, stdCons);
        redirectIO(python.getErrorStream(), System.err, errCons);
        return python;
    }

    /**
     * Creates and starts a Python process with home directory "./src/test/python".
     * 
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createPythonProcess(String... args) throws IOException {
        return createPythonProcess(null, null, args);
    }

    /**
     * Creates and starts a Python process with home directory "./src/test/python".
     * 
     * @param stdCons optional consumer on standard out, may be <b>null</b>
     * @param errCons optional consumer on standard error, may be <b>null</b>
     * @param args the process arguments for the script including python arguments (first), script and script arguments
     * @return the created process
     * @throws IOException if process creation fails
     */
    public static Process createPythonProcess(Consumer<String> stdCons, Consumer<String> errCons, String... args) 
        throws IOException {
        return createPythonProcess(new File("./src/test/python"), stdCons, errCons, args);
    }
    
    /**
     * Tests the Python implementation.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testPythonEnvironment() throws IOException, ExecutionException {
        testPythonEnvironment(AasFactory.DEFAULT_PROTOCOL); // currently "VAB-TCP"
    }
    
    /**
     * Tests the Python implementation.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    public void testPythonEnvironmentHttp() throws IOException, ExecutionException {
        testPythonEnvironment("VAB-HTTP");
    }
    
    /**
     * Tests the Python implementation.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Test
    @Ignore("VAB-HTTPS Server does not yet exist, certificate use on in support.aas.BaSyx unclear")
    public void testPythonEnvironmentHttps() throws IOException, ExecutionException {
        testPythonEnvironment("VAB-HTTPS");
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
        args.add("__init__.py");
        args.add("--port");
        args.add(String.valueOf(vabServer.getPort()));
        if (protocol.length() > 0) {
            args.add("--protocol");
            args.add(protocol);
        }
        String[] tmp = new String[args.size()];
        AtomicBoolean started = new AtomicBoolean(false);
        Consumer<String> bindingConsumer = l -> started.set(l.contains("INFO:root:Bound to"));
        Process python = createPythonProcess(bindingConsumer, bindingConsumer, args.toArray(tmp));
        // add protocol

        int count = 0;
        while (!started.get() && count < 20) { // wait max. for 20*200 ms
            TimeUtils.sleep(200);
            count++;
        }
        Assert.assertTrue("Python server process not started", started.get());
        
        ServerAddress aasServer = new ServerAddress(Schema.HTTP); 
        Endpoint aasServerBase = new Endpoint(aasServer, "");
        Endpoint aasServerRegistry = new Endpoint(aasServer, AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);

        MyService service = new MyService(); // pendent to Python service, used here as expected value(s)
        Aas aas = AasCreator.createAas(vabServer, service, protocol);
        
        Server httpServer = AasFactory.getInstance()
            .createDeploymentRecipe(aasServerBase)
            .addInMemoryRegistry(aasServerRegistry)
            .deploy(aas)
            .createServer()
            .start();
        
        AbstractEnvironmentTest.testAas(aasServerRegistry, service);

        httpServer.stop(true);
        python.destroy();
    }

}
