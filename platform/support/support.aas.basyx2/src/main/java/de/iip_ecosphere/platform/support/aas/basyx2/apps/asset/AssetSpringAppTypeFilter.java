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

package de.iip_ecosphere.platform.support.aas.basyx2.apps.asset;

import de.iip_ecosphere.platform.support.aas.basyx2.apps.common.InclusionBasedTypeFilter;

import static de.iip_ecosphere.platform.support.aas.basyx2.apps.common.BaSyxNames.*;

/**
 * Exclusion filter for AAS repositories.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AssetSpringAppTypeFilter extends InclusionBasedTypeFilter {
    
    /**
     * Sets up the filtering for AAS repositories.
     */
    public AssetSpringAppTypeFilter() {
        addInclusion(BASYX_HTTP);
        addInclusion(AAS_REPOSITORY_CLIENT);
        addInclusion("org.eclipse.digitaltwin.basyx.serialization.");
        addInclusion("org.eclipse.digitaltwin.basyx.deserialization.");
    }
    
}