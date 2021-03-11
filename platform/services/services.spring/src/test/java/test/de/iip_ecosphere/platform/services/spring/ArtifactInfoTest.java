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

package test.de.iip_ecosphere.platform.services.spring;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.spring.yaml.Artifact;
import de.iip_ecosphere.platform.services.spring.yaml.Relation;
import de.iip_ecosphere.platform.services.spring.yaml.Service;
import de.iip_ecosphere.platform.services.spring.yaml.ServiceDependency;

/**
 * Tests {@link Artifact} and {@link Service}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArtifactInfoTest {

    /**
     * Tests the yaml reader.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testYaml() throws IOException {
        Artifact info = Artifact.readFromYaml(null);
        Assert.assertTrue(info.getServices().isEmpty());
        
        try {
            info = Artifact.readFromYaml(getClass().getClassLoader().getResourceAsStream("test-error.yml"));
            Assert.fail("No exception");
        } catch (IOException e) {
            // ok here, desired
        }
        
        // failing test.yml
        info = Artifact.readFromYaml(getClass().getClassLoader().getResourceAsStream("test.yml"));
        Assert.assertEquals("art", info.getId());
        Assert.assertEquals("art-name", info.getName());
        Assert.assertFalse(info.getServices().isEmpty());
        Assert.assertEquals(2, info.getServices().size());
        
        Service service = info.getServices().get(0);
        assertServiceBasics("id-0", "name-0", "1.0.2", "desc desc-0", service);
        Assert.assertEquals(2, service.getCmdArg().size());
        Assert.assertEquals("arg-0-1", service.getCmdArg().get(0));
        Assert.assertEquals("arg-0-2", service.getCmdArg().get(1));
        Assert.assertEquals(0, service.getDependencies().size());
        Assert.assertEquals(2, service.getRelations().size());
        service.getRelations().get(0);
        assertRelation("", 1234, "localhost", service.getRelations().get(0));
        assertRelation("input", 9872, "me.here.de", service.getRelations().get(1));
        
        service = info.getServices().get(1);
        assertServiceBasics("id-1", "name-1", "1.0.3", "desc desc-1", service);
        Assert.assertEquals(0, service.getCmdArg().size());
        Assert.assertEquals(1, service.getDependencies().size());
        assertDependency("id-0", service.getDependencies().get(0));
        Assert.assertEquals(1, service.getRelations().size());
        assertRelation("output", 9872, "me.here.de", service.getRelations().get(0));
    }
    
    /**
     * Asserts basic properties of a service.
     * 
     * @param id the expected service id
     * @param name the expected service name
     * @param version the expected service version
     * @param descr the expected description
     * @param service the service instance to be asserted
     */
    private static void assertServiceBasics(String id, String name, String version, String descr, Service service) {
        Assert.assertNotNull(service);
        Assert.assertEquals(id, service.getId());
        Assert.assertEquals(name, service.getName());
        Assert.assertEquals(version, service.getVersion());
        Assert.assertEquals(descr, service.getDescription());
    }

    /**
     * Asserts properties of a relation.
     * 
     * @param channel the expected channel name/id
     * @param port the port number to be used/substituted
     * @param host the host name to be used/substituted
     * @param relation the relation to be asserted
     */
    private static void assertRelation(String channel, int port, String host, Relation relation) {
        Assert.assertNotNull(relation);
        Assert.assertEquals(channel, relation.getChannel());
        Assert.assertEquals(relation.getPortArg().replace(Relation.PORT_PLACEHOLDER, String.valueOf(port)), 
            relation.getPortArg(port));
        Assert.assertEquals(relation.getHostArg().replace(Relation.HOST_PLACEHOLDER, host), 
            relation.getHostArg(host));
    }
    
    /**
     * Asserts properties of a dependency (to be extended).
     * 
     * @param id the expected service id
     * @param dependency the dependency to be asserted
     */
    private static void assertDependency(String id, ServiceDependency dependency) {
        Assert.assertNotNull(dependency);
        Assert.assertEquals(id, dependency.getId());
    }
    
}
