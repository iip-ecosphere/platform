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

package de.iip_ecosphere.platform.support.aas.basyx2.server;

import java.util.List;

import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.ServerRecipe.LocalPersistenceType;
import de.iip_ecosphere.platform.support.aas.basyx2.server.BaSyxAbstractAasServer.ServerType;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.function.IORunnable;

/**
 * An initial BaSyx-specific deployment builder.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxDeploymentRecipe extends de.iip_ecosphere.platform.support.aas.basyx2.common.BaSyxDeploymentRecipe {

    /**
     * Creates a deployment builder with root/empty document base path.
     * 
     * @param spec the setup specification
     */
    BaSyxDeploymentRecipe(SetupSpec spec) {
        super(spec);
    }
    
    @Override
    protected AasServer createImmediateDeploymentServer(SetupSpec spec, List<IORunnable> actions, String... options) {
        return new BaSyxLocalServer(spec, ServerType.COMBINED, LocalPersistenceType.INMEMORY, options)
            .addActionsAfterStart(actions);
    }
    
    @Override
    protected AasServer createRegistryDeploymentServer(SetupSpec spec, String... options) {
        return new BaSyxLocalServer(spec, ServerType.COMBINED, LocalPersistenceType.INMEMORY, options);
    }    

}
