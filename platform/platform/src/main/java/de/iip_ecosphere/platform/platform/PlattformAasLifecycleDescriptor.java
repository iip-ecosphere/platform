/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.platform;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.EcsAas;
import de.iip_ecosphere.platform.ecsRuntime.EcsCmdLineLifecycleDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.EcsLifecycleDescriptor;
import de.iip_ecosphere.platform.services.ServicesAas;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.HeartbeatWatcher;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter;
import de.iip_ecosphere.platform.services.environment.services.TransportConverterFactory;
import de.iip_ecosphere.platform.support.LifecycleExclude;
import de.iip_ecosphere.platform.support.iip_aas.AbstractAasLifecycleDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.IipVersion;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.status.StatusMessage;
import de.iip_ecosphere.platform.transport.status.StatusMessageSerializer;

/**
 * This descriptor is responsible for creating the AAS of the platform.
 * 
 * @author Holger Eichelberger, SSE
 */
@LifecycleExclude({EcsCmdLineLifecycleDescriptor.class, EcsLifecycleDescriptor.class})
public class PlattformAasLifecycleDescriptor extends AbstractAasLifecycleDescriptor {

    private Timer timer = new Timer();
    private HeartbeatWatcher watcher = new HeartbeatWatcher();
    private TransportConverter<StatusMessage> traceConverter;
    
    /**
     * Creates AAS an instance for the service manager.
     */
    public PlattformAasLifecycleDescriptor() {
        super("Platform", () -> PlatformSetup.getInstance().getAas());
    }

    @Override
    public void startup(String[] args) {
        System.out.println("oktoflow Platform Server " + IipVersion.getInstance().getVersion() + ".");
        super.startup(args);
        ArtifactsManager.startWatching();

        PlatformSetup setup = PlatformSetup.getInstance();
        final int watcherTimeout = setup.getAasHeartbeatTimeout();
        if (watcherTimeout > 0) {
            try {
                watcher.setTimeout(watcherTimeout);
                watcher.installInto(Transport.createConnector());
                timer.schedule(new TimerTask() {
                    
                    @Override
                    public void run() {
                        watcher.deleteOutdated(devId -> {
                            LoggerFactory.getLogger(PlattformAasLifecycleDescriptor.class)
                                .info("Device {} outdated. Trying to delete AAS parts.", devId);
                            EcsAas.removeDevice(devId, 
                                sm -> ServicesAas.setCleanup(sm, devId), 
                                sm -> ServicesAas.removeDevice(sm, devId));
                        });
                    }
                }, watcherTimeout, watcherTimeout);
            } catch (IOException e) {
                LoggerFactory.getLogger(PlattformAasLifecycleDescriptor.class)
                    .error("Cannot install heartbeat watcher: {} Disabling heartbeat observation (partially).", 
                        e.getMessage());
            }
        }
        
        final long statusTimeout = setup.getAasStatusTimeout();
        traceConverter = TransportConverterFactory.getInstance().createConverter(setup.getAas(), setup.getTransport(), 
            StatusMessage.STATUS_STREAM, PlatformSetup.GATEWAY_PATH_STATUS, 
            StatusMessageSerializer.createTypeTranslator(), StatusMessage.class);
        traceConverter.setTimeout(statusTimeout);
        traceConverter.start(getAasSetup());
        /*timer.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    StatusConverter.INSTANCE.cleanup(AasPartRegistry.retrieveIipAas());
                } catch (IOException e) {
                    LoggerFactory.getLogger(PlattformAasLifecycleDescriptor.class)
                        .error("Cannot clean up status submodel: {}", e.getMessage());
                }
            }
            
        }, statusTimeout, statusTimeout);
        */
    }
    
    @Override
    public void shutdown() {
        //StatusConverter.INSTANCE.stop();
        traceConverter.stop();
        timer.cancel();
        try {
            watcher.uninstallFrom(Transport.getConnector());
        } catch (IOException e) {
            LoggerFactory.getLogger(PlattformAasLifecycleDescriptor.class)
                .error("Cannot uninstall heartbeat watcher: {}", e.getMessage());
        }
        ArtifactsManager.stopWatching();
        super.shutdown();
    }
    
    @Override
    protected boolean iipAasExists() {
        return true; // don't probe at this level, we build it up
    }
    
    @Override
    protected boolean enableAasHeartbeat() {
        return false; // we are the server, do not heartbeat and re-deploy to us
    }


}
