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

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.AbstractChannelConnector;
import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorFactory;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ChannelProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.iip_aas.NameplateSetup.Service;
import de.iip_ecosphere.platform.support.iip_aas.Version;

import org.junit.Assert;

/**
 * Tests {@link ConnectorFactory}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorFactoryTest {

    /**
     * The testing factory for two alternative Mqtt connector implementations differed by version.
     * 
     * @param <CO> the output type to the IIP-Ecosphere platform
     * @param <CI> the input type from the IIP-Ecosphere platform
     * @author Holger Eichelberger, SSE
     */
    public static class MyMqttConnectorFactory<CO, CI> implements ConnectorFactory<byte[], byte[], CO, CI, 
        ChannelProtocolAdapter<byte[], byte[], CO, CI>> {

        @SuppressWarnings("unchecked")
        @Override
        public Connector<byte[], byte[], CO, CI> createConnector(ConnectorParameter params,
            ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
            Connector<byte[], byte[], CO, CI> result;
            if (ConnectorFactory.hasVersion(params) && params.getService().getVersion().getSegment(0) == 5) {
                result = new MyMqttv5Connector<CO, CI>(adapter);
            } else {
                result = new MyMqttv3Connector<CO, CI>(adapter);
            }
            return result;
        }
        
    }
    
    /**
     * First test channel connector.
     *
     * @param <CO> the output type to the IIP-Ecosphere platform
     * @param <CI> the input type from the IIP-Ecosphere platform
     * @author Holger Eichelberger, SSE
     */
    public static class MyMqttv3Connector<CO, CI> extends AbstractChannelConnector<byte[], byte[], CO, CI> {
        
        /**
         * Creates a connector instance.
         * 
         * @param adapter the protocol adapter(s)
         * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
         */
        @SafeVarargs
        public MyMqttv3Connector(ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
            super(null, adapter);
        }

        @Override
        public void dispose() {
        }

        @Override
        public String supportedEncryption() {
            return null;
        }

        @Override
        public String enabledEncryption() {
            return null;
        }

        @Override
        public String getName() {
            return "MyMqttv3";
        }

        @Override
        protected void writeImpl(byte[] data, String channel) throws IOException {
        }

        @Override
        protected void connectImpl(ConnectorParameter params) throws IOException {
        }

        @Override
        protected void disconnectImpl() throws IOException {
        }

        @Override
        protected byte[] read() throws IOException {
            return null;
        }

        @Override
        protected void error(String message, Throwable th) {
        }
        
    }

    /**
     * Second test channel connector.
     *
     * @param <CO> the output type to the IIP-Ecosphere platform
     * @param <CI> the input type from the IIP-Ecosphere platform
     * @author Holger Eichelberger, SSE
     */
    public static class MyMqttv5Connector<CO, CI> extends AbstractChannelConnector<byte[], byte[], CO, CI> {

        /**
         * Creates a connector instance.
         * 
         * @param adapter the protocol adapter(s)
         * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
         */
        @SafeVarargs
        public MyMqttv5Connector(ChannelProtocolAdapter<byte[], byte[], CO, CI>... adapter) {
            super(null, adapter);
        }

        @Override
        public void dispose() {
        }

        @Override
        public String supportedEncryption() {
            return null;
        }

        @Override
        public String enabledEncryption() {
            return null;
        }

        @Override
        public String getName() {
            return "MyMqttv5";
        }

        @Override
        protected void writeImpl(byte[] data, String channel) throws IOException {
        }

        @Override
        protected void connectImpl(ConnectorParameter params) throws IOException {
        }

        @Override
        protected void disconnectImpl() throws IOException {
        }

        @Override
        protected byte[] read() throws IOException {
            return null;
        }

        @Override
        protected void error(String message, Throwable th) {
        }
        
    }

    /**
     * Testing channel protocol adapter.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyMqttProtocolAdapter implements ChannelProtocolAdapter<byte[], byte[], Object, Object> {

        @Override
        public byte[] adaptInput(Object data) throws IOException {
            return null;
        }

        @Override
        public Object adaptOutput(String channel, byte[] data) throws IOException {
            return null;
        }

        @Override
        public Class<? extends byte[]> getProtocolInputType() {
            return byte[].class;
        }

        @Override
        public Class<? extends Object> getConnectorInputType() {
            return Object.class;
        }

        @Override
        public Class<? extends byte[]> getProtocolOutputType() {
            return byte[].class;
        }

        @Override
        public Class<? extends Object> getConnectorOutputType() {
            return Object.class;
        }

        @Override
        public ModelAccess getModelAccess() {
            return null;
        }

        @Override
        public void setModelAccess(ModelAccess modelAccess) {
        }

        @Override
        public void initializeModelAccess() throws IOException {
        }

        @Override
        public String getInputChannel() {
            return null;
        }

        @Override
        public String getOutputChannel() {
            return null;
        }
        
    }

    /**
     * Some data item.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class DataItem {
    }
    
    /**
     * A testing protocol adapter connector.
     * 
     * @param <CO> the output type to the IIP-Ecosphere platform
     * @param <CI> the input type from the IIP-Ecosphere platform
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyOpcUaConnector<CO, CI> extends AbstractConnector<DataItem, Object, CO, CI> {

        /**
         * Creates an instance and installs the protocol adapter(s).
         * 
         * @param adapter the protocol adapter(s)
         * @throws IllegalArgumentException if {@code adapter} is <b>null</b> or empty or adapters are <b>null</b>
         */
        @SafeVarargs
        public MyOpcUaConnector(ProtocolAdapter<DataItem, Object, CO, CI>... adapter) {
            super(null, adapter);
        }

        @Override
        public void dispose() {
        }

        @Override
        public String supportedEncryption() {
            return null;
        }

        @Override
        public String enabledEncryption() {
            return null;
        }

        @Override
        public String getName() {
            return "MyOpcUa";
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
        protected DataItem read() throws IOException {
            return null;
        }

        @Override
        protected void error(String message, Throwable th) {
        }

    }

    /**
     * A protocol adapter class for {@link MyOpcUaConnector}.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class MyOpcProtocolAdapter implements ProtocolAdapter<DataItem, Object, Object, Object> {

        @Override
        public Object adaptInput(Object data) throws IOException {
            return null;
        }

        @Override
        public Object adaptOutput(String channel, DataItem data) throws IOException {
            return null;
        }

        @Override
        public Class<? extends Object> getProtocolInputType() {
            return Object.class;
        }

        @Override
        public Class<? extends Object> getConnectorInputType() {
            return Object.class;
        }

        @Override
        public Class<? extends DataItem> getProtocolOutputType() {
            return DataItem.class;
        }

        @Override
        public Class<? extends Object> getConnectorOutputType() {
            return Object.class;
        }

        @Override
        public ModelAccess getModelAccess() {
            return null;
        }

        @Override
        public void setModelAccess(ModelAccess modelAccess) {
        }

        @Override
        public void initializeModelAccess() throws IOException {
        }
    };

    /**
     * Tests {@link ConnectorFactory}.
     * 
     * @author Holger Eichelberger, SSE
     */
    @Test
    public void testFactory() {
        ConnectorParameter v0p = ConnectorParameterBuilder.newBuilder("l", 0).build();

        Service s3 = new Service();
        s3.setVersion(new Version(3));
        ConnectorParameter v3p = ConnectorParameterBuilder.newBuilder("l", 0).setService(s3).build();
        
        Service s5 = new Service();
        s5.setVersion(new Version(5));
        ConnectorParameter v5p = ConnectorParameterBuilder.newBuilder("l", 0).setService(s5).build();

        Connector<DataItem, Object, Object, Object> oc = ConnectorFactory.createConnector(
            MyOpcUaConnector.class.getName(), () -> v3p, new MyOpcProtocolAdapter());
        Assert.assertTrue(oc instanceof MyOpcUaConnector);

        Connector<byte[], byte[], Object, Object> mc = ConnectorFactory.createConnector(
            MyMqttConnectorFactory.class.getName(), () -> v5p, new MyMqttProtocolAdapter());
        Assert.assertTrue(mc instanceof MyMqttv5Connector);
        mc = ConnectorFactory.createConnector(
            MyMqttConnectorFactory.class.getName(), () -> v3p, new MyMqttProtocolAdapter());
        Assert.assertTrue(mc instanceof MyMqttv3Connector);
        mc = ConnectorFactory.createConnector(
            MyMqttConnectorFactory.class.getName(), () -> v0p, new MyMqttProtocolAdapter());
        Assert.assertTrue(mc instanceof MyMqttv3Connector);
    }

}
