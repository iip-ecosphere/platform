/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.examples.modbusTcp;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

/**
 * The  input translator for Sentron function test.
 * 
 * @param <O> the output datatype
 * 
 * @author Christian Nikolajew
 */
public class SentronFunctionTestInputTranslator<O> 
    extends AbstractConnectorInputTypeTranslator<SentronFunctionTestRw, O> {

    private Class<? extends O> sourceType;
    
    /**
     * Creates a new SentronFunctionTestInputTranslator.
     * 
     * @param sourceType the source type
     */
    public SentronFunctionTestInputTranslator(Class<? extends O> sourceType) {
        this.sourceType = sourceType;
    }
    
    @Override
    public Class<? extends O> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends SentronFunctionTestRw> getTargetType() {
        return SentronFunctionTestRw.class;
    }

    @Override
    public O from(SentronFunctionTestRw data) throws IOException {
        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();

        if (data.getBetriebsstundenzaehler() != null) {
            access.set("Betriebsstundenzaehler", data.getBetriebsstundenzaehler());
        }
        
        if (data.getUniversalzaehler() != null) {
            access.set("Universalzaehler", data.getUniversalzaehler());
        }
        
        if (data.getImpulszaehler() != null) {
            access.set("Impulszaehler 0", data.getImpulszaehler());
        }

        return null;
    }

}
