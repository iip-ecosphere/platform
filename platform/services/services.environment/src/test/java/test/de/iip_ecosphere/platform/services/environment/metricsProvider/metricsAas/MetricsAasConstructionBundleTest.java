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

package test.de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructionBundle;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsExtractorRestClient;
import static test.de.iip_ecosphere.platform.services.environment.metricsProvider.utils.TestUtils.assertThrows;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Tests the AAS construction.
 * 
 * @author Miguel Gomez
 */
public class MetricsAasConstructionBundleTest {

    private static SubmodelBuilder smBuilder;
    private static SubmodelBuilder smBuilder2;
    private static InvocablesCreator iCreator;
    private static InvocablesCreator iCreator2;
    private static MetricsExtractorRestClient client;
    private static MetricsExtractorRestClient client2;
    private static ProtocolServerBuilder pBuilder;
    private static ProtocolServerBuilder pBuilder2;
    private static MetricsAasConstructionBundle bundle;

    /**
     * Tests setting up an AAS.
     */
    @BeforeClass
    public static void setUpVariables() {
        ServerAddress vabServerAddress = new ServerAddress(Schema.HTTP);
        AasFactory factory = AasFactory.getInstance();

        AasBuilder aasBuilder = factory.createAasBuilder("aasname", "urn:::aas");
        iCreator = factory.createInvocablesCreator(AasFactory.DEFAULT_PROTOCOL, vabServerAddress.getHost(),
                vabServerAddress.getPort());
        smBuilder = aasBuilder.createSubmodelBuilder("smname", "urn:::sm");
        pBuilder = AasFactory.getInstance().createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL,
                vabServerAddress.getPort());
        client = new MetricsExtractorRestClient("localhost", 8080);

        AasBuilder aasBuilder2 = factory.createAasBuilder("aasname2", "urn:::aas2");
        iCreator2 = factory.createInvocablesCreator(AasFactory.DEFAULT_PROTOCOL, vabServerAddress.getHost(),
                vabServerAddress.getPort());
        smBuilder2 = aasBuilder2.createSubmodelBuilder("smname2", "urn:::sm2");
        pBuilder2 = AasFactory.getInstance().createProtocolServerBuilder(AasFactory.DEFAULT_PROTOCOL,
                vabServerAddress.getPort());
        client2 = new MetricsExtractorRestClient("localhost", 4040);
    }

    /**
     * Tests initializing {@link MetricsAasConstructionBundle}.
     */
    @Before
    public void setUpBundle() {
        bundle = new MetricsAasConstructionBundle(smBuilder, pBuilder, iCreator, client);
    }

    /**
     * Tests initializing {@link MetricsAasConstructionBundle}.
     */
    @Test
    public void testInitOk() {
        MetricsAasConstructionBundle bundle = new MetricsAasConstructionBundle(smBuilder, pBuilder, iCreator, client);
        bundle.setFilter(null);

        assertNotNull(bundle);
        assertEquals(smBuilder, bundle.getSubmodelBuilder());
        assertEquals(iCreator, bundle.getInvocablesCreator());
        assertEquals(client, bundle.getClient());
        assertEquals(pBuilder, bundle.getProtocolBuilder());
        assertNull(bundle.getFilter());
    }

    /**
     * Tests initializing {@link MetricsAasConstructionBundle}.
     */
    @Test
    public void testInitNullSubmodel() {
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsAasConstructionBundle(null, pBuilder, iCreator, client));
    }

    /**
     * Tests initializing {@link MetricsAasConstructionBundle}.
     */
    @Test
    public void testInitNullProtocolBuilder() {
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsAasConstructionBundle(smBuilder, null, iCreator, client));
    }

    /**
     * Tests initializing {@link MetricsAasConstructionBundle}.
     */
    @Test
    public void testInitNullInvocablesCreator() {
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsAasConstructionBundle(smBuilder, pBuilder, null, client));
    }

    /**
     * Tests initializing {@link MetricsAasConstructionBundle}.
     */
    @Test
    public void testInitNullClient() {
        assertThrows(IllegalArgumentException.class,
                () -> new MetricsAasConstructionBundle(smBuilder, pBuilder, iCreator, null));
    }

    /**
     * Tests setting the submodel builder.
     */
    @Test
    public void testSetSubmodelBuilder() {
        assertEquals(smBuilder, bundle.getSubmodelBuilder());
        bundle.setSubmodelBuilder(smBuilder2);
        assertEquals(smBuilder2, bundle.getSubmodelBuilder());
    }

    /**
     * Tests setting the invocables creator.
     */
    @Test
    public void testSetInvocablesCreator() {
        assertEquals(iCreator, bundle.getInvocablesCreator());
        bundle.setInvocablesCreator(iCreator2);
        assertEquals(iCreator2, bundle.getInvocablesCreator());
    }

    /**
     * Tests setting the client.
     */
    @Test
    public void testSetClient() {
        assertEquals(client, bundle.getClient());
        bundle.setClient(client2);
        assertEquals(client2, bundle.getClient());
    }

    /**
     * Tests setting the protocol builder.
     */
    @Test
    public void testSetProtocolBuilder() {
        assertEquals(pBuilder, bundle.getProtocolBuilder());
        bundle.setProtocolBuilder(pBuilder2);
        assertEquals(pBuilder2, bundle.getProtocolBuilder());
    }

}
