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

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorRegistry;
import de.iip_ecosphere.platform.connectors.ConnectorsAas;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.types.AbstractProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeAdapter;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasBuildResult;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.ClassUtility;
import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * Tests the connectors AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorsAasTest {

    private static final String NAME_CONN1 = "Connector1";
    private static final String NAME_CONN2 = "Connector2";
    
    /**
     * A test descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Connector1Descriptor implements ConnectorDescriptor {

        @Override
        public String getName() {
            return NAME_CONN1;
        }

        @Override
        public Class<?> getType() {
            return Connector1.class;
        }
        
    }

    /**
     * Implements a fake/mock connector with {@link MachineConnector}.
     * 
     * @param <CO> the connector output type
     * @param <CI> the connector input type
     * @author Holger Eichelberger, SSE
     */
    @MachineConnector(hasModel = false, supportsEvents = false, supportsHierarchicalQNames = false, 
        supportsModelCalls = false, supportsModelProperties = false, supportsModelStructs = false)
    private static class Connector1<CO, CI> extends AbstractConnector<Object, Object, CO, CI> {

        /**
         * Creates an instance.
         * 
         * @param adapter the protocol adapter
         */
        protected Connector1(ProtocolAdapter<Object, Object, CO, CI> adapter) {
            super(adapter);
        }

        @Override
        public void dispose() {
        }

        @Override
        public String getName() {
            return NAME_CONN1;
        }

        @Override
        protected void connectImpl(ConnectorParameter params) throws IOException {
        }

        @Override
        protected void disconnectImpl() throws IOException {
        }

        @Override
        protected void writeImpl(Object data) throws IOException {
        }

        @Override
        public Object read() throws IOException {
            return null;
        }

        @Override
        protected void error(String message, Throwable th) {
        }

        @Override
        public String supportedEncryption() {
            return null;
        }

        @Override
        public String enabledEncryption() {
            return null;
        }
        
    }

    /**
     * A test descriptor.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Connector2Descriptor implements ConnectorDescriptor {

        @Override
        public String getName() {
            return NAME_CONN2;
        }

        @Override
        public Class<?> getType() {
            return Connector2.class;
        }
        
    }

    /**
     * Implements a fake/mock connector without {@link MachineConnector}.
     * 
     * @param <CO> the connector output type
     * @param <CI> the connector input type
     * @author Holger Eichelberger, SSE
     */
    private static class Connector2<CO, CI> extends AbstractConnector<byte[], byte[], CO, CI> {

        /**
         * Creates an instance.
         * 
         * @param adapter the protocol adapter
         */
        protected Connector2(ProtocolAdapter<byte[], byte[], CO, CI> adapter) {
            super(adapter);
        }

        @Override
        public void dispose() {
        }

        @Override
        public String getName() {
            return NAME_CONN2;
        }

        @Override
        protected void connectImpl(ConnectorParameter params) throws IOException {
        }

        @Override
        protected void disconnectImpl() throws IOException {
        }

        @Override
        protected void writeImpl(byte[] data) throws IOException {
        }

        @Override
        protected byte[] read() throws IOException {
            return null;
        }

        @Override
        protected void error(String message, Throwable th) {
        }

        @Override
        public String supportedEncryption() {
            return null;
        }

        @Override
        public String enabledEncryption() {
            return null;
        }
        
    }
    
    /**
     * Some input data. Class structure is read via reflection. Don't remove the attributes! 
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class DataIn1 {
        @SuppressWarnings("unused")
        private String data;
    }

    /**
     * Some output data. Class structure is read via reflection. Don't remove the attributes!
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class DataOut1 {
        @SuppressWarnings("unused")
        private int value1;
        @SuppressWarnings("unused")
        private int value2;
    }
    
    /**
     * Some input data. Class structure is read via reflection. Don't remove the attributes! 
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class DataIn2 {
        @SuppressWarnings("unused")
        private double dData;
    }

    /**
     * Some output data. Class structure is read via reflection. Don't remove the attributes!
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class DataOut2 {
        @SuppressWarnings("unused")
        private String value1;
        @SuppressWarnings("unused")
        private boolean value2;
    }

    /**
     * Print the AAS to the console.
     * 
     * @param aas the AAS to print
     */
    private void printOut(Aas aas) {
        aas.accept(new AasPrintVisitor()); 
    }

    /**
     * Tests the connectors AAS.
     * 
     * throws IOException shall not occur
     */
    @Test
    public void testAas() throws IOException {
        NotificationMode oldP = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS); // deterministic tests
        // multiple test runs may load the same descriptor multiple times
        ConnectorRegistry.getRegisteredConnectorDescriptorsLoader().reload();
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(ConnectorsAas.class));
        // obtain the plattform AAS and go then on with the connectors sub-models
        AasBuildResult bResult = AasPartRegistry.build();
        List<Aas> aasList = bResult.getAas();
        Server implServer = bResult.getProtocolServerBuilder().build();
        implServer.start();
        Aas aas = AasPartRegistry.getAas(aasList, AasPartRegistry.NAME_AAS);
        Assert.assertNotNull(aas);
        printOut(aas);
        testDescriptorsSubmodel(aas);

        printOut(aas);
        AasSetup oldSetup = AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        Server server = AasPartRegistry.deploy(aasList).start();

        // do not go on with "aas" here... that is the local, non-deployed AAS. Connectors will modify deployed AAS.
        Assert.assertNotNull(AasPartRegistry.retrieveIipAas());
        testDescriptorsSubmodel(aas);
        List<Connector<?, ?, ?, ?>> connectors = new ArrayList<>();
        testActiveDescriptors(connectors, 0);
        
        Connector1<DataOut1, DataIn1> connector1 = createConnector1Instance();
        connectors.add(connector1);
        connector1.connect(null);
        
        System.out.println("Connected connector 1");
        testActiveDescriptors(connectors, 1); 

        Connector2<DataOut2, DataIn2> connector2 = createConnector2Instance();
        connectors.add(connector2);
        connector2.connect(null);
        System.out.println("Connected connector 2");
        testActiveDescriptors(connectors, 2); 
        
        connectors.remove(connector1);
        connector1.disconnect();
        System.out.println("Disconnected connector 1");
        testActiveDescriptors(connectors, 1);
        
        connectors.remove(connector2);
        connector2.disconnect();
        System.out.println("Disconnected connector 2");
        testActiveDescriptors(connectors, 0);
        
        server.stop(true);
        implServer.stop(true);
        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldP);
    }
    
    /**
     * Creates an instance of {@link Connector1}.
     * 
     * @return the instance
     */
    private static Connector1<DataOut1, DataIn1> createConnector1Instance() {
        return new Connector1<DataOut1, DataIn1>(
            new AbstractProtocolAdapter<Object, Object, DataOut1, DataIn1>() {

                @Override
                public Object adaptInput(DataIn1 data) throws IOException {
                    return null;
                }
    
                @Override
                public DataOut1 adaptOutput(Object data) throws IOException {
                    return null;
                }
    
                @Override
                public Class<? extends Object> getProtocolInputType() {
                    return Object.class;
                }
    
                @Override
                public Class<? extends DataIn1> getConnectorInputType() {
                    return DataIn1.class;
                }
    
                @Override
                public Class<? extends Object> getProtocolOutputType() {
                    return Object.class;
                }
    
                @Override
                public Class<? extends DataOut1> getConnectorOutputType() {
                    return DataOut1.class;
                }
    
                @Override
                public void initializeModelAccess() throws IOException {
                }
            });
    }
    
    /**
     * Creates an instance of {@link Connector1}.
     * 
     * @return the instance
     */
    private static Connector2<DataOut2, DataIn2> createConnector2Instance() {
        Serializer<DataIn2> dataIn2Serializer = new Serializer<DataIn2>() {

            @Override
            public DataIn2 from(byte[] data) throws IOException {
                return null;
            }

            @Override
            public byte[] to(DataIn2 source) throws IOException {
                return null;
            }

            @Override
            public DataIn2 clone(DataIn2 origin) throws IOException {
                return null;
            }

            @Override
            public Class<DataIn2> getType() {
                return DataIn2.class;
            }
        };
        
        Serializer<DataOut2> dataOut2Serializer = new Serializer<DataOut2>() {

            @Override
            public DataOut2 from(byte[] data) throws IOException {
                return null;
            }

            @Override
            public byte[] to(DataOut2 source) throws IOException {
                return null;
            }

            @Override
            public DataOut2 clone(DataOut2 origin) throws IOException {
                return null;
            }

            @Override
            public Class<DataOut2> getType() {
                return DataOut2.class;
            }
        };
        
        return new Connector2<DataOut2, DataIn2>(
            new TranslatingProtocolAdapter<byte[], byte[], DataOut2, DataIn2>(
                new ConnectorOutputTypeAdapter<DataOut2>(dataOut2Serializer), 
                new ConnectorInputTypeAdapter<DataIn2>(dataIn2Serializer)));
    }
    
    /**
     * Tests the connector descriptors in their sub model.
     * 
     * @param aas the AAS to inspect
     */
    private void testDescriptorsSubmodel(Aas aas) {
        Submodel cdsm = aas.getSubmodel(ConnectorsAas.NAME_DESCRIPTORS_SUBMODEL);
        Assert.assertNotNull(cdsm);
        
        Iterator<ConnectorDescriptor> iter = ConnectorRegistry.getRegisteredConnectorDescriptors();
        while (iter.hasNext()) {
            ConnectorDescriptor desc = iter.next();
            if (Connector1Descriptor.class == desc.getClass() || Connector2Descriptor.class == desc.getClass()) {
                SubmodelElementCollection sec = cdsm.getSubmodelElementCollection(ClassUtility.getName(desc.getType()));
                Assert.assertNotNull(sec);
                try {
                    Assert.assertEquals(desc.getName(), sec.getProperty(ConnectorsAas.NAME_DESC_VAR_NAME).getValue());
                    MachineConnector mc = ConnectorsAas.getMachineConnectorAnnotation(desc.getClass());
                    Assert.assertNotNull(mc);
                    assertBooleanProperty(mc.supportsEvents(), sec, ConnectorsAas.NAME_DESC_VAR_SUPPORTS_EVENTS);
                    assertBooleanProperty(mc.hasModel(), sec, ConnectorsAas.NAME_DESC_VAR_HAS_MODEL);
                    assertBooleanProperty(mc.supportsHierarchicalQNames(), sec, 
                        ConnectorsAas.NAME_DESC_VAR_SUPPORTS_QNAMES);
                    assertBooleanProperty(mc.supportsModelCalls(), sec, ConnectorsAas.NAME_DESC_VAR_SUPPORTS_CALLS);
                    assertBooleanProperty(mc.supportsModelProperties(), sec, 
                        ConnectorsAas.NAME_DESC_VAR_SUPPORTS_PROPERTIES);
                    assertBooleanProperty(mc.supportsModelStructs(), sec, ConnectorsAas.NAME_DESC_VAR_SUPPORTS_STRUCTS);
                } catch (ExecutionException e) {
                    Assert.fail(e.getMessage());
                }
            }
        }
    }

    /**
     * Asserts a boolean property on a sub-model element collection.
     * 
     * @param expected the expected value
     * @param sec the sub-model element collection
     * @param propertyName the property name
     * @throws ExecutionException in case that the property cannot be read
     */
    private static void assertBooleanProperty(boolean expected, SubmodelElementCollection sec, String propertyName) 
        throws ExecutionException {
        Property prop = sec.getProperty(propertyName);
        Assert.assertNotNull(prop);
        Assert.assertEquals(expected, prop.getValue());
    }

    /**
     * Asserts a String property on a sub-model element collection.
     * 
     * @param expected the expected value
     * @param sec the sub-model element collection
     * @param propertyName the property name
     * @throws ExecutionException in case that the property cannot be read
     */
    private static void assertStringProperty(String expected, SubmodelElementCollection sec, String propertyName) 
        throws ExecutionException {
        Property prop = sec.getProperty(propertyName);
        Assert.assertNotNull(prop);
        Assert.assertEquals(expected, prop.getValue());
    }
    
    /**
     * Asserts a reference element.
     * 
     * @param hasRef whether there shall be a reference value
     * @param sec the sub-model element collection
     * @param propertyName the property name
     * @throws ExecutionException in case that the property cannot be read
     */
    private static void assertReferenceElement(boolean hasRef, SubmodelElementCollection sec, String propertyName) 
        throws ExecutionException {
        ReferenceElement refElt = sec.getReferenceElement(propertyName);
        Assert.assertNotNull("RefElt " + propertyName + " on " + sec.getIdShort() + " does not exist", refElt);
        Assert.assertNotNull(refElt.getValue());
        Assert.assertEquals(hasRef, refElt.getValue().hasReference());
    }

    /**
     * Tests the connector descriptors in their sub-model. As we operate on a deployed modified AAS, it is 
     * important to retrieve a new one.
     * 
     * @param connectors all connectors instantiated for this test, active an non-active
     * @param expectedActive the expected number of connectors in the active sub-model
     * @throws IOException shall not occur
     */
    private void testActiveDescriptors(List<Connector<?, ?, ?, ?>> connectors, int expectedActive) throws IOException {
        Aas aas = AasPartRegistry.retrieveIipAas();
        printOut(aas);
        System.out.println();
        
        Assert.assertNotNull(aas);
        Submodel cdsm = aas.getSubmodel(ConnectorsAas.NAME_CONNECTORS_SUBMODEL);
        Assert.assertNotNull(cdsm);
        Assert.assertEquals(expectedActive, cdsm.getSubmodelElementsCount());
        
        for (Connector<?, ?, ?, ?> c : connectors) {
            String id = ClassUtility.getId(ConnectorsAas.NAME_SMC_CONNECTOR_PREFIX, c);
            SubmodelElementCollection connElt = cdsm.getSubmodelElementCollection(id);
            Assert.assertNotNull(connElt);
            // ClassUtility, we do not check the result
            try {
                assertReferenceElement(true, connElt, ConnectorsAas.NAME_SMC_VAR_IN);
                assertReferenceElement(true, connElt, ConnectorsAas.NAME_SMC_VAR_OUT);
                assertStringProperty(c.getName(), connElt, ConnectorsAas.NAME_SMC_VAR_CONNECTOR);
                assertReferenceElement(true, connElt, ConnectorsAas.NAME_SMC_VAR_DESCRIPTOR);
            } catch (ExecutionException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

}
