/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
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
import org.junit.Assert;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.ConnectorExtensionDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorExtensionDescriptor.DefaultConnectorExtension;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.ConnectorParameter.ConnectorParameterBuilder;
import de.iip_ecosphere.platform.connectors.types.AbstractProtocolAdapter;
import de.iip_ecosphere.platform.support.jsl.ExcludeFirst;
import test.de.iip_ecosphere.platform.support.aas.TestWithPlugin;

/**
 * Tests {@link ConnectorExtensionDescriptor}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorExtensionDescriptorTest extends TestWithPlugin {

    private static final String ID = "abba";
    private static final String VALUE = "xyz";

    /**
     * Extension as class to be loaded/referenced by JSL.
     * 
     * @author Holger Eichelberger, SSE
     */
    @ExcludeFirst // reduce priority
    public static class MyConnectorExtension extends DefaultConnectorExtension<String> {

        /**
         * Creates instance. [JSL]
         */
        public MyConnectorExtension() {
            super(ID, () -> VALUE);
        }
        
    }

    /**
     * A "connector" reacting on the extension.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MyConnector extends AbstractConnector<Object, Object, Object, Object> {

        private String extensionValue;
        
        /**
         * Creates an instance, applies a protocol adapter for convenience.
         */
        private MyConnector() {
            // do not use connector extension in constructor!
            super(new AbstractProtocolAdapter<Object, Object, Object, Object>() {

                @Override
                public Object adaptInput(Object data) throws IOException {
                    return data;
                }

                @Override
                public Object adaptOutput(String channel, Object data) throws IOException {
                    return data;
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
                public Class<? extends Object> getProtocolOutputType() {
                    return Object.class;
                }

                @Override
                public Class<? extends Object> getConnectorOutputType() {
                    return Object.class;
                }

                @Override
                public void initializeModelAccess() throws IOException {
                }
                
            });
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
            return "MyConnector";
        }

        @Override
        protected void connectImpl(ConnectorParameter params) throws IOException {
            extensionValue = ConnectorExtensionDescriptor.getExtension(this, String.class, null);
        }

        @Override
        protected void disconnectImpl() throws IOException {
        }

        @Override
        protected void writeImpl(Object data) throws IOException {
        }

        @Override
        protected Object read() throws IOException {
            return null;
        }

        @Override
        protected void error(String message, Throwable th) {
        }
        
    }
    
    /**
     * Tests the extension.
     * 
     * @throws IOException shall not happen if successful
     */
    @Test
    public void testExtension() throws IOException {
        MyConnector conn = new MyConnector();
        conn.setInstanceIdentification(ID); // required, shall be done by code generation
        Assert.assertNull(conn.extensionValue); // nothing happened
        conn.connect(ConnectorParameterBuilder.newBuilder("localhost", 0).build());
        Assert.assertEquals(VALUE, conn.extensionValue); // value is there
        conn.disconnect();
    }
    
}
