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

package de.iip_ecosphere.platform.support.aas.basyx2.apps.aasRegistry;

import de.iip_ecosphere.platform.support.aas.basyx2.apps.common.InclusionBasedTypeFilter;

import static de.iip_ecosphere.platform.support.aas.basyx2.apps.common.BaSyxNames.*;

/**
 * Exclusion filter for AAS registries.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasRegistryTypeFilter extends InclusionBasedTypeFilter {
    
    /**
     * Sets up the filtering for AAS registries.
     */
    public AasRegistryTypeFilter() {
        addInclusion(AAS_REGISTRY_SERVICE_API);
        addInclusion(AAS_REGISTRY_SERVICE_CFG); // seem to be enough, no further from standalone POM
    }
    
}