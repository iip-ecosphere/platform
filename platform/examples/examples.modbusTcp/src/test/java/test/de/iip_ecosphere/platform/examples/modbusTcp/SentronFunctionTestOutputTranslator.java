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
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelInputConverter;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;

/**
 * The  output translator for Sentron function test.
 * 
 * @param <S> the source datatype
 * 
 * @author Christian Nikolajew
 */
public class SentronFunctionTestOutputTranslator<S> 
    extends AbstractConnectorOutputTypeTranslator<S, SentronFunctionTest> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;
    
    /**
     * Creates a new SentronFunctionTestOutputTranslator.
     * 
     * @param withNotifications true = with Notifications ; false = without Notification
     * @param sourceType the source type
     */
    public SentronFunctionTestOutputTranslator(boolean withNotifications, Class<? extends S> sourceType) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType;
    }
    
    @Override
    public void initializeModelAccess() throws IOException {
        ModelAccess access = getModelAccess();
        access.useNotifications(withNotifications);
    }

    @Override
    public Class<? extends S> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends SentronFunctionTest> getTargetType() {
        return SentronFunctionTest.class;
    }

    @Override
    public SentronFunctionTest to(S source) throws IOException {
        
        AbstractModelAccess access = (AbstractModelAccess) getModelAccess(); 
        final ModelInputConverter inConverter = access.getInputConverter();
        SentronFunctionTest result = new SentronFunctionTest();
        
        result.setBetriebsstundenzaehler(inConverter.toInteger(access.get("Betriebsstundenzaehler")));
        result.setUniversalzaehler(inConverter.toInteger(access.get("Universalzaehler")));
        result.setImpulszaehler(inConverter.toInteger(access.get("Impulszaehler 0")));
        result.setSpannungL1L3((float) access.get("Spannung L1-L3"));
        result.setSpannungL2L3((float) access.get("Spannung L2-L3"));
        result.setSpannungL3L1((float) access.get("Spannung L3-L1"));
        
        return result;
    }

}
