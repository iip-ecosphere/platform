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

package de.iip_ecosphere.platform.support.semanticId.eclass;

import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolverDescriptor;

/**
 * JSL descriptor for {@link EclassSemanticIdResolver}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EclassSemanticIdResolverDescriptor implements SemanticIdResolverDescriptor {

    @Override
    public SemanticIdResolver createResolver() {
        return new EclassSemanticIdResolver();
    }

}
