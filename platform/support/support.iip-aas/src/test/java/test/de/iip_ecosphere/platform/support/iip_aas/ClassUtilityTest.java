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

package test.de.iip_ecosphere.platform.support.iip_aas;

import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.SubModel;
import de.iip_ecosphere.platform.support.iip_aas.ClassUtility;

/**
 * Tests {@link ClassUtility}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClassUtilityTest {

    /**
     * Tests adding a type to an AAS.
     */
    @Test
    public void testAddTypeToClass() {
        // TODO not the final test
        ClassUtility.addType((Aas) null, null);
    }

    /**
     * Tests adding a type to a sub-model.
     */
    @Test
    public void testAddTypeToSubModel() {
        ClassUtility.addType((SubModel) null, null);
    }

}
