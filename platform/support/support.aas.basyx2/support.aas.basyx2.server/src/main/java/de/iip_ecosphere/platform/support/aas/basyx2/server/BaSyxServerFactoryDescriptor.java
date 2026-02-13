/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2.server;

import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasFactory.AbstractServerFactoryDescriptor;
import de.iip_ecosphere.platform.support.aas.AasServerFactoryDescriptor;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.basyx2.common.PluginId;

/**
 * The server factory descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxServerFactoryDescriptor extends AbstractServerFactoryDescriptor 
    implements AasServerFactoryDescriptor {
    
    public static final String PLUGIN_ID = PluginId.PLUGIN_ID + AasFactory.POSTFIX_ID_SERVER;

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public Class<AasServerFactoryDescriptor> getType() {
        return AasServerFactoryDescriptor.class;
    }

    @Override
    public AasServerFactoryDescriptor createInstance() {
        return this;
    }

    @Override
    public DeploymentRecipe createDeploymentRecipe(SetupSpec spec) {
        return new BaSyxDeploymentRecipe(spec);
    }

    @Override
    public ServerRecipe createServerRecipe() {
        return new BaSyxServerRecipe();
    }

}
