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

package de.iip_ecosphere.platform.configuration;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;

/**
 * Realizes the AAS of the configuration component.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationAas implements AasContributor {

    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }
    
    @Override
    public boolean isValid() {
        return true;
    }

}
