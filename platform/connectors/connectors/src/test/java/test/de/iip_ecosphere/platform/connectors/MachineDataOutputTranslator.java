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

package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;

import org.junit.Assert;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import test.de.iip_ecosphere.platform.support.aas.AasTest;

/**
 * The machine data output translator for information-model based tests.
 * 
 * @param <S> the source datatype
 * @author Holger Eichelberger, SSE
 */
public class MachineDataOutputTranslator<S> extends AbstractConnectorOutputTypeTranslator<S, MachineData> {
    
    private boolean withNotifications;
    private Class<? extends S> sourceType;
    private OutputCustomizer customizer;
    
    /**
     * A plugin to this translator to customize the translator towards specific capabilities of the test/connector.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface OutputCustomizer {

        /**
         * Called in {@link MachineDataOutputTranslator#initializeModelAccess()}.
         * 
         * @param access the information model access object
         * @param withNotifications operate with/without notifications (for testing)
         * @throws IOException in case that model access problems occur
         */
        public void initializeModelAccess(ModelAccess access, boolean withNotifications) throws IOException;

        /**
         * Returns the machine vendor. This may be done directly from the information model or eg. through a 
         * structure if supported.
         * 
         * @param access the information model access object
         * @return the name of the vendor
         * @throws IOException in case that model access problems occur
         */
        public String getVendor(ModelAccess access) throws IOException;

        /**
         * Returns a valid top-level model part name.
         * 
         * @return the name of a valid top-level model part
         */
        public String getTopLevelModelPartName();
        
        /**
         * Returns the qualified model name for accessing the machine's lot size.
         * 
         * @return the lot size qualified name
         */
        public String getQNameVarLotSize();

        /**
         * Returns the qualified model name for accessing the machine's power consumption.
         * 
         * @return the power consumption qualified name
         */
        public String getQNameVarPowerConsumption();

    }
    
    /**
     * Creates instance.
     * 
     * @param withNotifications operate with/without notifications (for testing)
     * @param sourceType the source type
     * @param customizer the translator customizer
     */
    public MachineDataOutputTranslator(boolean withNotifications, Class<? extends S> sourceType, 
        OutputCustomizer customizer) {
        this.withNotifications = withNotifications;
        this.sourceType = sourceType;
        this.customizer = customizer;
    }

    @Override
    public MachineData to(Object source) throws IOException {
        ModelAccess access = getModelAccess();
        try {
            access.get("abxy"); // no submodel
            Assert.fail("Property shall not exist");
        } catch (IOException e) {
            // expected
        }
        try {
            access.get("ab" + access.getQSeparator() + "abxy"); // submodel/property does not exist
            Assert.fail("Property shall not exist");
        } catch (IOException e) {
            // expected
        }
        try {
            access.get(AasTest.NAME_SUBMODEL + access.getQSeparator() + "abxy"); // property does not exist
            Assert.fail("Property shall not exist");
        } catch (IOException e) {
            // expected
        }
        String vendor = customizer.getVendor(access);
        return new MachineData(
            (int) access.get(customizer.getQNameVarLotSize()), 
            (double) access.get(customizer.getQNameVarPowerConsumption()), 
            vendor);
    }

    @Override
    public void initializeModelAccess() throws IOException {
        ModelAccess access = getModelAccess();
        access.useNotifications(withNotifications);
        customizer.initializeModelAccess(access, withNotifications);
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
