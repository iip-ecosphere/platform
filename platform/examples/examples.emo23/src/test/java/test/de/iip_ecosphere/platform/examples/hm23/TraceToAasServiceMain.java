
/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.examples.hm23;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.examples.hm23.AppAas;
import de.iip_ecosphere.platform.examples.hm23.Commands;
import de.iip_ecosphere.platform.services.environment.EnvironmentSetup;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.services.environment.services.TraceToAasService;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.AbstractAasLifecycleDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.Version;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceResolver;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup.Address;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAas;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import iip.datatypes.AiResult;
import iip.datatypes.AiResultImpl;
import iip.datatypes.DecisionResult;
import iip.datatypes.DecisionResultImpl;
import iip.datatypes.ImageInput;
import iip.datatypes.ImageInputImpl;
import iip.datatypes.MdzhOutput;
import iip.datatypes.MdzhOutputImpl;
import iip.datatypes.PlcOutput;
import iip.datatypes.PlcOutputImpl;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

/**
 * Simple program to run TraceToAAS in server environment, e.g., for development. Used as basis for TraceToAasTest!
 * 
 * We do not use (info-)logging in here, because Qpid installs a turbo filter that denies all info messages.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TraceToAasServiceMain {
    
    private static Server qpid;
    private static MyLifecycleDescriptor aasDesc;
    private static Server registryServer;
    private static Server aasServer;

    // we go in here for local folders as this is a test rather than a deployable service
    private static final ResourceResolver RESOURCE_DEVICES_RESOLVER = new FolderResourceResolver("./resources/devices");
    
    /**
     * An internal lifecycle descriptor (mocking the platform).
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyLifecycleDescriptor extends AbstractAasLifecycleDescriptor {

        /**
         * Creates a descriptor instance.
         * 
         * @param setup the AAS setup
         */
        protected MyLifecycleDescriptor(AasSetup setup) {
            super("MyAas", () -> setup);
        }
        
    }
    
    /**
     * Initializes the test.
     * 
     * @param host the host name / IP address of the server if the server shall be visible, may be <b>null</b> or 
     *     empty for default ("localhost")
     * @param aasRegistryPort the port of the AAS registry, if {@code -1} use ephemeral ports for registry/server
     * @param aasServerPort the port of the AAS registry, if this or {@code aasRegistryPort} is {@code -1} use  
     *     ephemeral ports for registry/server
     * @param aasProtocolPort port for the AAS protocol server, may be {@code -1} for ephemeral
     */
    public static void startup(String host, int aasRegistryPort, int aasServerPort, int aasProtocolPort) {
        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        qpid = new TestQpidServer(broker);
        qpid.start();
        
        // adjust the setup 
        AasSetup aasSetup;
        if (aasRegistryPort < 0 || aasServerPort < 0) {
            aasSetup = AasSetup.createLocalEphemeralSetup(null, false);
        } else {
            aasSetup = new AasSetup(); // default setup
            if (null != host && host.length() > 0) {
                aasSetup.getServer().setHost(host);
                aasSetup.getRegistry().setHost(host);
                aasSetup.getImplementation().setHost(host);
            }
            aasSetup.getServer().setPort(aasServerPort);
            aasSetup.getRegistry().setPort(aasRegistryPort);
            if (aasProtocolPort < 0) {
                aasProtocolPort = NetUtils.getEphemeralPort();
            }
            aasSetup.getImplementation().setPort(aasProtocolPort);
        }
        String fullRegUri = AasFactory.getInstance().getFullRegistryUri(aasSetup.getRegistryEndpoint());
        System.out.println("Registry: " + fullRegUri);
        
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = aasSetup.getRegistryEndpoint();
        PersistenceType pType = LocalPersistenceType.INMEMORY;
        System.out.println("Starting " + pType + " AAS registry on " + regEndpoint.toUri());
        registryServer = rcp.createRegistryServer(aasSetup, pType);
        registryServer.start();
        Endpoint serverEndpoint = aasSetup.getServerEndpoint();
        System.out.println("Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        aasServer = rcp.createAasServer(aasSetup, pType);
        aasServer.start();
        
        // just like the real platform, but with private local descriptor, no full lifecycle
        aasDesc = new MyLifecycleDescriptor(aasSetup);
        aasDesc.startup(new String[] {});
        
        // mock the setup
        EnvironmentSetup setup = Starter.getSetup();
        setup.setAas(aasSetup);
        TransportSetup transSetup = new TransportSetup();
        transSetup.setHost("localhost");
        transSetup.setPort(broker.getPort());
        //transSetup.setUser("user"); // preliminary in here
        //transSetup.setPassword("pwd");
        transSetup.setAuthenticationKey("amqp");
        setup.setTransport(transSetup);
        Transport.setTransportSetup(() -> transSetup);
    }
    
    /**
     * Shuts down the test.
     */
    public static void shutdown() {
        aasDesc.shutdown();
        registryServer.stop(true);
        aasServer.stop(true);        
        qpid.stop(true);
        
    }
    
    /**
     * Creates a test service instance.
     * 
     * @return the service instance
     */
    public static TraceToAasService createService() {
        PlatformAas.setImageResolver(new FolderResourceResolver("./resources/software"));
        
        ApplicationSetup app = new ApplicationSetup();
        app.setName("myApp");
        app.setId("app-0");
        app.setVersion("1.2.3");
        app.setDescription("");
        app.setManufacturerName("IIP-Ecosphere@de");
        app.setManufacturerLogo("IIP-Ecosphere-Logo.png");
        app.setProductImage("AppImage.png");
        app.setManufacturerProductDesignation("A simple testing app.@de");
        Address addr = new Address();
        addr.setCityTown("Hildesheim@de");
        addr.setDepartment("Software Systems Engineering@de");
        addr.setStreet("Universitätsplatz 1@de");
        addr.setZipCode("30419@de");
        app.setAddress(addr);
        
        YamlService sDesc = new YamlService();
        sDesc.setName("TraceAasTest");
        sDesc.setVersion(new Version(TraceToAasService.VERSION));
        sDesc.setKind(ServiceKind.SINK_SERVICE);
        sDesc.setId("TraceAasTest");
        sDesc.setDeployable(true);
        
        return new MyAppAas(app, sDesc);
    }
    
    /**
     * A mocked version for testing that emits commands without sending them.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyAppAas extends AppAas {
        
        /**
         * Creates a service instance from a service id and a YAML artifact.
         * 
         * @param serviceId the service id
         * @param ymlFile the YML file containing the YAML artifact with the service descriptor
         */
        public MyAppAas(String serviceId, InputStream ymlFile) {
            super(serviceId, ymlFile);
        }
        
        /**
         * Creates a service instance. [for testing]
         *
         * @param app static information about the application
         * @param yaml the service description 
         */
        public MyAppAas(ApplicationSetup app, YamlService yaml) {
            super(app, yaml);
        }

        @Override
        protected Object sendCommand(Commands cmd, String param) {
            // main thread is blocking
            new Thread(() -> { System.out.println("SENDING COMMAND " + cmd); }).run();
            // do not really send here, no super.
            return null;
        }
        
    }
    
    /**
     * A simple main program for tests starting up an AAS, the {@link TraceToAasService} with
     * two log entries and waiting for CTRL-C.
     * 
     * @param args optional command line arguments, --aasServerPort=&lt;int&gt; determines the port of the AAS server, 
     *   --aasRegistyPort=&lt;int&gt; the port of the AAS registry server, --aasProtocolPort=&lt;int&gt; the port of the
     *   AAS implementation/protocol server for implementing functions, --aasHost=&lt;String&gt; as the host name/IP of 
     *   the server
     * @throws ExecutionException if setting service states fails
     */
    public static void main(String[] args) throws ExecutionException {
        System.out.println("Starting AAS server/registry.");
        int aasServerPort = CmdLine.getIntArg(args, "aasServerPort", AasPartRegistry.DEFAULT_PORT); 
        int aasRegistryPort = CmdLine.getIntArg(args, "aasRegistryPort", AasPartRegistry.DEFAULT_REGISTRY_PORT);
        int aasProtocolPort = CmdLine.getIntArg(args, "aasProtocolPort", AasPartRegistry.DEFAULT_PROTOCOL_PORT);
        String aasHost = CmdLine.getArg(args, "aasHost", AasPartRegistry.DEFAULT_HOST);
        startup(aasHost, aasServerPort, aasRegistryPort, aasProtocolPort);
        System.out.println("Creating TraceToAAS service.");
        TraceToAasService service = createService();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            
            System.out.println("Shutting down TraceToAAS service");
            try {
                service.setState(ServiceState.STOPPING);
            } catch (ExecutionException e) {
                System.out.println("While shutting down TraceToAAS service: " + e.getMessage());    
            }
            shutdown();
            System.out.println("Shutdown done.");
            
        }));
        
        System.out.println("Starting TraceToAAS service");
        service.setState(ServiceState.STARTING);
        
        System.out.print("Starting messages: ");

        String actualAiService = "PythonAi";
        sendOpcTraceRecord("PlcNextOpcConn", TraceRecord.ACTION_SENDING);
        sendOpcTraceRecord("ActionDecider", TraceRecord.ACTION_RECEIVING);
        sendAasTraceRecord("MdzhAasConn", TraceRecord.ACTION_SENDING);
        sendAasTraceRecord("ActionDecider", TraceRecord.ACTION_RECEIVING);
        for (int i = 1; i <= 3; i++) {
            sendImageTraceRecord("CamSource", TraceRecord.ACTION_SENDING);
            sendImageTraceRecord("ActionDecider", TraceRecord.ACTION_RECEIVING);
            sendAiTraceRecord(actualAiService, TraceRecord.ACTION_SENDING);
            sendAiTraceRecord("ActionDecider", TraceRecord.ACTION_RECEIVING);
        }
        sendDecisionResultTraceRecord("ActionDecider", TraceRecord.ACTION_SENDING);
        sendDecisionResultTraceRecord("AppAas", TraceRecord.ACTION_RECEIVING);

        sendAiSwitchTraceRecord("myKiFamily", TraceRecord.ACTION_SWITCHING_SERVICE, "myRtsa");
        actualAiService = "myRtsa"; // just for going on
        sendAiSwitchTraceRecord("myKiFamily", TraceRecord.ACTION_SWITCHED_SERVICE, actualAiService);
        
        System.out.println();
        System.out.println("Messages sent. Waiting until CTRL-C.");
        while (true) {
            TimeUtils.sleep(1000);
        }
    }

    /**
     * Send an PLC OPC trace record.
     * 
     * @param source the source service
     * @param action the action 
     */
    private static void sendOpcTraceRecord(String source, String action) {
        PlcOutput data = new PlcOutputImpl();
        data.setPC_ReadyForRequest(true);
        data.setHW_Btn0(false);
        data.setHW_Btn1(true);
        
        Transport.sendTraceRecord(new TraceRecord(source, action, data));
        System.out.print(".");
        TimeUtils.sleep(200);
    }

    /**
     * Send an MDZH AAS trace record.
     * 
     * @param source the source service
     * @param action the action 
     */
    private static void sendAasTraceRecord(String source, String action) {
        MdzhOutput data = new MdzhOutputImpl();
        data.setProductId("12345");
        data.setEngravingText("Bli bla blubb");
        data.setHardwareRevision("Car produced by IFW");
        data.setLength("4.23 m");
        data.setThickness("1.23 m");
        data.setWeight("1.01 kg");
        data.setWindows(4);
        data.setPattern(true);
        data.setTiresColor("dark-blue");
        Transport.sendTraceRecord(new TraceRecord(source, action, data));
        System.out.print(".");
        TimeUtils.sleep(200);
    }

    /**
     * Send an image capture trace record.
     * 
     * @param source the source service
     * @param action the action 
     */
    private static void sendImageTraceRecord(String source, String action) {
        ImageInput data = new ImageInputImpl();
        data.setImageUri("http://me.here.de/phoenix.jpg");
        data.setRobotId(2);
        data.setSide("left");
        AasUtils.resolveImage("PhoenixContact.jpg", RESOURCE_DEVICES_RESOLVER, true, 
            (n, r, m) -> {
                data.setImage(r);
            });

        Transport.sendTraceRecord(new TraceRecord(source, action, data));
        System.out.print(".");
        TimeUtils.sleep(200);
    }

    /**
     * Send an AI trace record.
     * 
     * @param source the source service
     * @param action the action 
     */
    private static void sendAiTraceRecord(String source, String action) {
        AiResult data = new AiResultImpl();
        String[] error = new String[] {"drill", "clatter"};
        double[] errorConf = new double[] {0.78, 0.23};
        data.setError(error);
        data.setErrorConfidence(errorConf);
        data.setImageUri("http://me.here.de/phoenix.jpg");
        data.setRobotId(2);
        Transport.sendTraceRecord(new TraceRecord(source, action, data));
        System.out.print(".");
        TimeUtils.sleep(200);
    }

    /**
     * Send decision trace record.
     * 
     * @param source the source service
     * @param action the action 
     */
    private static void sendDecisionResultTraceRecord(String source, String action) {
        DecisionResult data = new DecisionResultImpl();
        data.setIo(true);
        data.setIoReason(1);
        String[] error = new String[] {"drill"};
        double[] errorConf = new double[] {0.78};
        data.setError(error);
        data.setErrorConfidence(errorConf);
        data.setImageUri(new String[]{"http://me.here.de/phoenix0.jpg", "http://me.here.de/phoenix1.jpg", 
            "http://me.here.de/phoenix2.jpg"});
        data.setRobotId(1);
        Transport.sendTraceRecord(new TraceRecord(source, action, data));
        System.out.print(".");
        TimeUtils.sleep(200);
    }
    
    /**
     * Sends an AI switch trace record.
     * 
     * @param source the source service
     * @param action the action 
     * @param serviceId the respective service id
     */
    private static void sendAiSwitchTraceRecord(String source, String action, String serviceId) {
        Transport.sendTraceRecord(new TraceRecord(source, action, serviceId));
        TimeUtils.sleep(200);
        Transport.sendTraceRecord(new TraceRecord(source, action, serviceId));
    }

}
