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

package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.submodelRegistry;

import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.InclusionBasedTypeFilter;

import static de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.BaSyxNames.*;

/**
 * Exclusion filter for submodel registries.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SubmodelRegistryTypeFilter extends InclusionBasedTypeFilter {
    
    /**
     * Sets up the filtering for submodel registries.
     */
    public SubmodelRegistryTypeFilter() {
        addInclusion(SM_REGISTRY);
        addInclusion(SM_REGISTRY_SERVICE_API);
        addInclusion(SM_REGISTRY_SERVICE_STORAGE);
    }
    
}