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

package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.kubernetes.KubernetesConfiguration;

/**
 * Tests {@link KubernetesConfiguration}.
 * 
 * @author Ahmad Alomosh, SSE
 */
public class KubernetesConfigurationTest {
    
    /**
     * Tests the configuration.
     */
    @Test
    public void testConfiguration() throws IOException {
        KubernetesConfiguration cfg = KubernetesConfiguration.readFromYaml();
        Assert.assertNotNull(cfg);
        // TODO read out relevant information for initial test
    }

}
