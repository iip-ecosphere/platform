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

package de.iip_ecosphere.platform.support.iip_aas;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.setup.AbstractSetup;

/**
 * ECS runtime configuration (poor man's spring approach). Implementing components shall extend this class and add
 * their specific configuration settings. Subclasses must have a no-arg constructor and getters/setters for all
 * configuration values.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasBasedSetup extends AbstractSetup {
    
    private AasSetup aas = new AasSetup();
    
    /**
     * Returns the AAS setup.
     * 
     * @return the AAS setup
     */
    public AasSetup getAas() {
        return aas;
    }
    
    /**
     * Defines the AAS setup.
     * 
     * @param aas the AAS setup
     */
    public void setAas(AasSetup aas) {
        this.aas = aas;
    }

}
