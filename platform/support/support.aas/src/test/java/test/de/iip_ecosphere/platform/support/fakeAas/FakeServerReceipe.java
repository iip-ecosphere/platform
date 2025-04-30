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

package test.de.iip_ecosphere.platform.support.fakeAas;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.CorsEnabledRecipe;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.SetupSpec;

/**
 * Implements a fake server recipe.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeServerReceipe implements ServerRecipe {

    @Override
    public AasServer createAasServer(SetupSpec spec, PersistenceType persistence, String... options) {
        return null;
    }

    @Override
    public Server createRegistryServer(SetupSpec spec, PersistenceType persistence, String... options) {
        return null;
    }

    @Override
    public PersistenceType toPersistenceType(String type) {
        return LocalPersistenceType.INMEMORY;
    }

    @Override
    public CorsEnabledRecipe setAccessControlAllowOrigin(String accessControlAllowOrigin) {
        return null;
    }

}
