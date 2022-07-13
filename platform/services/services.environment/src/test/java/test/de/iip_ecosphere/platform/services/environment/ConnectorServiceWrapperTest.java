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

package test.de.iip_ecosphere.platform.services.environment;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.services.environment.ConnectorServiceWrapper;
import de.iip_ecosphere.platform.services.environment.MockingConnectorServiceWrapper;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.support.iip_aas.Version;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

/**
 * Tests the {@link ConnectorServiceWrapper} and {@link MockingConnectorServiceWrapper}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConnectorServiceWrapperTest {

    /**
     * Tests the {@link ConnectorServiceWrapper}.
     * 
     * @throws ExecutionException shall not occur in successful tests
     */
    @Test
    public void testWrapper() throws ExecutionException {
        NotificationMode mo = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        YamlService yaml = getYamlService();
        ConnectorImpl<Object, Object> conn = new ConnectorImpl<>(createConnectorAdapter());
        ConnectorParameter param = ConnectorParameter.ConnectorParameterBuilder
            .newBuilder("localhost", 0)
            .build();
        ConnectorServiceWrapper<Object, Object, Object, Object> wrapper 
            = new ConnectorServiceWrapper<>(getYamlService(), conn, () -> param);
        testWrapper(wrapper, yaml);
        ActiveAasBase.setNotificationMode(mo);
    }
    
    /**
     * Tests the {@link MockingConnectorServiceWrapper}.
     * 
     * @throws ExecutionException shall not occur in successful tests
     */
    @Test
    public void testMockingWrapper() throws ExecutionException {
        NotificationMode mo = ActiveAasBase.setNotificationMode(NotificationMode.NONE);
        YamlService yaml = getYamlService();
        ConnectorImpl<Object, Object> conn = new ConnectorImpl<>(createConnectorAdapter());
        ConnectorParameter param = ConnectorParameter.ConnectorParameterBuilder
            .newBuilder("localhost", 0)
            .build();
        
        MockingConnectorServiceWrapper<Object, Object, Object, Object> wrapper 
            = new MockingConnectorServiceWrapper<>(yaml, conn, () -> param);

        AtomicInteger inRcv = new AtomicInteger(0);
        AtomicInteger rcv = new AtomicInteger(0);
        wrapper.setInputCallback(new ReceptionCallback<Object>() {
            
            @Override
            public void received(Object data) {
                inRcv.incrementAndGet();
            }
            
            @Override
            public Class<Object> getType() {
                return Object.class;
            }
        });
        wrapper.setReceptionCallback(new ReceptionCallback<Object>() {

            @Override
            public void received(Object data) {
                rcv.incrementAndGet();
            }

            @Override
            public Class<Object> getType() {
                return Object.class;
            }
        });
        
        testWrapper(wrapper, yaml);
        wrapper.send(new Object()); // for inRcv
        TimeUtils.sleep(1000);
        Assert.assertTrue(inRcv.get() > 0);
        Assert.assertTrue(rcv.get() > 0);
        
        ActiveAasBase.setNotificationMode(mo);
    }

    /**
     * Returns a YAML service for testing.
     * 
     * @return the YAML instance
     */
    private YamlService getYamlService() {
        YamlService yaml = new YamlService();
        yaml.setId("id");
        yaml.setName("name");
        yaml.setKind(ServiceKind.PROBE_SERVICE);
        yaml.setDescription("desc");
        yaml.setVersion(new Version("1.0.0"));
        yaml.setDeployable(true);
        return yaml;
    }

    /**
     * Identity output translator.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class IdentityOutputTranslator extends AbstractConnectorOutputTypeTranslator<Object, Object> {

        @Override
        public void initializeModelAccess() throws IOException {
        }

        @Override
        public Class<? extends Object> getSourceType() {
            return Object.class;
        }

        @Override
        public Class<? extends Object> getTargetType() {
            return Object.class;
        }

        @Override
        public Object to(Object source) throws IOException {
            return source;
        }
        
    }

    /**
     * Identity input translator.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class IdentityInputTranslator extends AbstractConnectorInputTypeTranslator<Object, Object> {

        @Override
        public Class<? extends Object> getSourceType() {
            return Object.class;
        }

        @Override
        public Class<? extends Object> getTargetType() {
            return Object.class;
        }

        @Override
        public Object from(Object data) throws IOException {
            return data;
        }
        
    }

    /**
     * Returns a connector adapter that literally does nothing, just for testing.
     * 
     * @return the connector adapter
     */
    private TranslatingProtocolAdapter<Object, Object, Object, Object> createConnectorAdapter() {
        return new TranslatingProtocolAdapter<Object, Object, Object, Object>(
            new IdentityOutputTranslator(), 
            new IdentityInputTranslator());
    }
    
    /**
     * Generic test for a wrapper instance based on the constituting {@code yaml} instance.
     * 
     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @param wrapper the wrapper instance to be tested
     * @param yaml the service deployment information
     * 
     * @throws ExecutionException shall not occur in successful tests
     */
    private <O, I, CO, CI> void testWrapper(ConnectorServiceWrapper<O, I, CO, CI> wrapper, YamlService yaml) 
        throws ExecutionException {
        Assert.assertNotNull(wrapper.getConnector());
        Assert.assertEquals(yaml.getId(), wrapper.getId());
        Assert.assertEquals(yaml.getName(), wrapper.getName());
        Assert.assertEquals(yaml.getDescription(), wrapper.getDescription());
        Assert.assertEquals(yaml.getKind(), wrapper.getKind());
        Assert.assertEquals(yaml.getVersion(), wrapper.getVersion());
        
        Assert.assertEquals(ServiceState.AVAILABLE, wrapper.getState());
        Assert.assertNull(wrapper.getParameterConfigurer(null)); // no param

        wrapper.enablePolling(false);
        wrapper.setState(ServiceState.STARTING);
        Assert.assertEquals(ServiceState.RUNNING, wrapper.getState());

        wrapper.enableNotifications(false);
        wrapper.enablePolling(true);
        
        wrapper.setState(ServiceState.STOPPING);
        Assert.assertEquals(ServiceState.STOPPED, wrapper.getState());
    }
    
}
