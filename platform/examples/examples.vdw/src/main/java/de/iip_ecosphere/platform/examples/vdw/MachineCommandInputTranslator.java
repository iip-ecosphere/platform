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

import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

/**
 * The machine command input translator for information-model based tests.
 *
 * Plan: This class shall be generated from the configuration model.
 * 
 * @param <O> the output datatype
 * @author Holger Eichelberger, SSE
 */
public class MachineCommandInputTranslator<O> extends AbstractConnectorInputTypeTranslator<MachineCommand, O> {

    private Class<? extends O> sourceType;
    
    /**
     * Creates a new machine command input translator.
     * 
     * @param sourceType the source type
     */
    public MachineCommandInputTranslator(Class<? extends O> sourceType) {
        this.sourceType = sourceType;
    }
    
    @Override
    public O from(MachineCommand data) throws IOException {
        //ModelAccess access = getModelAccess();
        // generated code with "semantic" from configuration model
        return null; // irrelevant
    }

    @Override
    public Class<? extends O> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends MachineCommand> getTargetType() {
        return MachineCommand.class;
    }
    
}
