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

import java.io.IOException;

import de.iip_ecosphere.platform.configuration.cfg.AasChange;
import de.iip_ecosphere.platform.configuration.cfg.ConfigurationFactory;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper.OperationCompletedListener;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Realizes the AAS of the configuration component.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationAas implements AasContributor, OperationCompletedListener {

    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_CONFIGURATION; 

    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = AasPartRegistry.createSubmodelBuilderRbac(aasBuilder, NAME_SUBMODEL);
        ConfigurationFactory.getAasChanges().setup(smB, iCreator, this);
        smB.build();
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        ConfigurationFactory.getAasChanges().bindOperations(sBuilder);
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }
    
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void operationCompleted() {
        try {
            Aas aas = AasPartRegistry.retrieveIipAas();
            Submodel sm = aas.getSubmodel(NAME_SUBMODEL);
            SubmodelBuilder smB = AasPartRegistry.createSubmodelBuilderRbac(aas, NAME_SUBMODEL);
            for (AasChange c : ConfigurationFactory.getAasChanges().getAndClearAasChanges()) {
                c.apply(sm, smB);
            }
            smB.build();
        } catch (IOException e) {
            LoggerFactory.getLogger(ConfigurationAas.class).error(
                "While modifying configuration AAS: {}", e.getMessage());
        }
    }

    @Override
    public void operationFailed() {
        ConfigurationFactory.getAasChanges().clearAasChanges();
    }

}
