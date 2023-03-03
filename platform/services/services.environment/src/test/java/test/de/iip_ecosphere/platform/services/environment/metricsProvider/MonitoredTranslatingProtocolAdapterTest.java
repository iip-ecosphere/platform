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

package test.de.iip_ecosphere.platform.services.environment.metricsProvider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ConnectorInputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.ConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.services.environment.metricsProvider.MonitoredTranslatingProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

/**
 * Tests {@link MonitoredTranslatingProtocolAdapter}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MonitoredTranslatingProtocolAdapterTest {
    
    /**
     * Represents some data.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ConnectorData {
        private String data;
    }

    /**
     * A test output translator that does a 100 ms translation.
     */
    private static class ConnectorOutTranslator implements ConnectorOutputTypeTranslator<String, ConnectorData> {

        @Override
        public ConnectorData to(String source) throws IOException {
            TimeUtils.sleep(100);
            ConnectorData result = new ConnectorData(); 
            result.data = source;
            return result;
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
        public Class<? extends String> getSourceType() {
            return String.class;
        }

        @Override
        public Class<? extends ConnectorData> getTargetType() {
            return ConnectorData.class;
        }
        
    }

    /**
     * A test input translator that does a 150 ms translation.
     */
    private static class ConnectorInTranslator implements ConnectorInputTypeTranslator<ConnectorData, String> {

        @Override
        public String from(ConnectorData data) throws IOException {
            TimeUtils.sleep(150);
            return data.data;
        }

        @Override
        public ModelAccess getModelAccess() {
            return null;
        }

        @Override
        public void setModelAccess(ModelAccess modelAccess) {
        }

        @Override
        public Class<? extends String> getSourceType() {
            return String.class;
        }

        @Override
        public Class<? extends ConnectorData> getTargetType() {
            return ConnectorData.class;
        }
        
    }

    /**
     * Tests the adapter and roughly the measured values.
     */
    @Test
    public void testAdapterWithLog() {
        File log = new File(FileUtils.getTempDirectory(), "monTest.log");
        log.deleteOnExit();
        testAdapter(log);
    }

    /**
     * Tests the adapter and roughly the measured values.
     */
    @Test
    public void testAdapterWithoutLog() {
        testAdapter(null);
    }

    /**
     * Performs the actual adapter test.
     * 
     * @param log the expected log file, may be <b>null</b> for none
     */
    private static void testAdapter(File log) {
        SimpleMeterRegistry reg = new SimpleMeterRegistry();
        MetricsProvider metrics = new MetricsProvider(reg);
        MonitoredTranslatingProtocolAdapter<String, String, ConnectorData, ConnectorData> adapter 
            = new MonitoredTranslatingProtocolAdapter<>(new ConnectorOutTranslator(), 
                new ConnectorInTranslator(), metrics, log);
        
        final int max = 10;
        for (int i = 0; i < max; i++) {
            try {
                adapter.adaptInput(adapter.adaptOutput("", "test"));
            } catch (IOException e) {
            }
        }
        
        Timer iTimer = reg.get(MonitoredTranslatingProtocolAdapter.ADAPT_INPUT_TIME).timer();
        double tmp = iTimer.mean(TimeUnit.MILLISECONDS);
        Assert.assertTrue(140 <= tmp && tmp <= 200);
        Assert.assertEquals(max, iTimer.count());

        Timer oTimer = reg.get(MonitoredTranslatingProtocolAdapter.ADAPT_OUTPUT_TIME).timer();
        tmp = oTimer.mean(TimeUnit.MILLISECONDS);
        Assert.assertTrue(90 <= tmp && tmp <= 150);
        Assert.assertEquals(max, oTimer.count());
        
        if (null != log) {
            Assert.assertTrue(log.exists());
            Assert.assertTrue(log.length() > 0);
            try {   
                System.out.println("LOG:");    
                System.out.println(FileUtils.readFileToString(log, "UTF-8"));
            } catch (IOException t) {
                t.printStackTrace();
            }
            FileUtils.deleteQuietly(log);
        }
    }

}
