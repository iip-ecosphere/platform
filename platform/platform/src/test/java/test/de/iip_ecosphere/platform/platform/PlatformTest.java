/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.platform;

import org.junit.Test;

import de.iip_ecosphere.platform.platform.PersistentAasSetup.ConfiguredPersistenceType;
import de.iip_ecosphere.platform.platform.PlatformAas;
import de.iip_ecosphere.platform.platform.ArtifactsManager;
import de.iip_ecosphere.platform.platform.ArtifactsManager.Artifact;
import de.iip_ecosphere.platform.platform.ArtifactsManager.ArtifactKind;
import de.iip_ecosphere.platform.platform.PersistentAasSetup;
import de.iip_ecosphere.platform.platform.PlatformSetup;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;

/**
 * Platform test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformTest {
    
    /**
     * Simple platform test.
     */
    @Test
    public void testPlatform() {
        PlatformSetup cfg = PlatformSetup.getInstance();

        Assert.assertEquals(8080, cfg.getAas().getServer().getPort());
        Assert.assertEquals(Schema.HTTPS, cfg.getAas().getServer().getSchema());
        Assert.assertEquals("127.0.0.1", cfg.getAas().getServer().getHost());
        Assert.assertEquals("aas", cfg.getAas().getServer().getPath());

        Assert.assertEquals(8081, cfg.getAas().getRegistry().getPort());
        Assert.assertEquals(Schema.HTTPS, cfg.getAas().getRegistry().getSchema());
        Assert.assertEquals("127.0.0.1", cfg.getAas().getRegistry().getHost());
        Assert.assertEquals("registry", cfg.getAas().getRegistry().getPath());

        Assert.assertEquals(8082, cfg.getAas().getImplementation().getPort());
        Assert.assertEquals(Schema.TCP, cfg.getAas().getImplementation().getSchema());
        Assert.assertEquals("127.0.0.1", cfg.getAas().getImplementation().getHost());
        Assert.assertEquals("VAB-TCP", cfg.getAas().getImplementation().getProtocol());
        
        Assert.assertEquals(ConfiguredPersistenceType.MONGO, cfg.getAas().getPersistence());

        // overwrite for local test execution
        AasSetup.createLocalEphemeralSetup(cfg.getAas(), false);
        cfg.getAas().setPersistence(ConfiguredPersistenceType.INMEMORY);
        
        //HTTPS not fully setup with certificates, cannot start LifecycleHandler here
    }
    
    /**
     * Tests the platform aAS via the lifecycle descriptors.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur 
     * @throws URISyntaxException shall not occur
     */
    @Test
    public void testLifecycle() throws IOException, ExecutionException, URISyntaxException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        PersistentAasSetup aasSetup = AasSetup.createLocalEphemeralSetup(new PersistentAasSetup(), false, 
            () -> new PersistentAasSetup());
        aasSetup.setPersistence(ConfiguredPersistenceType.INMEMORY);
        AasSetup oldSetup = AasPartRegistry.setAasSetup(aasSetup);
        PlatformSetup.getInstance().setAas(aasSetup);

        LifecycleHandler.startup(new String[] {});

        testArtifactManager();
        
        LifecycleHandler.shutdown();

        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldM);
    }
    
    /**
     * Tests the artifact manager.
     */
    private void testArtifactManager() throws IOException {
        ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        ArtifactsManager mgr = ArtifactsManager.getInstance();
        Assert.assertNotNull(mgr);
        Assert.assertTrue(mgr.getArtifactCount() > 0);

        Assert.assertNull(mgr.getArtifact(""));
        Artifact a = mgr.getArtifact("art");
        Assert.assertNotNull(a);
        Assert.assertEquals("art", a.getId());
        Assert.assertEquals(ArtifactKind.SERVICE_ARTIFACT, a.getKind());
        Assert.assertNotNull(a.getName());
        Assert.assertNotNull(a.getDescription());
        Assert.assertTrue(a.getAccessUri().toString().startsWith(PlatformSetup.getInstance().getArtifactsUriPrefix()));

        a = mgr.getArtifact("art1");
        Assert.assertNotNull(a);
        Assert.assertEquals("art1", a.getId());
        Assert.assertEquals(ArtifactKind.SERVICE_ARTIFACT, a.getKind());
        Assert.assertNotNull(a.getName());
        Assert.assertNotNull(a.getDescription());
        Assert.assertTrue(a.getAccessUri().toString().startsWith(PlatformSetup.getInstance().getArtifactsUriPrefix()));

        a = mgr.getArtifact("cnt1");
        Assert.assertNotNull(a);
        Assert.assertEquals("cnt1", a.getId());
        Assert.assertEquals(ArtifactKind.CONTAINER, a.getKind());
        Assert.assertNotNull(a.getName());
        Assert.assertNotNull(a.getDescription());
        Assert.assertTrue(a.getAccessUri().toString().startsWith(PlatformSetup.getInstance().getArtifactsUriPrefix()));

        Assert.assertEquals(mgr.getArtifactCount(), CollectionUtils.toList(mgr.artifacts().iterator()).size());
        for (Artifact ar: mgr.artifacts()) {
            System.out.println(" - " + ar.getId() + " " + ar.getName() + " " + ar.getKind() + " " + ar.getVersion() 
                + " " + ar.getAccessUri());
        }
        
        SubmodelElementsCollectionClient sc = new SubmodelElementsCollectionClient(PlatformAas.NAME_SUBMODEL, 
            PlatformAas.NAME_COLL_SERVICE_ARTIFACTS);
        SubmodelElementCollection coll = sc.getSubmodel().getSubmodelElementCollection(
            PlatformAas.NAME_COLL_SERVICE_ARTIFACTS);
        Assert.assertEquals(2, coll.getElementsCount());
        Assert.assertNotNull(coll.getElement("art"));
        Assert.assertNotNull(coll.getElement("art1"));
        
        coll = sc.getSubmodel().getSubmodelElementCollection(
            PlatformAas.NAME_COLL_CONTAINER);
        Assert.assertEquals(1, coll.getElementsCount());
        Assert.assertNotNull(coll.getElement("cnt1"));
        
        // preliminary, not nice
        FileUtils.copyFile(
            new File("src/test/resources/service3.jar"), 
            new File("src/test/resources/artifacts/service3.jar"));
        TimeUtils.sleep(1000); // wait for watcher
        Assert.assertEquals(4, mgr.getArtifactCount());
        
        sc = new SubmodelElementsCollectionClient(PlatformAas.NAME_SUBMODEL, 
            PlatformAas.NAME_COLL_SERVICE_ARTIFACTS);
        coll = sc.getSubmodel().getSubmodelElementCollection(
            PlatformAas.NAME_COLL_SERVICE_ARTIFACTS);        
        Assert.assertEquals(3, coll.getElementsCount());

        coll = sc.getSubmodel().getSubmodelElementCollection(
            PlatformAas.NAME_COLL_CONTAINER);
        Assert.assertEquals(1, coll.getElementsCount()); // unchanged

        FileUtils.deleteQuietly(new File("src/test/resources/artifacts/service3.jar"));
        TimeUtils.sleep(1000); // wait for watcher
        
        //Assert.assertEquals(count, mgr.getArtifactCount()); // watcher unclear although file is gone
        //Assert.assertEquals(count, coll.getElementsCount());
    }

    /**
     * Cleanup.
     */
    @AfterClass
    public static void shutdown() {
        FileUtils.deleteQuietly(new File("src/test/resources/artifacts/service3.jar"));
    }
    
}
