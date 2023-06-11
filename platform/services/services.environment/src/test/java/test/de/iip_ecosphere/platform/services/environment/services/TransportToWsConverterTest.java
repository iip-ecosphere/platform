/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.environment.services;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.services.TransportConverter;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter.Watcher;
import de.iip_ecosphere.platform.services.environment.services.TransportConverterFactory;
import de.iip_ecosphere.platform.services.environment.services.TransportToWsConverter;
import de.iip_ecosphere.platform.services.environment.services.WsTransportConverterFactory;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.TraceRecord;
import test.de.iip_ecosphere.platform.test.amqp.qpid.TestQpidServer;

/**
 * Tests {@link TransportToWsConverter}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportToWsConverterTest {

    /**
     * Tests {@link TransportToWsConverter}.
     */
    @Test
    public void testConverter() {
        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        Server qpid = new TestQpidServer(broker);
        qpid.start();

        AtomicInteger count = new AtomicInteger();
        TransportSetup transSetup = new TransportSetup();
        transSetup.setHost("localhost");
        transSetup.setPort(broker.getPort());
        transSetup.setAuthenticationKey("amqp"); 
        Transport.setTransportSetup(() -> transSetup);
        Endpoint converterEndpoint = new Endpoint(Schema.WS, Endpoint.LOCALHOST, NetUtils.getEphemeralPort(), 
            "/status");
        Server converterServer = TransportToWsConverter.createServer(converterEndpoint).start();
        TransportToWsConverter<TraceRecord> converter = new TransportToWsConverter<>(TraceRecord.TRACE_STREAM, 
            TraceRecord.class, converterEndpoint);
        converter.start(null);
        Assert.assertEquals(converterEndpoint, converter.getEndpoint());
        Watcher<TraceRecord> watcher = converter.createWatcher(0);
        watcher.setConsumer(t -> {
            count.incrementAndGet();
        });
        watcher.start();
                
        int[] img = new int[] {128, 128, 64, 12, 0, 8};
        TraceToAasServiceMain.MyData data = new TraceToAasServiceMain.MyData(img);
        Transport.sendTraceRecord(new TraceRecord("source", TraceRecord.ACTION_SENDING, data));
        TimeUtils.sleep(700);
        Transport.sendTraceRecord(new TraceRecord("rtsa", TraceRecord.ACTION_RECEIVING, data));
        TimeUtils.sleep(700); 
        Transport.sendTraceRecord(new TraceRecord("rtsa", TraceRecord.ACTION_SENDING, data));
        TimeUtils.sleep(1500);
        Transport.sendTraceRecord(new TraceRecord("receiver", TraceRecord.ACTION_RECEIVING, data));
        TimeUtils.sleep(700); 

        watcher.stop();
        converter.stop();
        converterServer.stop(true);
        
        Assert.assertEquals(4, count.get());
                
        qpid.stop(true);
        Transport.releaseConnector(false); // allow for reuse, next test
    }
    
    /**
     * Tests {@link WsTransportConverterFactory}.
     */
    @Test 
    public void testFactory() {
        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        Server qpid = new TestQpidServer(broker);
        qpid.start();
        Assert.assertNotNull(TransportConverterFactory.getInstance());

        AtomicInteger count = new AtomicInteger();
        TransportSetup transSetup = new TransportSetup();
        transSetup.setHost("localhost");
        transSetup.setPort(broker.getPort());
        transSetup.setAuthenticationKey("amqp"); 
        transSetup.setGatewayPort(NetUtils.getEphemeralPort());
        Transport.setTransportSetup(() -> transSetup);
        final String path = "/status";
        final AasSetup aas = null;
        final WsTransportConverterFactory factory = WsTransportConverterFactory.INSTANCE;
        Server converterServer = factory.createServer(aas, transSetup).start();
        TransportConverter<TraceRecord> converter = factory.createConverter(
            aas, transSetup, TraceRecord.TRACE_STREAM, path, null, TraceRecord.class);
        converter.start(aas);
        Assert.assertNotNull(converter.getEndpoint());
        Watcher<TraceRecord> watcher = factory.createWatcher(aas, transSetup, path, null, TraceRecord.class, 0);
        watcher.setConsumer(t -> {
            count.incrementAndGet();
        });
        watcher.start();
                
        int[] img = new int[] {128, 128, 64, 12, 0, 8};
        TraceToAasServiceMain.MyData data = new TraceToAasServiceMain.MyData(img);
        Transport.sendTraceRecord(new TraceRecord("source", TraceRecord.ACTION_SENDING, data));
        TimeUtils.sleep(700);
        Transport.sendTraceRecord(new TraceRecord("rtsa", TraceRecord.ACTION_RECEIVING, data));
        TimeUtils.sleep(700); 
        Transport.sendTraceRecord(new TraceRecord("rtsa", TraceRecord.ACTION_SENDING, data));
        TimeUtils.sleep(1500);
        Transport.sendTraceRecord(new TraceRecord("receiver", TraceRecord.ACTION_RECEIVING, data));
        TimeUtils.sleep(700); 

        watcher.stop();
        converter.stop();
        converterServer.stop(true);
        
        Assert.assertEquals(4, count.get());
                
        qpid.stop(true);
        Transport.releaseConnector(false); // allow for reuse, next test
    }

}
