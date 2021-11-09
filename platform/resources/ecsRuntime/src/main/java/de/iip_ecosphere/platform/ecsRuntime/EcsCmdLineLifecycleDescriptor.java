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

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.LifecycleDescriptor;

/**
 * Handle onboarding/offboarding command line arguments.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsCmdLineLifecycleDescriptor implements LifecycleDescriptor {

    @Override
    public void startup(String[] args) {
        if (args.length == 1) {
            String cmd = args[0].toLowerCase();
            try {
                if ("onboard".equals(cmd)) {
                    DeviceManagement.addDevice(true);
                    System.exit(0);
                } else if ("offboard".equals(cmd)) {
                    DeviceManagement.removeDevice(true);
                    System.exit(0);
                }
            } catch (ExecutionException e) {
                LoggerFactory.getLogger(EcsCmdLineLifecycleDescriptor.class).error(e.getMessage());
            }
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Thread getShutdownHook() {
        return null;
    }

    @Override
    public int priority() {
        return CMD_LINE_PRIORITY;
    }

}
