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
import test.de.iip_ecosphere.platform.connectors.IdentityInputTranslator;
import test.de.iip_ecosphere.platform.connectors.IdentityOutputTranslator;
import de.iip_ecosphere.platform.support.Version;

import java.util.HashMap;
import java.util.Map;
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
        testStopWrapper(wrapper);
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
        testStopWrapper(wrapper);
        
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
     * The wrapper is started but not stopped.
     * 
     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @param wrapper the wrapper instance to be tested
     * @param yaml the service deployment information
     * 
     * @throws ExecutionException shall not occur in successful tests#
     * @see #testStopWrapper(ConnectorServiceWrapper)
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
        
        Assert.assertEquals("myPathOut", wrapper.getOutPath("myPathOut"));
        Assert.assertEquals("myPathIn", wrapper.getInPath("myPathIn"));
        Map<String, String> values = new HashMap<>();
        values.put("inPath", "/pi/1/");
        values.put("outPath", "/po/2/");
        wrapper.reconfigure(values);
        Assert.assertEquals("/po/2/", wrapper.getOutPath("myPathOut"));
        Assert.assertEquals("/pi/1/", wrapper.getInPath("myPathIn"));
    }

    /**
     * Generic stopping test for a wrapper instance.
     * 
     * @param <O> the output type from the underlying machine/platform
     * @param <I> the input type to the underlying machine/platform
     * @param <CO> the output type of the connector
     * @param <CI> the input type of the connector
     * @param wrapper the wrapper instance to be tested
     * 
     * @throws ExecutionException shall not occur in successful tests#
     */
    private <O, I, CO, CI> void testStopWrapper(ConnectorServiceWrapper<O, I, CO, CI> wrapper) 
            throws ExecutionException {
        wrapper.setState(ServiceState.STOPPING);
        Assert.assertEquals(ServiceState.STOPPED, wrapper.getState());
    }

}
