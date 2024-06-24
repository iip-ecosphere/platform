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
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

/**
 * The machine command input translator for information-model based tests.
 * 
 * @param <O> the output datatype
 * @author Holger Eichelberger, SSE
 */
public class MachineCommandInputTranslator<O> extends AbstractConnectorInputTypeTranslator<MachineCommand, O> {

    private Class<? extends O> sourceType;
    private InputCustomizer customizer;

    /**
     * Customizer of this input translator.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface InputCustomizer {

        /**
         * Returns the qualified name of the command operation to start the machine.
         * 
         * @return the qualified name
         */
        public String getQNameOperationStartMachine();

        /**
         * Returns the qualified name of the command operation to stop the machine.
         * 
         * @return the qualified name
         */
        public String getQNameOperationStopMachine();
        
        /**
         * Returns the qualified name of the command operation to access the lot size.
         * 
         * @return the qualified name
         */
        public String getQNameVarLotSize();
        
        /**
         * Returns a valid top-level model part name.
         * 
         * @return the name of a valid top-level model part
         */
        public String getTopLevelModelPartName();

        /**
         * Performs additional actions during {@link MachineCommandInputTranslator#from(MachineCommand)}, e.g., 
         * to access structures in the information model.
         * 
         * @param access the model access instance
         * @param data the data object being handled
         * @throws IOException if model access fails
         */
        public void additionalFromActions(ModelAccess access, MachineCommand data) throws IOException;
        
        /**
         * Returns whether expected exceptions while setting values shall be asserted.
         * 
         * @return {@code true} for assert, {@code false} else
         */
        public default boolean assertSetExceptions() {
            return true;
        }

        /**
         * Returns whether expected exceptions while calling operations shall be asserted.
         * 
         * @return {@code true} for assert, {@code false} else
         */
        public default boolean assertOperationExceptions() {
            return true;
        }
        
        /**
         * Returns the qualified name of the start field.
         * 
         * @return the qualified name, <b>null</b> for do not write/access
         */
        public default String getQNameStart() {
            return null;
        }

        /**
         * Returns the qualified name of the stop field.
         * 
         * @return the qualified name, <b>null</b> for do not write/access
         */
        public default String getQNameStop() {
            return null;
        }

    }
    
    /**
     * Creates a new machine command input translator.
     * 
     * @param sourceType the source type
     * @param customizer the input customizer adapting this class to the specifc tpe
     */
    public MachineCommandInputTranslator(Class<? extends O> sourceType, InputCustomizer customizer) {
        this.sourceType = sourceType;
        this.customizer = customizer;
    }
    
    @Override
    public O from(MachineCommand data) throws IOException {
        ModelAccess access = getModelAccess();
        // generated code with "semantic" from configuration model
        if (data.isStart()) {
            access.call(customizer.getQNameOperationStartMachine());
        }
        if (data.isStop()) {
            access.call(customizer.getQNameOperationStopMachine());
        }
        if (data.getLotSize() > 0) {
            access.set(customizer.getQNameVarLotSize(), access.getOutputConverter().fromInteger(data.getLotSize()));
        }
        customizer.additionalFromActions(access, data);
        try {  // property does not exist
            access.set(customizer.getTopLevelModelPartName() + access.getQSeparator() + "abxy", "");
            if (customizer.assertSetExceptions()) {
                Assert.fail("Property shall not exist");
            }
        } catch (IOException e) {
            // expected
        }
        try {  // operation does not exist
            access.call(customizer.getTopLevelModelPartName() + access.getQSeparator() + "abxy");
            if (customizer.assertOperationExceptions()) {
                Assert.fail("Operation shall not exist");
            }
        } catch (IOException e) {
            // expected
        }
        try {
            access.call("abc" + access.getQSeparator() + "abxy"); // submodel/operation do not exist
            if (customizer.assertOperationExceptions()) {
                Assert.fail("Operation shall not exist");
            }
        } catch (IOException e) {
            // expected
        }
        try {
            access.call("abxy"); // no submodel
            if (customizer.assertOperationExceptions()) {
                Assert.fail("Operation shall not exist");
            }
        } catch (IOException e) {
            // expected
        }
        if (null != customizer.getQNameStart()) {
            access.set(customizer.getQNameStart(), access.getOutputConverter().fromBoolean(data.isStart()));
        }
        if (null != customizer.getQNameStop()) {
            access.set(customizer.getQNameStop(), access.getOutputConverter().fromBoolean(data.isStop()));
        }
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
