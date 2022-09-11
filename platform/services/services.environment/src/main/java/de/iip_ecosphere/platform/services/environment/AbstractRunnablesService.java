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

package de.iip_ecosphere.platform.services.environment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.services.environment.AbstractProcessService.RunnableWithStop;

/**
 * A basic service that holds and stops runnables.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractRunnablesService extends AbstractService {

    private List<RunnableWithStop> runnables = new ArrayList<>();

    /**
     * Creates a service from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public AbstractRunnablesService(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }
    
    /**
     * Creates a service from YAML information.
     * 
     * @param yaml the service information as read from YAML. By default, the Python executable is 
     * "ServiceEnvironment.py", which can be overridden by {@link YamlProcess#getExecutable()}. 
     * {@link YamlProcess#getHomePath()} is set to the home path where the 
     * executable was extracted to. Further, {@link YamlProcess#getCmdArg()} are taken over if given.
     */
    public AbstractRunnablesService(YamlService yaml) {
        super(yaml);
    }
    
    /**
     * Registers a runnable that shall be stopped in {@link #stop()}.
     * 
     * @param runnable the runnable, ignored if <b>null</b>
     */
    protected void register(RunnableWithStop runnable) {
        if (null != runnable) {
            runnables.add(runnable);
        }
    }

    @Override
    protected ServiceState stop() {
        for (RunnableWithStop r : runnables) {
            r.stop();
        }
        runnables.clear();
        return super.stop();
    }

}
