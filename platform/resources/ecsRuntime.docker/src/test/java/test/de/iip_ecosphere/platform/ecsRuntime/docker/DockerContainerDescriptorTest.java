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

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.InternetProtocol;

import de.iip_ecosphere.platform.ecsRuntime.docker.DockerContainerDescriptor;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;

/**
 * Tests a given docker descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DockerContainerDescriptorTest {
    
    /**
     * Tests reading descriptors.
     */
    @Test
    public void testDescriptor() {
        Assert.assertNull(DockerContainerDescriptor.readFromYaml(ResourceLoader.getResourceAsStream("xyz.yml"), 
            new File("xyz.yml").toURI()));
        
        DockerContainerDescriptor desc = DockerContainerDescriptor.readFromYaml(
            ResourceLoader.getResourceAsStream("mesh-info.yml"), new File("mesh-info.yml").toURI());
        Assert.assertNotNull(desc);
        
        Assert.assertEquals("test-serviceMgr", desc.getId());
        Assert.assertEquals("Test Service Manager", desc.getName());
        Assert.assertEquals("1.0", desc.getVersion().toString());
        Assert.assertEquals("iip/serviceMgr", desc.getDockerImageName());
        Assert.assertEquals("serviceMgr.tar.gz", desc.getDockerImageZipfile());
        Assert.assertEquals(1, desc.getEnv().size());
        Assert.assertEquals("IIP_PORT=${port}", desc.getEnv().get(0));
        Assert.assertEquals(1, desc.instantiateEnv(1234, 0).size());
        Assert.assertEquals("IIP_PORT=1234", desc.instantiateEnv(1234, 0).get(0));
        Assert.assertEquals(4, desc.getExposedPorts().size());
        Assert.assertEquals("${port}/TCP", desc.getExposedPorts().get(0));
        Assert.assertEquals("22/UDP", desc.getExposedPorts().get(1));
        Assert.assertEquals("80", desc.getExposedPorts().get(2));
        Assert.assertEquals("8080/DEFAULT", desc.getExposedPorts().get(3));
        List<ExposedPort> exp = desc.instantiateExposedPorts(1235, 0);
        Assert.assertEquals(4, exp.size());
        Assert.assertEquals(1235, exp.get(0).getPort());
        Assert.assertEquals(InternetProtocol.TCP, exp.get(0).getProtocol());
        Assert.assertEquals(22, exp.get(1).getPort());
        Assert.assertEquals(InternetProtocol.UDP, exp.get(1).getProtocol());
        Assert.assertEquals(80, exp.get(2).getPort());
        Assert.assertEquals(InternetProtocol.DEFAULT, exp.get(2).getProtocol());
        Assert.assertEquals(8080, exp.get(3).getPort());
        Assert.assertEquals(InternetProtocol.DEFAULT, exp.get(3).getProtocol());
        Assert.assertTrue(desc.requiresPort(DockerContainerDescriptor.PORT_PLACEHOLDER));
        Assert.assertTrue(desc.getDood());
        Assert.assertTrue(desc.getAttachStdIn());
        Assert.assertFalse(desc.getPrivileged()); // not required
        Assert.assertTrue(desc.getWithTty());
        Assert.assertEquals("host", desc.getNetworkMode());
        Assert.assertFalse(desc.getAttachStdOut());
        Assert.assertFalse(desc.getAttachStdErr());
        Assert.assertNotNull(desc.getUri());
    }

}
