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
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.configuration.ivml.AasIvmlMapper;
import de.iip_ecosphere.platform.configuration.ivml.AasIvmlMapper.AasChange;
import de.iip_ecosphere.platform.configuration.ivml.AbstractIvmlModifier.ConfigurationChangeListener;
import de.iip_ecosphere.platform.configuration.ivml.AbstractIvmlModifier.ConfigurationChangeType;
import de.iip_ecosphere.platform.configuration.serviceMesh.ServiceMeshGraphMapper;
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
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Realizes the AAS of the configuration component.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ConfigurationAas implements AasContributor, ConfigurationChangeListener, OperationCompletedListener {

    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_CONFIGURATION; 
    
    private transient List<AasChange> aasChanges = new ArrayList<>();

    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = AasPartRegistry.createSubmodelBuilderRbac(aasBuilder, NAME_SUBMODEL);
        AasIvmlMapper mapper = new AasIvmlMapper(() -> ConfigurationManager.getVilConfiguration(), 
            new ServiceMeshGraphMapper(), this);
        mapper.mapByType(smB, iCreator);
        mapper.addGraphFormat(new DrawflowGraphFormat());
        ConfigurationManager.setAasOperationCompletedListener(this);
        ConfigurationManager.setAasIvmlMapper(mapper);        
        smB.build();
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        ConfigurationManager.getAasIvmlMapper().bindOperations(sBuilder);
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
  
    /**
     * Clears all remaining AAS changes.
     */
    private void clearAasChanges() {
        aasChanges.clear();
    }

    @Override
    public void configurationChanged(IDecisionVariable var, ConfigurationChangeType type) {
        aasChanges.add(new AasChange(var, type));
    }

    @Override
    public void operationCompleted() {
        try {
            Aas aas = AasPartRegistry.retrieveIipAas();
            Submodel sm = aas.getSubmodel(NAME_SUBMODEL);
            SubmodelBuilder smB = AasPartRegistry.createSubmodelBuilderRbac(aas, NAME_SUBMODEL);
            for (AasChange c : aasChanges) {
                c.apply(ConfigurationManager.getAasIvmlMapper(), sm, smB);
            }
            smB.build();
        } catch (IOException e) {
            LoggerFactory.getLogger(ConfigurationAas.class).error(
                "While modifying configuration AAS: {}", e.getMessage());
        }
        clearAasChanges();
    }

    @Override
    public void operationFailed() {
        clearAasChanges();
    }

}
