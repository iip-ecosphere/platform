
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

package test.de.iip_ecosphere.platform.services.environment.services;

import java.util.concurrent.ExecutionException;

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
import de.iip_ecosphere.platform.support.iip_aas.AbstractAasLifecycleDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup.Address;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
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
        registryServer = rcp.createRegistryServer(regEndpoint, pType);
        registryServer.start();
        Endpoint serverEndpoint = aasSetup.getServerEndpoint();
        System.out.println("Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        aasServer = rcp.createAasServer(aasSetup.getServerEndpoint(), pType, regEndpoint);
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
        transSetup.setUser("user"); // preliminary in here
        transSetup.setPassword("pwd");
        setup.setTransport(transSetup);
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
     * A simple data class.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyData {
        
        private long timestamp;
        private int[] image; // TODO probably byte, requires model update

        /**
         * For serialization.
         */
        public MyData() {
        }
        
        /**
         * Creates an instance.
         * 
         * @param image the "image" data
         */
        public MyData(int[] image) {
            this.timestamp = System.currentTimeMillis();
            this.image = image;
        }
        
        /**
         * Returns the timestamp. (retrieved, called by service)
         * 
         * @return the timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Returns the image data. (retrieved, called by service)
         * 
         * @return the image data
         */
        public int[] getImage() {
            return image;
        }
        
        // setters are usually there, shall also work without here
        
    }
    
    /**
     * Creates a test service instance.
     * 
     * @return the service instance
     */
    public static TraceToAasService createService() {
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
        
        return new TraceToAasService(app, sDesc);
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
        
        System.out.println("Starting messages");

        int[] img = new int[] {128, 128, 64, 12, 0, 8};
        TraceToAasServiceMain.MyData data = new TraceToAasServiceMain.MyData(img);
        Transport.sendTraceRecord(new TraceRecord("source", TraceRecord.ACTION_SENDING, data));
        TimeUtils.sleep(700);
        Transport.sendTraceRecord(new TraceRecord("rtsa", TraceRecord.ACTION_RECEIVING, data));
        TimeUtils.sleep(700); 
        Transport.sendTraceRecord(new TraceRecord("rtsa", TraceRecord.ACTION_SENDING, data));
        TimeUtils.sleep(1500);
        Transport.sendTraceRecord(new TraceRecord("receiver", TraceRecord.ACTION_RECEIVING, data));
        TimeUtils.sleep(700); 
        
        System.out.println("Messages sent. Waiting until CTRL-C.");
        while (true) {
            TimeUtils.sleep(1000);
        }
    }
}
