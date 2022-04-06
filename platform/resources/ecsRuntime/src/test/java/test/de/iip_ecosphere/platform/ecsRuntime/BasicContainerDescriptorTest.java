/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.ecsRuntime;

import java.io.File;

import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.BasicContainerDescriptor;
import org.junit.Assert;

/**
 * Tests {@link BasicContainerDescriptor}.
 *
 * @author Holger Eichelberger, SSE
 */
public class BasicContainerDescriptorTest {
    
    /**
     * Tests the descriptor.
     */
    @Test
    public void testDescriptor() {
        BasicContainerDescriptor desc = BasicContainerDescriptor.readFromYamlFile(
            new File("src/test/resources/image-info.yml"));
        Assert.assertNotNull(desc);
        Assert.assertEquals("01", desc.getId());
        Assert.assertEquals("test-container", desc.getName());
        Assert.assertEquals("1.0", desc.getVersion().toString());
        Assert.assertEquals("alpine-ssh-image.tar.gz", desc.getImageFile());
        Assert.assertEquals("alpine-ssh-image.tar.gz", desc.getDockerImageZipfile());
    }

}
