package test.de.iip_ecosphere.platform.services.spring;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactory;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.ServicesAas;
import de.iip_ecosphere.platform.services.environment.AbstractService;
import de.iip_ecosphere.platform.services.environment.ServiceMapper;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.ServiceStub;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.MeterRepresentation;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstants;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.services.spring.ClasspathJavaCommandBuilder;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceManager;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceSetup;
import de.iip_ecosphere.platform.services.spring.SpringInstances;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;
import de.iip_ecosphere.platform.support.net.ManagedServerAddress;
import de.iip_ecosphere.platform.support.net.NetworkManager;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

public class AbstractTestServiceManager {

    /**
     * A predicate testing whether the value of a JSON gauge is positive.
     */
    protected static final Predicate<Object> POSITIVE_GAUGE_VALUE = o -> {
        if (o instanceof Number) {
            return ((Number) o).doubleValue() > 0; 
        } else if (o != null) { // TODO remove with METRICS_AS_VALUES
            Meter meter = MeterRepresentation.parseMeter(o.toString());
            Assert.assertTrue(meter instanceof Gauge); 
            return ((Gauge) meter).value() > 0; 
        } else {
            System.out.println("Warning: Predicate value is null. For Jenkins, assuming all is fine.");
            return true;
        }
    };

    private static Server server;
    private static NotificationMode oldM;
    private static AasSetup oldSetup;
    private static Server implServer;
    private static Server aasServer;
    @Autowired
    private SpringCloudServiceSetup config;
    private List<String> netKeyToRelease = new ArrayList<>();
    private List<Server> serversToRelease = new ArrayList<>();

    /**
     * Returns the configuration instance.
     * 
     * @return the configuration instance
     */
    protected SpringCloudServiceSetup getConfig() {
        return config;
    }
    
    /**
     * Initializes the test by starting an embedded AMQP server. Requires the Qpid configuration file in src/test.
     * We do not rely on MQTT here, because Moquette is not stable enough and Hivemq requires JDK 11.
     * 
     * @param broker the broker address
     */
    public static void init(ServerAddress broker) {
        server = new TestQpidServer(broker); // prescribes protocol for artifacts
        // spring is too late, AbstractSetup does not load it even with modified constructor...
        TransportSetup setup = ServiceFactory.getTransport();
        setup.setPort(broker.getPort());
        setup.setHost("localhost");
        setup.setAuthenticationKey("amqp"); // -> identityStore.yml
        server.start();
        System.out.println("AMQP broker on port " + broker.getPort());
        
        oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(ServicesAas.class));
        oldSetup = AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(c -> c instanceof ServicesAas);
        
        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        System.out.println("AAS Imp server on port " + AasPartRegistry.getSetup().getImplementation().getPort());
        aasServer = AasPartRegistry.deploy(res.getAas()); 
        aasServer.start();
        
        int aasRegPort = AasPartRegistry.getSetup().getRegistry().getPort();
        int aasPort = AasPartRegistry.getSetup().getServer().getPort();
        System.out.println("AAS registry on port " + aasRegPort);
        System.out.println("AAS server on port " + aasPort);

