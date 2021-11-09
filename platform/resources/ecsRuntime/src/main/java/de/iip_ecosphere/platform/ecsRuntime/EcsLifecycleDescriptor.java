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

package de.iip_ecosphere.platform.ecsRuntime;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.PidLifecycleDescriptor;
import de.iip_ecosphere.platform.support.TerminatingLifecycleDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.AbstractAasLifecycleDescriptor;
import de.iip_ecosphere.platform.support.net.NetworkManagerFactory;

/**
 * The basic ECS lifecycle descriptor for powering up the AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsLifecycleDescriptor extends AbstractAasLifecycleDescriptor implements PidLifecycleDescriptor, 
    TerminatingLifecycleDescriptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(EcsLifecycleDescriptor.class);
    private boolean registered = false;
    private boolean continueWaiting = true;

    /**
     * Creates an instance for the service manager.
     */
    public EcsLifecycleDescriptor() {
        super("ECS", () -> EcsFactory.getSetup().getAas());
    }

    @Override
    public void startup(String[] args) {
        System.out.println("IIP-Ecosphere ECS Runtime.");
        super.startup(args);
        EcsSetup setup = EcsFactory.getSetup();
        NetworkManagerFactory.configure(setup.getNetMgr());
        Monitor.startScheduling();
        boolean autoOnOff = setup.getAutoOnOffboarding();
        try {
            DeviceManagement.addDevice(autoOnOff ? true : false);
            registered = true;
        }  catch (ExecutionException e) {
            if (!autoOnOff) { // graceful if auto
                continueWaiting = false;
                LOGGER.error(e.getMessage());
            }
        } 
    }
    
    @Override
    public void shutdown() {
        Monitor.stopScheduling();
        EcsAas.notifyResourceRemoved();
        EcsSetup setup = EcsFactory.getSetup();
        boolean autoOnOff = setup.getAutoOnOffboarding();
        if (registered) {
            try {
                DeviceManagement.removeDevice(autoOnOff ? true : false);
            } catch (ExecutionException e) {
                if (!autoOnOff) { // graceful if auto
                    LOGGER.error(e.getMessage());
                }
            }
        }
        super.shutdown();
    }
    
    @Override
    public String getPidFileName() {
        return "iip-ecsRuntime.pid";
    }

    @Override
    public boolean continueWaiting() {
        return continueWaiting;
    }
    
}
