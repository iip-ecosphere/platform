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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.services.TypedDataDescriptor;
import de.iip_ecosphere.platform.services.AbstractServiceManager.TypedDataConnection;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.spring.SpringCloudArtifactDescriptor;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceDescriptor;
import de.iip_ecosphere.platform.services.spring.SpringCloudServiceManager;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;

/**
 * Tests selected service manager functionality based on simplified instances of a service manager loaded from 
 * a descriptor file.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceMgrAndDescriptorTest {
    
    /**
     * A mocking service manager for a given service artifact. Only selected functions are implemented!
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class MockServiceManager implements ServiceManager {

        private Map<String, SpringCloudArtifactDescriptor> arts = new HashMap<>();
        private Map<String, SpringCloudServiceDescriptor> svc = new HashMap<>();

        /**
         * Creates a service manager instance for the given artifact descriptor.
         * 
         * @param aDesc the artifact descriptor to analyze
         */
        private MockServiceManager(SpringCloudArtifactDescriptor aDesc) {
            arts.put(aDesc.getId(), aDesc);
            for (SpringCloudServiceDescriptor s: aDesc.getServices()) {
                svc.put(s.getId(), s);
            }
        }
        
        @Override
        public void updateService(String serviceId, URI location) throws ExecutionException {
        }
        
        @Override
        public void switchToService(String serviceId, String targetId) throws ExecutionException {
        }
        
        @Override
        public void stopService(String... serviceId) throws ExecutionException {
        }
        
        @Override
        public void startService(String... serviceId) throws ExecutionException {
            startService(null, serviceId);
        }

        @Override
        public void startService(Map<String, String> options, String... serviceId) throws ExecutionException {
        }

        @Override
        public void setServiceState(String serviceId, ServiceState state) throws ExecutionException {
        }
        
        @Override
        public void removeArtifact(String artifactId) throws ExecutionException {
        }
        
        @Override
        public void reconfigureService(String serviceId, Map<String, String> values) throws ExecutionException {
        }
        
        @Override
        public void passivateService(String serviceId) throws ExecutionException {
        }
        
        @Override
        public void migrateService(String serviceId, String resourceId) throws ExecutionException {
        }
        
        @Override
        public ServiceState getServiceState(String serviceId) {
            return null;
        }
        
        @Override
        public String addArtifact(URI location) throws ExecutionException {
            return null;
        }
        
        @Override
        public void activateService(String serviceId) throws ExecutionException {
        }
        
        @Override
        public Collection<? extends ServiceDescriptor> getServices() {
            return svc.values();
        }
        
        @Override
        public Set<String> getServiceIds() {
            return null;
        }
        
        @Override
        public ServiceDescriptor getService(String serviceId) {
            return svc.get(serviceId);
        }
        
        @Override
        public List<TypedDataDescriptor> getParameters(String serviceId) {
            return null;
        }
        
        @Override
        public List<TypedDataConnectorDescriptor> getOutputDataConnectors(String serviceId) {
            return null;
        }
        
        @Override
        public List<TypedDataConnectorDescriptor> getInputDataConnectors(String serviceId) {
            return null;
        }
        
        @Override
        public Collection<? extends ArtifactDescriptor> getArtifacts() {
            return arts.values();
        }
        
        @Override
        public Set<String> getArtifactIds() {
            return null;
        }
        
        @Override
        public ArtifactDescriptor getArtifact(String artifactId) {
            return arts.get(artifactId);
        }
        
        @Override
        public void cloneArtifact(String artifactId, URI location) throws ExecutionException {
        }

    }

    /**
     * Creates a simple service manager from loading a YAML file. Only basic operations referring to the loaded 
     * artifact descriptor and the contained services are implemented.
     * 
     * @param descriptor the descriptor to load
     * @return the service manager instance
     * @throws IOException if the descriptor cannot be loaded for some reason
     */
    static ServiceManager createServiceManager(File descriptor) throws IOException {
        YamlArtifact art = YamlArtifact.readFromYaml(new FileInputStream(descriptor));
        SpringCloudArtifactDescriptor aDesc = SpringCloudArtifactDescriptor.createInstance(art, 
            descriptor.toURI(), descriptor);
        return new MockServiceManager(aDesc);
    }

    /**
     * Asserts a function definition.
     * 
     * @param expected the expected result
     * @param mgr the service manager instance
     * @param serviceId the service ids to operate on
     */
    private static void assertFunctionDef(String expected, ServiceManager mgr, String... serviceId) {
        Set<TypedDataConnection> conn = AbstractServiceManager.determineInternalConnections(mgr, serviceId);
        String tmp = SpringCloudServiceDescriptor.toFunctionDefinition(conn);
        String[] exp = expected.split(";");
        if (exp.length == 1) {
            Assert.assertEquals(expected, tmp);
        } else {
            for (String e : exp) {
                Assert.assertTrue(tmp.startsWith(e) || tmp.endsWith(e) || tmp.contains(";" + e + ";"));
            }
        }
    }

    /**
     * Tests {@link AbstractServiceManager#determineInternalConnections(ServiceManager, String...)} in combination 
     * with {@link SpringCloudServiceDescriptor#toFunctionDefinition(Set)}.
     * 
     * @throws IOException if loading the test descriptor "ServiceMesh3Deployment.yml" fails
     */
    @Test
    public void testInternalConnections() throws IOException {
        ServiceManager mgr = createServiceManager(new File("src/test/resources/ServiceMesh3Deployment.yml"));
        
        // single services shall be activatable
        assertFunctionDef("receiveRec13_SimpleReceiver3", mgr, "SimpleReceiver3");
        assertFunctionDef("createRec13_SimpleSource3", mgr, "SimpleSource3");
        assertFunctionDef("transformRec13Rec13_SimpleTransformer3", mgr, "SimpleTransformer3");
        
        assertFunctionDef("receiveRec13_SimpleReceiver3;transformRec13Rec13_SimpleTransformer3", mgr, 
            "SimpleReceiver3", "SimpleTransformer3");
        assertFunctionDef("createRec13_SimpleSource3;transformRec13Rec13_SimpleTransformer3", mgr, 
            "SimpleSource3", "SimpleTransformer3");

        assertFunctionDef(
            "receiveRec13_SimpleReceiver3;createRec13_SimpleSource3;transformRec13Rec13_SimpleTransformer3", mgr, 
             "SimpleSource3", "SimpleReceiver3", "SimpleTransformer3");
    }
    
    /**
     * Tests {@link SpringCloudServiceManager#determineSpringConditionals(ServiceManager, String...)}.
     * 
     * @throws IOException if loading the test descriptor "ServiceMesh3Deployment.yml" fails
     */
    @Test
    public void testSpringConditionals() throws IOException  {
        ServiceManager mgr = createServiceManager(new File("src/test/resources/ServiceMesh3Deployment.yml"));
        List<String> args = SpringCloudServiceManager.determineSpringConditionals(mgr, 
            "SimpleTransformer3", "SimpleReceiver3");
        String[] tmp = args.toArray(new String[0]);
        Assert.assertTrue(args.size() == 3); // 3 services in yml, subset, must mention all
        Assert.assertTrue(CmdLine.getBooleanArg(tmp, 
            SpringCloudServiceManager.OPT_SERVICE_PREFIX + "SimpleTransformer3", false));
        Assert.assertTrue(CmdLine.getBooleanArg(tmp, 
            SpringCloudServiceManager.OPT_SERVICE_PREFIX + "SimpleReceiver3", false));
        Assert.assertFalse(CmdLine.getBooleanArg(tmp, 
            SpringCloudServiceManager.OPT_SERVICE_PREFIX + "SimpleSource3", true));

        args = SpringCloudServiceManager.determineSpringConditionals(mgr, 
            "SimpleSource3");
        tmp = args.toArray(new String[0]);
        Assert.assertTrue(args.size() == 3); // 3 services in yml, subset, must mention all
        Assert.assertFalse(CmdLine.getBooleanArg(tmp, 
            SpringCloudServiceManager.OPT_SERVICE_PREFIX + "SimpleTransformer3", true));
        Assert.assertFalse(CmdLine.getBooleanArg(tmp, 
            SpringCloudServiceManager.OPT_SERVICE_PREFIX + "SimpleReceiver3", true));
        Assert.assertTrue(CmdLine.getBooleanArg(tmp, 
            SpringCloudServiceManager.OPT_SERVICE_PREFIX + "SimpleSource3", false));
    }
    
    /**
     * Tests {@link SpringCloudServiceManager#handleOptions(Map, ServiceManager, String...)}.
     * 
     * @throws IOException if loading the test descriptor "ServiceMesh3Deployment.yml" fails
     */
    @Test
    public void testOptions() throws IOException {
        ServiceManager mgr = createServiceManager(new File("src/test/resources/ServiceMesh3Deployment.yml"));
        Map<String, String> opts = new HashMap<>();
        opts.put("DONTKNOW", null);
        opts.put("ensemble", "{\"SimpleReceiver3\":\"SimpleTransformer3\", \"a\":\"b\", \"SimpleSource3\":\"c\"}");
        SpringCloudServiceManager.handleOptions(opts, mgr, "SimpleTransformer3", "SimpleReceiver3");
        Assert.assertEquals(mgr.getService("SimpleReceiver3").getEnsembleLeader(), 
            mgr.getService("SimpleTransformer3"));
        Assert.assertNull(mgr.getService("SimpleTransformer3").getEnsembleLeader());
        Assert.assertNull(mgr.getService("SimpleSource3").getEnsembleLeader());

        opts.put("ensemble", "{\"SimpleReceiver3\":\"\", \"a\":\"b\", \"SimpleSource3\":\"c\"}");
        SpringCloudServiceManager.handleOptions(opts, mgr, "SimpleTransformer3", "SimpleReceiver3");
        Assert.assertNull(mgr.getService("SimpleReceiver3").getEnsembleLeader());
        Assert.assertNull(mgr.getService("SimpleTransformer3").getEnsembleLeader());
        Assert.assertNull(mgr.getService("SimpleSource3").getEnsembleLeader());
    }

    /**
     * Tests {@link SpringCloudServiceManager#handleOptions(Map, ServiceManager, String...)}.
     * 
     * @throws IOException if loading the test descriptor "ServiceMesh3Deployment.yml" fails
     */
    @Test
    public void testTopLevel() throws IOException {
        ServiceManager mgr = createServiceManager(new File("src/test/resources/ServiceMesh3Deployment.yml"));
        String[] res = SpringCloudServiceManager.topLevel(mgr, "SimpleTransformer3", "SimpleReceiver3");
        
        // all are top-level
        List<String> resList = new ArrayList<>();
        CollectionUtils.addAll(resList, res);
        Assert.assertTrue(resList.size() == 2);
        Assert.assertTrue(resList.contains("SimpleTransformer3"));
        Assert.assertTrue(resList.contains("SimpleReceiver3"));
    }

}
