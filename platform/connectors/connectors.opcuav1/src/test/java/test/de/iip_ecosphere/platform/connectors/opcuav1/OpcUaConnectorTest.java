/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.opcuav1;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorFactory;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.opcuav1.DataItem;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.StringUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.plugins.CurrentClassloaderPluginSetupDescriptor;
import de.iip_ecosphere.platform.support.plugins.PluginManager;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace.Namespace;

/**
 * Tests the OPC UA connector (not secure, polling).
 * 
 * @author Holger Eichelberger, SSE
 */
public class OpcUaConnectorTest extends AbstractOpcUaConnectorTest {
    
    private static TestServer testServer;
    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaConnectorTest.class);
    
    /**
     * Sets the test up by starting an embedded OPC UA server.
     * 
     * @throws ExecutionException shall not occur
     * @throws InterruptedException shall not occur
     */
    @BeforeClass
    public static void init() throws ExecutionException, InterruptedException {
        PluginManager.registerPlugin(CurrentClassloaderPluginSetupDescriptor.INSTANCE);
        setSetup(new NoSecuritySetup("milo", NetUtils.getEphemeralPort(), NetUtils.getEphemeralPort()));
        testServer = new TestServer((server) -> new Namespace(server), getSetup());
        testServer.startup().get();
        LOGGER.info("OPC UA server started");
    }
    
    /**
     * Shuts down the test server.
     * 
     * @throws ExecutionException shall not occur
     * @throws InterruptedException shall not occur
     */
    @AfterClass
    public static void shutdown() throws ExecutionException, InterruptedException {
        if (null != testServer) {
            testServer.shutdown().get();
            LOGGER.info("OPC UA server stopped");
            testServer = null;
        }
        AbstractOpcUaConnectorTest.dispose(); // this is dangerous and shall only be done at the very end
    }

    /**
     * Tests the connector in polling mode.
     * 
     * @throws IOException in case that creating the connector fails
     */
    @Test
    public void testWithPolling() throws IOException {
        testConnector(false);
        testWithBrowsePath(false);
    }
    
    /**
     * Tests browse name as browse path (IFW).
     * 
     * @throws IOException in case of I/O problems, shall not occur
     */
    @Test
    public void testWithBrowsePath() throws IOException  {
        testWithBrowsePath(false);
    }

    /**
     * Tests browse name as browse path (IFW).
     * 
     * @param withNotification whether we run this test with notifications
     * @throws IOException in case of I/O problems, shall not occur
     */
    private void testWithBrowsePath(boolean withNotification) throws IOException {
        NotificationMode mo = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        @SuppressWarnings("unchecked")
        Connector<DataItem, Object, VarData, VarData> conn 
            = ConnectorFactory.createConnectorByPlugin(ConnectorDescriptor.PLUGIN_ID_PREFIX + "opcua-v1", 
                () -> getConnectorParameter(), 
                new TranslatingProtocolAdapter<DataItem, Object, VarData, VarData>(
                    new VarDataOutputTranslator(false),
                    new VarDataInputTranslator()));
        Assert.assertNotNull(conn);
        conn.enablePolling(false);
        AtomicReference<VarData> received = new AtomicReference<>();
        conn.setReceptionCallback(new ReceptionCallback<VarData>() {
            
            @Override
            public void received(VarData data) {
                received.set(data);
            }
            
            @Override
            public Class<VarData> getType() {
                return VarData.class;
            }
        });
        conn.connect(getConnectorParameter());
        conn.request(true);
        conn.disconnect();

        Assert.assertNotNull(received.get());
        Assert.assertEquals(Namespace.BNAME_VAL, received.get().myVar, 0.001);
        ActiveAasBase.setNotificationMode(mo);
    }

    /**
     * Just a test class with a single variable.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class VarData {
        
        private double myVar;
        
        @Override
        public String toString() {
            return StringUtils.toStringShortStyle(this);
        }
    }
    
    /**
     * Output translator for {@link VarData}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class VarDataOutputTranslator extends AbstractConnectorOutputTypeTranslator<DataItem, VarData> {
        
        private boolean withNotifications;
        
        /**
         * Creates instance.
         * 
         * @param withNotifications operate with/without notifications (for testing)
         * @param sourceType the source type
         * @param customizer the translator customizer
         */
        public VarDataOutputTranslator(boolean withNotifications) {
            this.withNotifications = withNotifications;
        }

        @Override
        public VarData to(DataItem source) throws IOException {
            ModelAccess access = getModelAccess();
            access = access.stepInto(Namespace.TOP_OBJECTS);
            access = access.stepInto(Namespace.BNAME_ROOT);
            access = access.stepInto(Namespace.BNAME_FOLDER);
            VarData result = new VarData();
            result.myVar = access.getDouble(Namespace.BNAME_VAR);
            access = access.stepOut();
            access = access.stepOut();
            access = access.stepOut();
            return result;
        }

        @Override
        public void initializeModelAccess() throws IOException {
            ModelAccess access = getModelAccess();
            access.useNotifications(withNotifications);
        }

        @Override
        public Class<? extends DataItem> getSourceType() {
            return DataItem.class;
        }

        @Override
        public Class<? extends VarData> getTargetType() {
            return VarData.class;
        }

    }

    /**
     * Input translator for {@link VarData}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class VarDataInputTranslator extends AbstractConnectorInputTypeTranslator<VarData, Object> {
        
        /**
         * Creates instance.
         */
        public VarDataInputTranslator() {
        }

        @Override
        public Object from(VarData data) throws IOException {
            ModelAccess access = getModelAccess();
            access = access.stepInto(Namespace.TOP_OBJECTS);
            access = access.stepInto(Namespace.BNAME_ROOT);
            access = access.stepInto(Namespace.BNAME_FOLDER);
            access.setDouble("myVar", data.myVar);
            access = access.stepOut();
            access = access.stepOut();
            access = access.stepOut();
            return null;
        }

        @Override
        public Class<? extends Object> getSourceType() {
            return Object.class;
        }

        @Override
        public Class<? extends VarData> getTargetType() {
            return VarData.class;
        }

    }
    
    /**
     * Tests the connector in event-based mode.
     * 
     * @throws IOException in case that creating the connector fails
     */
    @Test
    public void testWithNotifications() throws IOException {
        testConnector(true);
    }

}