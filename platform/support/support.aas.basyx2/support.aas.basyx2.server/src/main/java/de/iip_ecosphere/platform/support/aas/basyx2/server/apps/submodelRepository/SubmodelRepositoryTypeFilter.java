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

package de.iip_ecosphere.platform.support.aas.basyx2.server.apps.submodelRepository;

import de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.InclusionBasedTypeFilter;

import static de.iip_ecosphere.platform.support.aas.basyx2.server.apps.common.BaSyxNames.*;

/**
 * Exclusion filter for submodel repositories.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SubmodelRepositoryTypeFilter extends InclusionBasedTypeFilter {

    /**
     * Sets up the filtering for submodel repositories.
     */
    public SubmodelRepositoryTypeFilter() {
        addInclusion(SM_REPOSITORY);
        addInclusion(SM_SERVICE);
        addInclusion(BASYX_HTTP);
        addInclusion("org.eclipse.digitaltwin.basyx.serialization.");
        addInclusion("org.eclipse.digitaltwin.basyx.deserialization.");
    }
    
}