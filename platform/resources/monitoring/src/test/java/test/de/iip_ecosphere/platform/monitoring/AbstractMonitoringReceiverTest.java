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

package test.de.iip_ecosphere.platform.monitoring;

import java.util.Timer;
import java.util.TimerTask;

import de.iip_ecosphere.platform.ecsRuntime.Monitor;
import de.iip_ecosphere.platform.monitoring.MonitoringReceiver;
import de.iip_ecosphere.platform.monitoring.MonitoringSetup;
import de.iip_ecosphere.platform.services.environment.spring.metricsProvider.MetricsProvider;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/**
 * Tests a {@link MonitoringReceiver} with a simple mocked reusable example scenario.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractMonitoringReceiverTest {
    
    /**
     * Encapsulates the monitoring receiver.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected interface MonitoringRecieverLifecycle {

        /**
         * Starts the receiver. May also create it.
         * 
         * @param transSetup the transport setup
         */
        public void start(TransportSetup transSetup);        
        
        /**
         * Stops the receiver.
         */
        public void stop();
    }
    
    /**
     * Creates the broker instance.
     * 
     * @param broker the broker address
     * @return the instance
     */
    protected abstract Server createBroker(ServerAddress broker);
    
    /**
     * Runs a platform-like monitoring scenario with an ECS-runtime and a service manager on the same device.
     * 
     * @param mrl the lifecylce
     */
    protected void runScenario(MonitoringRecieverLifecycle mrl) {
        System.out.println("Starting broker"); // Qpid eats up logging info
        ServerAddress broker = new ServerAddress(Schema.IGNORE);
        Server qpid = createBroker(broker);
        qpid.start();
        
        TransportSetup transSetup = new TransportSetup();
        transSetup.setHost("localhost");
        transSetup.setPort(broker.getPort());
        transSetup.setUser("user"); // preliminary in here
        transSetup.setPassword("pwd");
        MonitoringSetup.getInstance().setTransport(transSetup);

        // for status
        Transport.setTransportSetup(() -> transSetup);

        // a part of ECS runtime
        Monitor.setTransportSetup(() -> transSetup);

        System.out.println("Creating/starting receiver");
        mrl.start(transSetup);
        
        // part of onboarding
        System.out.println("Onboarding device");
        Monitor.sendResourceStatus(ActionTypes.ADDED);
        Monitor.startScheduling();
        
        System.out.println("Device onboarded");

        System.out.println("Starting service monitoring");
        MetricsProvider mProvider = new MetricsProvider(new SimpleMeterRegistry()); // as in services.environment
        mProvider.setInjectedValues(transSetup);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            
            @Override
            public void run() {
                mProvider.calculateMetrics();
            }
        }, 0, 1000);
        
        // a bit from generated code
        String serviceId = "aiService";
        Transport.sendServiceStatus(ActionTypes.ADDED, serviceId);
        mProvider.increaseCounter("service.aiService.sent");
        mProvider.increaseCounter("service.aiService.received");
        mProvider.increaseCounter("service.aiService.received");
        Transport.sendServiceStatus(ActionTypes.REMOVED, serviceId);

        System.out.println("Sleeping...");
        TimeUtils.sleep(6000);

        // part of offboarding
        System.out.println("Device offboarding");
        Monitor.sendResourceStatus(ActionTypes.REMOVED);

        System.out.println("Stopping");
        timer.cancel();
        mrl.stop();
        mProvider.destroy();
        Monitor.stopScheduling();
        Transport.releaseConnector();
        qpid.stop(true);

        Transport.setTransportSetup(null);
        Monitor.setTransportSetup(null);
    }

}
