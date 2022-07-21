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

package de.iip_ecosphere.platform.support.iip_aas;

import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolverDescriptor;

/**
 * Default eclass resolver for built-in definitions. (Re)Assigns {@link SemanticIdResolutionResult#getKind()} and
 * {@link SemanticIdResolutionResult#getPublisher()} with default values. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class EclassYamlSemanticIdResolverDescriptor implements SemanticIdResolverDescriptor {

    public static final String PUBLISHER = "ECLASS";
    public static final String KIND = "IRDI";
    
    @Override
    public SemanticIdResolver createResolver() {
        // TODO predicate shall be IRDI-structure regex
        return new YamlSemanticCatalog.YamlBasedResolver("Eclass Yaml resolver", "eclassCatalog.yml", s -> true, d -> {
            d.setPublisher(PUBLISHER);
            d.setKind(KIND);
            return d;
        });
    }

}
