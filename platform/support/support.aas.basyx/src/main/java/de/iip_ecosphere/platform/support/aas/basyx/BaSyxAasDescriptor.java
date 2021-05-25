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

package de.iip_ecosphere.platform.support.aas.basyx;

import org.eclipse.basyx.aas.metamodel.map.descriptor.AASDescriptor;
import org.eclipse.basyx.aas.restapi.VABMultiSubmodelProvider;

/**
 * An internal AAS deployment descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
class BaSyxAasDescriptor {
    
    private AASDescriptor aasDescriptor;
    private VABMultiSubmodelProvider fullProvider;
    
    /**
     * Creates an instance.
     * 
     * @param fullProvider the sub-model provider
     * @param aasDescriptor the AAS descriptor
     */
    BaSyxAasDescriptor(VABMultiSubmodelProvider fullProvider, AASDescriptor aasDescriptor) {
        this.fullProvider = fullProvider;
        this.aasDescriptor = aasDescriptor;
    }

    /**
     * Returns the BaSyx AAS descriptor.
     * 
     * @return the BaSyx AAS descriptor
     */
    AASDescriptor getAasDescriptor() {
        return aasDescriptor;
    }

    /**
     * Returns the sub-model provider.
     * 
     * @return the sub-model provider
     */
    VABMultiSubmodelProvider getFullProvider() {
        return fullProvider;
    }
}