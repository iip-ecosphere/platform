/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration;

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Represents an AAS change.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AasChange {
    
    private ConfigurationChangeType type;

    /**
     * Creates an instance.
     * 
     * @param type the change type
     */
    public AasChange(ConfigurationChangeType type) {
        this.type = type;
    }
    
    /**
     * Returns the change type.
     * 
     * @return the change type
     */
    public ConfigurationChangeType getType() {
        return type;
    }
    
    /**
     * Applies the change.
     * 
     * @param sm the submodel containing the configuration
     * @param smB the submodel builder of {@code sm} for modifications
     */
    public abstract void apply(Submodel sm, SubmodelBuilder smB);
    
}