        // additional arguments that AAS can be reached from service
        SpringInstances.setServiceCmdArgs(CollectionUtils.toList(
            CmdLine.composeArgument(Starter.PARAM_IIP_TEST_AAS_PORT, aasPort),
            CmdLine.composeArgument(Starter.PARAM_IIP_TEST_AASREG_PORT, aasRegPort)));
    }

    /**
     * Shuts down client and test server.
     */
    public static void shutdown() {
        MetricsAasConstructor.clear();
        if (null != server) {
            server.stop(false);
            aasServer.stop(true);
            implServer.stop(true);
        }
        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldM);
    }

    /**
     * Asserts that {@code file} exists.
     * 
     * @param file the file
     */
    protected static final void assertFileExists(File file) {
        Assert.assertTrue("File " + file + " does not exist", file.exists());
    }
    
    /**
     * Artifact asserter interface.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected class ArtifactAsserter {

        /**
         * Performs specific tests for the given descriptor.
         * 
         * @param desc the descriptor
         */
        public void testDescriptor(ArtifactDescriptor desc) {
        }

        /**
         * Specific tests for the given deployment, i.e., the running services.
         * 
         * @param desc the descriptor
         */
        public void testDeployment(ArtifactDescriptor desc) {
        }

        /**
         * Called after service stop to clean up resources.
         * 
         * @param desc the descriptor
         */
        public void cleanup(ArtifactDescriptor desc) {
        }

    }
    
    /**
     * Implements the tests for a start-stop scenario with different descriptors/related asserters.
     * 
     * @param descriptorName the descriptor name
     * @param asserter the asserter related to descriptor-specific properties
     * @param fakeServer fake command servers for services - clashes with services that are based on the 
     *     service environment
     * @throws ExecutionException if executing service operations fails
     * @throws IOException if accessing metrics fails
     */
    protected void doTestStartStop(String descriptorName, ArtifactAsserter asserter, boolean fakeServer) 
        throws ExecutionException, IOException {
        config.setDescriptorName(descriptorName);
        ServiceManager mgr = ServiceFactory.getServiceManager();
        Assert.assertTrue(mgr instanceof SpringCloudServiceManager);
        ((SpringCloudServiceManager) mgr).clear(); // start test in fair conditions
        
        File f = new File("./target/jars/simpleStream.spring.jar");
        Assert.assertTrue("Test cannot be executed as " + f 
            + " does not exist. Was it downloaded by Maven?", f.exists());
        String aId = mgr.addArtifact(f.toURI());
        Assert.assertNotNull(aId);
        Assert.assertTrue(aId.length() > 0);
        ArtifactDescriptor aDesc = mgr.getArtifact(aId);
        Assert.assertNotNull(aDesc);
        Assert.assertTrue(mgr.getArtifactIds().contains(aId));
        Assert.assertTrue(mgr.getArtifacts().contains(aDesc));

        // commonalities for both descriptors
        Assert.assertTrue(aDesc.getServiceIds().size() == 2);
        Assert.assertTrue(aDesc.getServiceIds().contains("simpleStream-create"));
        Assert.assertTrue(aDesc.getServiceIds().contains("simpleStream-log"));
        Assert.assertTrue(aDesc.getServices().size() == 2);
        ServiceDescriptor inputService = aDesc.getService("simpleStream-create");
        ServiceDescriptor outputService = aDesc.getService("simpleStream-log");
        Assert.assertTrue(aDesc.getServices().contains(inputService));
        Assert.assertTrue(aDesc.getServices().contains(outputService));
        
        asserter.testDescriptor(aDesc);
        for (ServiceDescriptor sDesc : aDesc.getServices()) {
            Assert.assertNotNull(sDesc.getId());
            Assert.assertTrue(sDesc.getId().length() > 0);
            Assert.assertNotNull(sDesc.getVersion());
            Assert.assertNotNull(sDesc.getName());
            Assert.assertTrue(sDesc.getName().length() > 0);
            Assert.assertEquals(ServiceState.AVAILABLE, sDesc.getState());
        }

        String[] ids = new String[aDesc.getServices().size()];
        aDesc.getServiceIds().toArray(ids);
        if (fakeServer) {
            startFakeServiceCommandServers(mgr, ids);
        }

        System.out.println("STARTING " + mgr + " " + java.util.Arrays.toString(ids)); // needed on Jenkins...
        mgr.startService(ids);

        for (ServiceDescriptor sDesc : aDesc.getServices()) {
            Assert.assertEquals("Service " + sDesc.getId() + " " + sDesc.getName() + " not running: "
                + sDesc.getState(), ServiceState.RUNNING, sDesc.getState());
        }
        
        Aas aas = AasPartRegistry.retrieveIipAas();
        Submodel sub = aas.getSubmodel(ServicesAas.NAME_SUBMODEL);
        Assert.assertNotNull(sub);
        sub.accept(new AasPrintVisitor());

        TimeUtils.sleep(5000);
        
        Map<String, Predicate<Object>> expectedMetrics = new HashMap<>();
        expectedMetrics.put(MetricsAasConstants.SYSTEM_MEMORY_TOTAL, POSITIVE_GAUGE_VALUE);
        expectedMetrics.put(MetricsAasConstants.SYSTEM_MEMORY_USAGE, POSITIVE_GAUGE_VALUE);
        assertMetrics(ids, expectedMetrics);

        asserter.testDeployment(aDesc);
        mgr.stopService(ids);
        releaseFakeServiceCommandServers();
        
        for (ServiceDescriptor sDesc : aDesc.getServices()) {
            Assert.assertEquals("Service " + sDesc.getId() + " " + sDesc.getName() + " not stopped: " 
                + sDesc.getState(), ServiceState.STOPPED, sDesc.getState());
        }
        
        asserter.cleanup(aDesc);
        mgr.removeArtifact(aId);
        Assert.assertFalse(mgr.getArtifactIds().contains(aId));
        Assert.assertFalse(mgr.getArtifacts().contains(aDesc));
        Assert.assertNull(mgr.getArtifact(aId));
        assertReceiverLog();
        MetricsAasConstructor.clear();
    }
    
    /**
     * Asserts the receiver log.
     */
    protected static void assertReceiverLog() {
        File f = new File(FileUtils.getTempDirectoryPath() + "/test.simpleStream.spring.log");
        Assert.assertTrue("Receiver log does not exist", f.exists());
        Assert.assertTrue("Receiver log is empty", f.length() > 0);
    }
    
    /**
     * Asserts the existence of selected AAS metrics and/or their values.
     * 
     * @param ids service ids
     * @param expected the expected metrics as key-predicate pairs, whereby the predicate may be <b>null</b> to 
     *     indicated that the value shall not be tested 
     * @throws IOException if the AAS cannot be retrieved
     * @throws ExecutionException if a property cannot be queried
     */
    protected void assertMetrics(String[] ids, Map<String, Predicate<Object>> expected) 
        throws IOException, ExecutionException {
        Aas aas = AasPartRegistry.retrieveIipAas();
        Submodel sub = aas.getSubmodel(ServicesAas.NAME_SUBMODEL);
        Assert.assertNotNull(sub);
        sub.accept(new AasPrintVisitor());
        SubmodelElementCollection services = sub.getSubmodelElementCollection(ServicesAas.NAME_COLL_SERVICES);
        Assert.assertNotNull(sub);
        for (String id: ids) {
            SubmodelElementCollection service = services.getSubmodelElementCollection(AasUtils.fixId(id));
            Assert.assertNotNull(service);
            for (Map.Entry<String, Predicate<Object>> ent : expected.entrySet()) {
                Property prop = service .getProperty(ent.getKey());
                Assert.assertNotNull(ent.getKey() + " missing", prop);
                Predicate<Object> pred = ent.getValue();
                if (null != pred) {
                    Object val = prop.getValue();
                    Assert.assertTrue("Test for " + ent.getKey() + " failed", pred.test(val));
                }
            }
        }
    }
    
    /**
     * A fake service implementation for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected static class ServiceImpl extends AbstractService {

        /**
         * Creates a service implementation based on a service descriptor.
         * 
         * @param desc the descriptor
         */
        protected ServiceImpl(ServiceDescriptor desc) {
            super(desc.getId(), desc.getName(), desc.getVersion(), desc.getDescription(), 
                desc.isDeployable(), desc.isTopLevel(), desc.getKind());
        }

        @Override
        public void migrate(String resourceId) throws ExecutionException {
        }

        @Override
        public void update(URI location) throws ExecutionException {
        }

        @Override
        public void switchTo(String targetId) throws ExecutionException {
        }

        @Override
        public void reconfigure(Map<String, String> values) throws ExecutionException {
        }
        
    }
    
    /**
     * Starts fake service command servers to test the integration of {@link ServiceStub}.
     * 
     * @param mgr the service manager
     * @param ids the ids of the services to start
     */
    protected void startFakeServiceCommandServers(ServiceManager mgr, String[] ids) {
        NetworkManager nMgr = NetworkManagerFactory.getInstance();
        for (String id : ids) {
            String key = Starter.getServiceCommandNetworkMgrKey(id);
            ManagedServerAddress addr = nMgr.obtainPort(key);
            if (addr.isNew()) {
                netKeyToRelease.add(key);
            }
            ServiceDescriptor desc = mgr.getService(id);
            ProtocolServerBuilder sBuilder = AasFactory.getInstance().createProtocolServerBuilder(
                config.getServiceProtocol(), addr.getPort());
            ServiceMapper mapper = new ServiceMapper(sBuilder);
            mapper.mapService(new ServiceImpl(desc));
            Server server = sBuilder.build();
            server.start();
            serversToRelease.add(server);
        }
    }
    
    /**
     * Release the servers created in {@link #startFakeServiceCommandServers(ServiceManager, String[])}.
     */
    protected void releaseFakeServiceCommandServers() {
        for (Server s : serversToRelease) {
            s.stop(true);
        }
        NetworkManager nMgr = NetworkManagerFactory.getInstance();
        for (String key : netKeyToRelease) {
            nMgr.releasePort(key);
        }
    }
    
    /**
     * Tests service start/stop with the ZIP artifact containing explicit dependency JARs.
     * 
     * @param useClasspath consider classpath file 
     * @throws ExecutionException shall not occur
     */
    protected void testWithZipArchive(boolean useClasspath) throws ExecutionException {
        String prop = System.getProperty(ClasspathJavaCommandBuilder.PROP_ZIP_CLASSPATH, "");
        System.setProperty(ClasspathJavaCommandBuilder.PROP_ZIP_CLASSPATH, String.valueOf(useClasspath));
        ServiceManager mgr = ServiceFactory.getServiceManager();
        File file = new File("target/jars/simpleStream.spring.zip");
        String aid = mgr.addArtifact(file.toURI());
        mgr.startService("simpleStream-create", "simpleStream-log");
        TimeUtils.sleep(5000);
        mgr.stopService("simpleStream-create", "simpleStream-log");
        mgr.removeArtifact(aid);
        System.setProperty(ClasspathJavaCommandBuilder.PROP_ZIP_CLASSPATH, prop);
    }

}
