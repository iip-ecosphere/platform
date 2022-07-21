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
public class AdminShellYamlSemanticIdResolverDescriptor implements SemanticIdResolverDescriptor {

    public static final String PUBLISHER = "ZVEI";
    public static final String KIND = "IRI";
    
    @Override
    public SemanticIdResolver createResolver() {
        return new YamlSemanticCatalog.YamlBasedResolver("AdminShell Yaml resolver", "adminShellCatalog.yml", 
            s -> s.startsWith("https://") || s.startsWith("http://"), 
            d -> {
                d.setPublisher(PUBLISHER);
                d.setKind(KIND);
                return d;
            }
        );
    }

}
