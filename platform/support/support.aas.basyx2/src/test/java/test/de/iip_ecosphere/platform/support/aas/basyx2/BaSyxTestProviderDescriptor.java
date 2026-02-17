/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.support.aas.basyx2;

import de.iip_ecosphere.platform.support.plugins.TestProviderDescriptor;

/**
 * Provides the tests requiring plugins/classloader separation to the external classpath-less test project.
 *
 * @author Holger Eichelberger, SSE
 */
public class BaSyxTestProviderDescriptor implements TestProviderDescriptor {

    @Override
    public Class<?>[] getTests(int index) {
        Class<?>[] result;
        switch (index) {
        case 0: // indexes along initial AllTest<x>.java
            result = new Class<?>[] {
                BaSyxTest.class, 
                BaSyxDeploymentTest.class};
            break;
        default:
            result = null;
            break;
        }
        return result;
    }

}
