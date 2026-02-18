/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.spring;

import de.iip_ecosphere.platform.support.plugins.TestProviderDescriptor;

/**
 * Provides the tests requiring plugins/classloader separation to the external classpath-less test project.
 *  
 * @author Holger Eichelberger, SSE
 */
public class SpringTestProviderDescriptor implements TestProviderDescriptor {

    @Override
    public Class<?>[] getTests(int index) {
        Class<?>[] result;
        switch (index) {
        case 0: // indexes along initial AllTest<x>.java
            result = new Class<?>[] {
                SimpleStartStopServiceManagerTest.class, 
                ServerStartStopServiceManagerTest.class};
            break;
        case 1: // this does not run together with 0 in BaSyx2 (setURLStreamHandlerFactory)
            result = new Class<?>[] {TestLifecycleDescriptor.class};
            break;
        case 2:
            result = new Class<?>[] {EnsembleStartStopServiceManagerTest.class};
            break;
        case 3:
            result = new Class<?>[] {ZipCpServiceManagerTest.class};
            break;
        case 4:
            result = new Class<?>[] {ZipNoCpServiceManagerTest.class};
            break;
        default:
            result = null;
            break;
        }
        return result;
    }

}
