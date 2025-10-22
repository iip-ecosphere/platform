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

package de.iip_ecosphere.platform.configuration.easyProducer;

import de.iip_ecosphere.platform.configuration.easyProducer.ivml.AasIvmlMapper;
import de.iip_ecosphere.platform.configuration.easyProducer.serviceMesh.ServiceMeshGraphMapper;
import de.iip_ecosphere.platform.configuration.cfg.ConfigurationChangeType;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.AasChange;
import de.iip_ecosphere.platform.configuration.easyProducer.ivml.AbstractIvmlModifier.ConfigurationChangeListener;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper.OperationCompletedListener;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * The AAS changes that also act as configuration change listener.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasChanges extends de.iip_ecosphere.platform.configuration.cfg.AasChanges 
    implements ConfigurationChangeListener {

    static final AasChanges INSTANCE = new AasChanges();
    
    /**
     * Creates an instances, prevents external creation.
     */
    protected AasChanges() {
    }

    @Override
    public void setup(SubmodelBuilder smBuilder, InvocablesCreator iCreator, 
        OperationCompletedListener completedListener) {
        AasIvmlMapper mapper = new AasIvmlMapper(() -> ConfigurationManager.getVilConfiguration(), 
            new ServiceMeshGraphMapper(), this);
        mapper.mapByType(smBuilder, iCreator);
        mapper.addGraphFormat(new DrawflowGraphFormat());
        ConfigurationManager.setAasOperationCompletedListener(completedListener);
        ConfigurationManager.setAasIvmlMapper(mapper);        
    }

    @Override
    public void bindOperations(ProtocolServerBuilder sBuilder) {
        ConfigurationManager.getAasIvmlMapper().bindOperations(sBuilder);
    }
    
    @Override
    public void configurationChanged(IDecisionVariable var, ConfigurationChangeType type) {
        addAasChange(new AasChange(var, type));
    }
    
}
