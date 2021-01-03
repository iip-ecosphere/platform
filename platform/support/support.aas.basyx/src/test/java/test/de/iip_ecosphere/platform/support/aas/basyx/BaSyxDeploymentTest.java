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

package test.de.iip_ecosphere.platform.support.aas.basyx;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Ignore;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.basyx.Tomcats;

/**
 * Tests deployment scenarios.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxDeploymentTest extends test.de.iip_ecosphere.platform.support.aas.DeploymentTest {
    
    /**
     * Tests a changed attribute value on a dynamically deployed sub-model elements collection. Does not work,
     * similarly when creating the connectors component AAS.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur
     */
    @Ignore("Fails setting value with ResourceNotFoundException -> tries to access ArrayList")
    @Test
    public void localDynamicSubmodelElementsCollectionPropertyDeployment() throws IOException, ExecutionException {
        super.localDynamicSubmodelElementsCollectionPropertyDeployment();
    }

    /**
     * Tests a remote AAS deployment.
     */
    @Test
    public void remoteAasDeploymentTest() throws IOException {
        super.remoteAasDeploymentTest();
        Tomcats.clear(); // just that it is done once
    }

}
