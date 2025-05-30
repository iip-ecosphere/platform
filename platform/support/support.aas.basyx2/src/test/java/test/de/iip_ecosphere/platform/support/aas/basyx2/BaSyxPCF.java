/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx2;

import java.io.IOException;

import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.IdentifierType;
import de.iip_ecosphere.platform.support.aas.SemanticIdRecognizer;
import test.de.iip_ecosphere.platform.support.aas.PCF;

/**
 * Sets up the {@link PCF} for BaSyx. You will find the persisted AAS in the {@code output} folder.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxPCF extends PCF {
    
    /**
     * Executes the test standalone.
     * 
     * @param args command line arguments, the first argument may be {@code --withOperations} to 
     *   enable the creation of operations
     */
    public static void main(String[] args) {
        BaSyxExampleUtils.execute(new BaSyxPCF(), args);
    }
    
    /**
     * Tests creating and storing the AAS.
     * 
     * @throws IOException if persisting does not work.
     */
    @Test
    public void testCreateAndStore() throws IOException {
        // some IRIs are stated as IRDIs in IDTA spec, fix this here as the BaSyx2 integration
        // infers the prefix via the SemanticIdRecognizer
        SemanticIdRecognizer fake = SemanticIdRecognizer.register(new SemanticIdRecognizer() {
            
            @Override
            public String parseSemanticId(String value) {
                return value;
            }
            
            @Override
            public boolean isFallback() {
                return false;
            }
            
            @Override
            public boolean isASemanticId(String value) {
                return true;
            }
            
            @Override
            public boolean handles(String value) {
                return "https://admin-shell.io/idta/CarbonFootprint/ProductCarbonFootprint/0/9".equals(value);
            }
            
            @Override
            public String getIdentifierPrefix() {
                return IdentifierType.IRDI_PREFIX;
            }
        });
        testCreateAndStore(true);
        SemanticIdRecognizer.unregister(fake);
    }

}
