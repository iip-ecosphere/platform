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

package test.de.iip_ecosphere.platform.configuration;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.ConfigurationManager;
import net.ssehub.easy.producer.core.mgmt.EasyExecutor;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;

/**
 * Tests the SimpleMesh model.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IvmlMetaModelTests extends AbstractIvmlTests {

    /**
     * Tests loading the meta model.
     */
    @Test
    public void testMetaModel() {
        ConfigurationLifecycleDescriptor lcd = assertLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        Assert.assertNotNull(ConfigurationManager.getIvmlConfiguration());
        // not much to do, no configuration, shall work anyway, not complete without configuration
        ReasoningResult rRes = ConfigurationManager.validateAndPropagate();
        EasyExecutor.printReasoningMessages(rRes);
        lcd.shutdown();
    }
    
}
