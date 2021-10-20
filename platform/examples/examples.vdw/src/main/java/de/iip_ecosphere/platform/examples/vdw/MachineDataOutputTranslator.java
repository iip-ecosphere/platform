/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.vdw;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.examples.vdw.MachineData.BasicWoodworking;
import de.iip_ecosphere.platform.examples.vdw.MachineData.FullMachineToolDynamic;

/**
 * The machine data output translator for information-model based tests.
 * 
 * Plan: This class shall be generated from the configuration model.
 * 
 * @param <S> the source datatype
 * @author Holger Eichelberger, SSE
 */
public class MachineDataOutputTranslator<S> extends AbstractConnectorOutputTypeTranslator<S, MachineData> {
    
    private Class<? extends S> sourceType;
    
    /**
     * Creates instance.
     * 
     * @param sourceType the source type
     */
    public MachineDataOutputTranslator(Class<? extends S> sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public MachineData to(Object source) throws IOException {
        ModelAccess access = getModelAccess();
        final String sep = access.getQSeparator();
        
        // this is "constant" in the model; just for testing, we read it every time
        
        final String wwBasePath = access.iqName("Machines", "BasicWoodworking", "State", "Machine");
        final String wwOverviewPath = wwBasePath + sep + "Overview";
        BasicWoodworking ww = new BasicWoodworking(
            (int) access.get(wwOverviewPath + sep + "CurrentMode"), 
            (int) access.get(wwOverviewPath + sep + "CurrentState"));

        FullMachineToolDynamic fmt = null;
        // this is dynamic, may fail... subject to monitoring (see initializeModelAccess)
        try {
            final String fmtBasePath = getFmtProductionPath(access);
            fmt = new FullMachineToolDynamic((int) access.get(fmtBasePath + sep + "ActiveProgram" 
                + sep + "NumberInList"));            
//            fmt = new FullMachineToolDynamic((int) access.get(fmtBasePath + sep + "MyJob 1" + sep + "State" + sep 
//                + "Current State"));
        } catch (IOException e) {
        }
        
        return new MachineData(ww, fmt);
    }

    /**
     * Returns the FullMachineTooDynamic ProductionPlan path.
     * 
     * @param access the access instance
     * @return the path
     */
    private String getFmtProductionPath(ModelAccess access) {
        return access.iqName("Machines", "FullMachineToolDynamic", "Production");        
    }

    @Override
    public void initializeModelAccess() throws IOException {
        ModelAccess access = getModelAccess();
        access.useNotifications(true);
        access.monitorModelChanges(); // monitoring an item does not help here as it appears/disappears
    }

    @Override
    public Class<? extends S> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends MachineData> getTargetType() {
        return MachineData.class;
    }

}
