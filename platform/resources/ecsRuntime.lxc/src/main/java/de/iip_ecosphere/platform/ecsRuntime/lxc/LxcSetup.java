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

package de.iip_ecosphere.platform.ecsRuntime.lxc;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.EcsSetup;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Implements the LXC specific configuration. For configuration prerequisites, see {@link EcsSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LxcSetup extends EcsSetup {

    private Lxc lxc = new Lxc();

    /**
     * Returns the LXC setup.
     * 
     * @return LXC setup
     */
    public Lxc getLxc() {
        return lxc;
    }
    
    /**
     * Defines LXC setup.
     * 
     * @param lxc the setup instance
     */
    public void setLxc(Lxc lxc) {
        this.lxc = lxc;
    }
    
    /**
    * Reads a {@link LxcSetup} instance from a {@link AbstractSetup#DEFAULT_FNAME} in the 
    * root folder of the jar/classpath. 
    *
    * @return configuration instance
    */
    public static LxcSetup readFromYaml() {
        LxcSetup result;
        try {
            return EcsSetup.readConfiguration(LxcSetup.class);
        } catch (IOException e) {
            LoggerFactory.getLogger(LxcSetup.class).error("Reading configuration: " + e.getMessage());
            result = new LxcSetup();
        }
        return result;
    }
    
}
