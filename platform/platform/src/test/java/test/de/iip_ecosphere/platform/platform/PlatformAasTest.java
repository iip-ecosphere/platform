/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.platform;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.EcsAas;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.platform.ArtifactsManager.ArtifactKind;
import de.iip_ecosphere.platform.platform.PersistentAasSetup;
import de.iip_ecosphere.platform.platform.PlatformAas;
import de.iip_ecosphere.platform.platform.PlatformSetup;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.PersistenceType;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationInstanceAasConstructor;
import de.iip_ecosphere.platform.transport.Transport;
import test.de.iip_ecosphere.platform.transport.TestWithQpid;

/**
 * Tests {@link PlatformAas}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAasTest extends TestWithQpid {

    private static Server qpid;
    
    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() {
        loadPlugins();
        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        qpid = TestWithQpid.fromPlugin(broker);
        EcsFactory.getSetup().getTransport().setPort(broker.getPort());
        qpid.start();
        Transport.setTransportSetup(() -> EcsFactory.getSetup().getTransport());
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        Server.stop(qpid, true);
        Transport.setTransportSetup(null);
    }
    
    /**
     * Tests {@link PlatformAas}.
     * 
     * @throws IOException shall not occur in a successful test
     * @throws ExecutionException shall not occur, if AAS operations fail
     */
    @Test
    public void testPlatformAas() throws IOException, ExecutionException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(EcsAas.class));
        
        AasSetup mySetup = AasSetup.createLocalEphemeralSetup(null, false);
        AasSetup oldSetup = AasPartRegistry.setAasSetup(mySetup);
        PersistentAasSetup oldPSetup = PlatformSetup.getInstance().getAas(); 
        PlatformSetup.getInstance().setAas(new PersistentAasSetup(mySetup));
        // like in an usual platform - platform server goes first
        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = AasPartRegistry.getSetup().getRegistryEndpoint();
        PersistenceType pType = LocalPersistenceType.INMEMORY;
        System.out.println("Starting " + pType + " AAS registry on " + regEndpoint.toUri());
        Server registryServer = rcp.createRegistryServer(AasPartRegistry.getSetup(), pType);
        registryServer.start();
        Endpoint serverEndpoint = AasPartRegistry.getSetup().getServerEndpoint();
        System.out.println("Starting " + pType + " AAS server on " + serverEndpoint.toUri());
        Server aasServer = rcp.createAasServer(AasPartRegistry.getSetup(), pType);
        aasServer.start();
        
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(c -> c instanceof PlatformAas);
        
        // active AAS require two server instances and a deployment
        Server implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        AasPartRegistry.remoteDeploy(res.getAas()); 

        String id1 = PlatformAas.notifyAppNewInstance("app-1", "plan-1");
        if (ApplicationInstanceAasConstructor.firstAppWithoutAppId()) {
            Assert.assertNull(id1); // it's the first one
        } else {
            Assert.assertNotNull(id1); // it's the first one
        }        
        String id2 = PlatformAas.notifyAppNewInstance("app-1", "plan-1");
        Assert.assertNotNull(id2);
        
        int count = PlatformAas.notifyAppInstanceStopped("app-1", id2);
        Assert.assertEquals(1, count); // already one gone
        count = PlatformAas.notifyAppInstanceStopped("app-1", id1);
        Assert.assertEquals(0, count); // both gone
        
        Assert.assertTrue(res.getAas().size() > 0); 
        testUpload(res.getAas().get(0));

        aasServer.stop(true);
        implServer.stop(true);
        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldM);
        MetricsAasConstructor.clear();
        PlatformSetup.getInstance().setAas(oldPSetup);
    }
    
    /**
     * Tests uploading a single-chunk and a multi-chunk file.
     * 
     * @param aas the AAS to use
     * @throws IOException in case that reading a file fails.
     * @throws ExecutionException shall not occur, if AAS operations fail
     */
    private static void testUpload(Aas aas) throws IOException, ExecutionException {
        // ensure setup
        PlatformSetup setup = PlatformSetup.getInstance();
        File origArtifactsFolder = setup.getArtifactsFolder();
        File origUploadFolder = setup.getUploadFolder();
        File artifactsFolder = new File(FileUtils.getTempDirectory(), "okto-artifacts-test");
        File uploadFolder = new File(FileUtils.getTempDirectory(), "okto-uploads-test");
        FileUtils.deleteQuietly(uploadFolder);
        FileUtils.deleteQuietly(artifactsFolder);
        artifactsFolder.mkdirs();
        uploadFolder.mkdirs();
        setup.setArtifactsFolder(artifactsFolder);
        setup.setUploadFolder(null); // use artifacts folder first
        
        // get/ensure AAS operation
        Submodel sm = aas.getSubmodel(PlatformAas.NAME_SUBMODEL_ARTIFACTS);
        Assert.assertNotNull(sm);
        Operation op = sm.getOperation(PlatformAas.NAME_OPERATION_UPLOAD);
        Assert.assertNotNull(op);
        
        // single chunk upload
        String fName = "deployment_plan.yaml";
        File source = new File("src/test/resources", fName);
        byte[] data = FileUtils.readFileToByteArray(source);
        String utfData = Base64.getEncoder().encodeToString(data);
        op.invoke(ArtifactKind.DEPLOYMENT_PLAN.name(), 0, fName, utfData);
        File target = new File(artifactsFolder, fName);
        Assert.assertTrue(target.exists());
        Assert.assertTrue(FileUtils.contentEquals(source, target));
        
        // small chunk upload
        setup.setUploadFolder(uploadFolder);
        fName = "service2.zip";
        source = new File("src/test/resources/artifacts", fName);
        data = FileUtils.readFileToByteArray(source);
        int pos = 0;
        int chunkCount = 1;
        final int chunkLength = 100;
        while (pos < data.length) {
            int actChunkLength = Math.min(chunkLength, data.length - pos);
            byte[] chunk = new byte[actChunkLength];
            int seqNr = actChunkLength < chunkLength ? -chunkCount : chunkCount;
            System.arraycopy(data, pos, chunk, 0, actChunkLength);
            utfData = Base64.getEncoder().encodeToString(chunk);
            op.invoke(ArtifactKind.IMPLEMENTATION_ARTIFACT.name(), seqNr, fName, utfData);
            pos += chunk.length;
            chunkCount++;
        }
        target = new File(uploadFolder, fName);
        Assert.assertTrue(target.exists());
        Assert.assertTrue(FileUtils.contentEquals(source, target));
        
        // cleanup, reset setup
        FileUtils.deleteQuietly(uploadFolder);
        FileUtils.deleteQuietly(artifactsFolder);
        setup.setArtifactsFolder(origArtifactsFolder);
        setup.setUploadFolder(origUploadFolder);
    }
    
}
