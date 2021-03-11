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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.spring.yaml.Artifact;
import de.iip_ecosphere.platform.services.spring.yaml.Endpoint;
import de.iip_ecosphere.platform.services.spring.yaml.Process;
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
        assertServiceBasics(service, "id-0", "name-0", "1.0.2", "desc desc-0");
        assertCmdArgs(service.getCmdArg(), "arg-0-1", "arg-0-2");
        Assert.assertEquals(0, service.getDependencies().size());
        Assert.assertEquals(2, service.getRelations().size());
        service.getRelations().get(0);
        assertRelation(service.getRelations().get(0), "", 1234, "localhost");
        assertRelation(service.getRelations().get(1), "input", 9872, "me.here.de");
        Assert.assertNull(service.getProcess());
        
        service = info.getServices().get(1);
        assertServiceBasics(service, "id-1", "name-1", "1.0.3", "desc desc-1");
        assertCmdArgs(service.getCmdArg());
        Assert.assertEquals(1, service.getDependencies().size());
        assertDependency(service.getDependencies().get(0), "id-0");
        Assert.assertEquals(1, service.getRelations().size());
        assertRelation(service.getRelations().get(0), "output", 9872, "me.here.de");
        Assert.assertNotNull(service.getProcess());
        assertProcess(service.getProcess(), "impl/python", "python", "MyServiceWrapper.py");
    }
    
    /**
     * Asserts a list of command line arguments w.r.t. the {@code expected} arguments.
     * 
     * @param args the arguments
     * @param expected the expected values
     */
    private static void assertCmdArgs(List<String> args, String... expected) {
        Assert.assertNotNull(args);
        Assert.assertEquals(expected.length, args.size());
        int a = 0;
        for (String e: expected) {
            Assert.assertEquals(e, args.get(a));
            a++;
        }
        
    }
    
    /**
     * Asserts basic properties of a {@link Service}.
     * 
     * @param service the service instance to be asserted
     * @param id the expected service id
     * @param name the expected service name
     * @param version the expected service version
     * @param descr the expected description
     */
    private static void assertServiceBasics(Service service, String id, String name, String version, String descr) {
        Assert.assertNotNull(service);
        Assert.assertEquals(id, service.getId());
        Assert.assertEquals(name, service.getName());
        Assert.assertEquals(version, service.getVersion());
        Assert.assertEquals(descr, service.getDescription());
    }

    /**
     * Asserts properties of a {@link Relation}.
     * 
     * @param relation the relation to be asserted
     * @param channel the expected channel name/id
     * @param port the port number to be used/substituted
     * @param host the host name to be used/substituted
     */
    private static void assertRelation(Relation relation, String channel, int port, String host) {
        Assert.assertNotNull(relation);
        Assert.assertEquals(channel, relation.getChannel());
        assertEndpoint(relation.getEndpoint(), port, host);
    }

    /**
     * Asserts properties of an {@link Endpoint}.
     * 
     * @param port the port number to be used/substituted
     * @param host the host name to be used/substituted
     * @param endpoint the endpoint to be asserted
     */
    private static void assertEndpoint(Endpoint endpoint, int port, String host) {
        Assert.assertNotNull(endpoint);
        Assert.assertEquals(endpoint.getPortArg().replace(Endpoint.PORT_PLACEHOLDER, String.valueOf(port)), 
            endpoint.getPortArg(port));
        Assert.assertEquals(endpoint.getHostArg().replace(Endpoint.HOST_PLACEHOLDER, host), 
            endpoint.getHostArg(host));
    }
    
    /**
     * Asserts properties of a {@link ServiceDependency} (to be extended).
     * 
     * @param id the expected service id
     * @param dependency the dependency to be asserted
     */
    private static void assertDependency(ServiceDependency dependency, String id) {
        Assert.assertNotNull(dependency);
        Assert.assertEquals(id, dependency.getId());
    }
    
    /**
     * Asserts {@link Process} information.
     * 
     * @param process the process to assert
     * @param path the expected path
     * @param cmdArgs the expected command line arguments
     */
    private static void assertProcess(Process process, String path, 
        String... cmdArgs) {
        Assert.assertEquals(path, process.getPath());
        assertEndpoint(process.getStreamEndpoint(), 1234, "localhost");
        assertEndpoint(process.getAasEndpoint(), 1235, "aas.de");
        assertCmdArgs(process.getCmdArg(), cmdArgs);
    }
    
}
