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

package test.de.iip_ecosphere.platform.ecsRuntime.docker;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.docker.DockerSetup;

/**
 * Tests {@link DockerSetup}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DockerSetupTest {
    
    /**
     * Tests the configuration.
     */
    @Test
    public void testConfiguration() throws IOException {
        DockerSetup cfg = DockerSetup.readFromYaml();
        Assert.assertNotNull(cfg);
        Assert.assertNotNull(cfg.getDocker().getDockerHost());
        Assert.assertTrue(cfg.getDocker().getDockerHost().length() > 0);
    }

}
