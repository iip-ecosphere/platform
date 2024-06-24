/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
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

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.connectors.AbstractThreadedConnector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;

/**
 * Tests {@link AbstractThreadedConnector}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractThreadedConnectorTest {
    
    private int disposed;

    /**
     * A simple model access class for disposed thread testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyModelAccess extends AbstractModelAccess {

        /**
         * Creates an instance.
         */
        protected MyModelAccess() {
            super(null);
        }

        @Override
        public String topInstancesQName() {
            return null;
        }

        @Override
        public String getQSeparator() {
            return null;
        }

        @Override
        public Object call(String qName, Object... args) throws IOException {
            return null;
        }

        @Override
        public Object get(String qName) throws IOException {
            return null;
        }

        @Override
        public void set(String qName, Object value) throws IOException {
        }

        @Override
        public <T> T getStruct(String qName, Class<T> type) throws IOException {
            return null;
        }

        @Override
        public void setStruct(String qName, Object value) throws IOException {
        }

        @Override
        public void registerCustomType(Class<?> cls) throws IOException {
        }

        @Override
        public void monitor(int notificationInterval, String... qNames) throws IOException {
        }

        @Override
        public void monitorModelChanges(int notificationInterval) throws IOException {
        }

        @Override
        public ModelAccess stepInto(String name) throws IOException {
            return null;
        }

        @Override
        public ModelAccess stepOut() {
            return null;
        }

        @Override
        public ConnectorParameter getConnectorParameter() {
            return null;
        }
        
        @Override
        public void dispose() {
            disposed++;
        }
        
    }
    
    /**
     * A connector for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private class MyConnector extends AbstractThreadedConnector<Object, Object, Object, Object, MyModelAccess> {

        /**
         * Creates an instance.
         */
        protected MyConnector() {
            super(new TranslatingProtocolAdapter<>(new IdentityOutputTranslator(), new IdentityInputTranslator()));
            setModelAccessSupplier(() -> new MyModelAccess());
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
            return null;
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
        protected Object read() throws IOException {
            return new Object(); // data
        }

        @Override
        protected void error(String message, Throwable th) {
        }
        
    }
    
    /**
     * Tests {@link AbstractThreadedConnector}.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testConnector() throws IOException {
        NotificationMode mode = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        MyConnector conn = new MyConnector();
        conn.connect(ConnectorParameter.ConnectorParameterBuilder.newBuilder("localhost", 1234).build());
        System.out.println("Main thread read/write");                
        conn.write(new Object());
        conn.request(true);
        Assert.assertEquals(0, disposed);
        new Thread(() -> {
            try {
                System.out.println("Additional thread read/write");                
                conn.write(new Object());
                conn.request(true);
                TimeUtils.sleep(500);
            } catch (IOException e) {
                Assert.fail("No exception expected.");
            }
        }).start();
        System.out.println("Waiting for cleanup/disposal... >5s");
        TimeUtils.sleep(conn.getCleanupPeriod() + 2000);
        Assert.assertEquals(1, disposed);
        conn.disconnect();
        ActiveAasBase.setNotificationMode(mode);
    }

}
