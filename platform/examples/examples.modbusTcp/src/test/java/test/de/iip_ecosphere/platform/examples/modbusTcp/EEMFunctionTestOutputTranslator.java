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
 * The  output translator for EEM function test.
 * 
 * @param <S> the source datatype
 * 
 * @author Christian Nikolajew
 */
public class EEMFunctionTestOutputTranslator<S> extends AbstractConnectorOutputTypeTranslator<S, EEMFunctionTest> {

    private boolean withNotifications;
    private Class<? extends S> sourceType;
    
    /**
     * Creates a new EEMFunctionTestOutputTranslator.
     * 
     * @param withNotifications true = with Notifications ; false = without Notification
     * @param sourceType the source type
     */
    public EEMFunctionTestOutputTranslator(boolean withNotifications, Class<? extends S> sourceType) {
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
    public Class<? extends EEMFunctionTest> getTargetType() {
        return EEMFunctionTest.class;
    }

    @Override
    public EEMFunctionTest to(S source) throws IOException {
        
        AbstractModelAccess access = (AbstractModelAccess) getModelAccess();  
        final ModelInputConverter inConverter = access.getInputConverter();
        EEMFunctionTest result = new EEMFunctionTest();
        
        result.setDay(inConverter.toInteger(access.get("Day")));
        result.setMonth(inConverter.toInteger(access.get("Month")));
        result.setYear(inConverter.toInteger(access.get("Year")));
        result.setU12((float) access.get("U12"));
        result.setU23((float) access.get("U23"));
        result.setU31((float) access.get("U31"));
        
        return result;
    }

}
