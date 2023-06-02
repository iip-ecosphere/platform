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

package de.iip_ecosphere.platform.platform.cli;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.PlatformAasClient;
import de.iip_ecosphere.platform.support.iip_aas.PlatformClient;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;

/**
 * A factory for platform client instances. [testing]
 * 
 * @author Holger Eichelberger, SSE
 */
public interface PlatformClientFactory {

    /**
     * The default factory.
     */
    public static final PlatformClientFactory DEFAULT = new PlatformClientFactory() {
        
        @Override
        public PlatformClient create() throws IOException {
            return new PlatformAasClient();
        }
        
    };

    public static final PlatformClientFactory LOCAL = new PlatformClientFactory() {

        @Override
        public PlatformClient create() throws IOException {
            return new PlatformClient() {
                
                @Override
                public String snapshotAas(String id) throws ExecutionException {
                    try {
                        Aas aas = AasPartRegistry.retrieveIipAas();
                        File target = new File("platform.aasx");
                        AasFactory.getInstance().createPersistenceRecipe().writeTo(CollectionUtils.toList(aas), target);
                        return target.getAbsolutePath();
                    } catch (IOException e) {
                        throw new ExecutionException(e);
                    }
                }

                @Override
                public SemanticIdResolutionResult resolveSemanticId(String id) throws ExecutionException {
                    return SemanticIdResolver.resolve(id);
                }
                
            };
        }
        
    };
    
    /**
     * Creates a platform AAS client.
     * 
     * @return the client instance
     * @throws IOException if the client cannot be created
     */
    public PlatformClient create() throws IOException;

}
