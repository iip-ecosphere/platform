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

package test.de.iip_ecosphere.platform.platform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.junit.Test;

import de.iip_ecosphere.platform.deviceMgt.Credentials;
import de.iip_ecosphere.platform.deviceMgt.DeviceManagementClient;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsClient;
import de.iip_ecosphere.platform.ecsRuntime.ResourcesClient;
import de.iip_ecosphere.platform.platform.cli.DeviceManagementClientFactory;
import de.iip_ecosphere.platform.platform.cli.EcsClientFactory;
import de.iip_ecosphere.platform.platform.cli.PlatformClientFactory;
import de.iip_ecosphere.platform.platform.cli.ResourcesClientFactory;
import de.iip_ecosphere.platform.platform.cli.ServicesClientFactory;
import de.iip_ecosphere.platform.services.ServicesClient;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.PlatformClient;

import org.junit.Assert;

/**
 * Tests the platform Cli.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CliTest {

    /**
     * Successful nested command sequence. 
     */
    private static final String[] COMPLETE_SEQUENCE = new String[] {
        "help",
        "resources",
        "list",
        "back",
        "container", "abb0", 
        "list",
        "add", "file://test.yml", 
        "start", "test", 
        "..",
        "services", "abb0", 
        "add", "file://test.jar", 
        "listArtifacts",
        "startAll", "test", 
        "listServices",
        "stopAll", "test", 
        "remove", "test", 
        "..",
        "container", "abb0", 
        "stop", "test", 
        "undeploy", "test", 
        "exit"
    };

    /**
     * Successful command sequence within resources level. 
     */
    private static final String[] RESOURCES_SEQUENCE = new String[] {
        "resources",
        "help",
        "list",
        "createSsh", "abb0",
        "exit"
    };

    /**
     * Successful command sequence within services level. 
     */
    private static final String[] SERVICES_SEQUENCE = new String[] {
        "services", "ab01",
        "help",
        "exit"
    };

    /**
     * Successful service start command sequence within services level. 
     */
    private static final String[] SERVICES_START_SEQUENCE = new String[] {
        "services", "ab01",
        "start", "s1", "s2", ".",
        "exit"
    };

    /**
     * Successful command sequence within container level. 
     */
    private static final String[] CONTAINER_SEQUENCE = new String[] {
        "container", "ab01",
        "help",
        "exit"
    };

    /**
     * Successful command sequence to create an AAS snapshot. 
     */
    private static final String[] SNAPSHOT_SEQUENCE = new String[] {
        "help",
        "snapshotAAS",
        "exit"
    };

    /**
     * Fails with unknown command within main level. 
     */
    private static final String[] MAIN_FAIL = new String[] {
        "ccd",
        "exit"
    };

    /**
     * Fails with unknown command within resources level. 
     */
    private static final String[] RESOURCES_FAIL = new String[] {
        "resources", 
        "abx",
        "exit"
    };

    /**
     * Fails with unknown command within service level. 
     */
    private static final String[] SERVICES_FAIL = new String[] {
        "services", "ab01",
        "abx",
        "exit"
    };

    /**
     * Fails with missing service ids within service level. 
     */
    private static final String[] SERVICES_START_FAIL = new String[] {
        "services", "ab01",
        "start",
        "exit"
    };

    /**
     * Fails with unknown command within container level. 
     */
    private static final String[] CONTAINER_FAIL = new String[] {
        "container", "ab01",
        "abx",
        "exit"
    };

    /**
     * The absolute URI of the test deployment plan.
     */
    private static final String DEPLOYMENT_PLAN_URI = new File("./src/test/resources/plan.yml")
        .getAbsoluteFile().toURI().toString();
    
    /**
     * Successful sequence for deployment plan execution.
     */
    private static final String[] DEPLOY_SEQUENCE = new String[] {
        "deploy", DEPLOYMENT_PLAN_URI,
        "undeploy", DEPLOYMENT_PLAN_URI,
        "exit"
    };

    /**
     * Mock services client/factory for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ServicesFactory implements ServicesClientFactory, ServicesClient {

        @Override
        public ServicesClient create(String resourceId) throws IOException {
            return this;
        }

        @Override
        public String addArtifact(URI location) throws ExecutionException {
            return null;
        }

        @Override
        public void startService(String... serviceId) throws ExecutionException {
        }

        @Override
        public void stopService(String... serviceId) throws ExecutionException {
        }

        @Override
        public void migrateService(String serviceId, String resourceId) throws ExecutionException {
        }

        @Override
        public void removeArtifact(String artifactId) throws ExecutionException {
        }

        @Override
        public void updateService(String serviceId, URI location) throws ExecutionException {
        }

        @Override
        public void switchToService(String serviceId, String targetId) throws ExecutionException {
        }

        @Override
        public void activateService(String serviceId) throws ExecutionException {
        }

        @Override
        public void passivateService(String serviceId) throws ExecutionException {
        }

        @Override
        public void reconfigureService(String serviceId, Map<String, String> values) throws ExecutionException {
        }

        @Override
        public void setServiceState(String serviceId, ServiceState state) throws ExecutionException {
        }

        @Override
        public ServiceState getServiceState(String serviceId) {
            return null;
        }

        @Override
        public String[] getServices(String artifactId, boolean topLevelOnly) {
            return null;
        }

        @Override
        public SubmodelElementCollection getServices() {
            return null;
        }

        @Override
        public SubmodelElementCollection getArtifacts() {
            return null;
        }
        
    }

    /**
     * Mock services client/factory for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class EcsFactory implements EcsClient, EcsClientFactory {

        @Override
        public EcsClient create(String resourceId) throws IOException {
            return this;
        }

        @Override
        public String addContainer(URI location) throws ExecutionException {
            return null;
        }

        @Override
        public void startContainer(String id) throws ExecutionException {
        }

        @Override
        public void stopContainer(String id) throws ExecutionException {
        }

        @Override
        public void migrateContainer(String id, String resourceId) throws ExecutionException {
        }

        @Override
        public void undeployContainer(String id) throws ExecutionException {
        }

        @Override
        public void updateContainer(String id, URI location) throws ExecutionException {
        }

        @Override
        public ContainerState getState(String id) {
            return null;
        }

        @Override
        public String getContainerSystemName() {
            return null;
        }

        @Override
        public String getContainerSystemVersion() {
            return null;
        }

        @Override
        public SubmodelElementCollection getContainers() {
            return null;
        }

        @Override
        public Credentials createRemoteConnectionCredentials() throws ExecutionException {
            return null;
        }

        @Override
        public String getRuntimeName() throws ExecutionException {
            return null;
        }

        @Override
        public Integer getRuntimeVersion() throws ExecutionException {
            return null;
        }
        
    }

    /**
     * Mock resources client/factory for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ResourcesFactory implements ResourcesClientFactory, ResourcesClient {

        @Override
        public ResourcesClient create() throws IOException {
            return this;
        }

        @Override
        public Submodel getResources() throws IOException {
            return null;
        }
        
    }

    /**
     * Mock device management client/factory for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class DeviceManagementFactory implements DeviceManagementClientFactory, DeviceManagementClient {

        @Override
        public DeviceManagementClient create() throws IOException {
            return this;
        }

        @Override
        public void updateRuntime(String id) throws ExecutionException {
        }

        @Override
        public SSHConnectionDetails establishSsh(String id) throws ExecutionException {
            return new SSHConnectionDetails("host", 22, "me", "here");
        }

        @Override
        public void setConfig(String id, String configPath) throws ExecutionException {
        }
        
    }
    
    /**
     * Mock platform client/factory for testing.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class PlatformFactory implements PlatformClientFactory, PlatformClient {

        @Override
        public PlatformClient create() throws IOException {
            return this;
        }

        @Override
        public String snapshotAas(String id) throws ExecutionException {
            return "";
        }
        
    }
    
    /**
     * Accepts and counts error messages.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ErrorConsumer implements Consumer<String> {

        private int count = 0;
        
        @Override
        public void accept(String text) {
            count++;
        }

        /**
         * Clears the error count.
         */
        private void clear() {
            count = 0;
        }
        
        /**
         * Asserts the error count.
         * 
         * @param msg the message if the expected count does not hold
         * @param expected expected number
         */
        private void assertCount(String msg, int expected) {
            Assert.assertEquals(msg, expected, count);
        }
        
    }
    
    
    /**
     * Tests the Cli mocking out the AAS functionality as already tested.
     */
    @Test
    public void testCli() {
        ServicesFactory servicesFactory = new ServicesFactory();
        EcsFactory ecsFactory = new EcsFactory();
        ResourcesFactory resourcesFactory = new ResourcesFactory();
        DeviceManagementClientFactory deviceManagementClientFactory = new DeviceManagementFactory();
        ErrorConsumer errorConsumer = new ErrorConsumer();
        PlatformClientFactory platformClientFactory = new PlatformFactory();
        de.iip_ecosphere.platform.platform.Cli.setFactories(servicesFactory, ecsFactory, resourcesFactory, 
            deviceManagementClientFactory, platformClientFactory);
        de.iip_ecosphere.platform.platform.Cli.setErrorConsumer(errorConsumer);
        
        test(COMPLETE_SEQUENCE, errorConsumer, 0);
        test(RESOURCES_SEQUENCE, errorConsumer, 0);
        test(CONTAINER_SEQUENCE, errorConsumer, 0);
        test(SERVICES_SEQUENCE, errorConsumer, 0);
        test(SERVICES_START_SEQUENCE, errorConsumer, 0);
        test(SNAPSHOT_SEQUENCE, errorConsumer, 0);
        test(DEPLOY_SEQUENCE, errorConsumer, 0);

        test(MAIN_FAIL, errorConsumer, 1);
        test(RESOURCES_FAIL, errorConsumer, 1);
        test(CONTAINER_FAIL, errorConsumer, 1);
        test(SERVICES_FAIL, errorConsumer, 1);
        test(SERVICES_START_FAIL, errorConsumer, 1);
        
        de.iip_ecosphere.platform.platform.Cli.setFactories(ServicesClientFactory.DEFAULT, EcsClientFactory.DEFAULT, 
            ResourcesClientFactory.DEFAULT, DeviceManagementClientFactory.DEFAULT, PlatformClientFactory.DEFAULT);
        de.iip_ecosphere.platform.platform.Cli.setErrorConsumer(
            de.iip_ecosphere.platform.platform.Cli.DEFAULT_ERROR_CONSUMER);
    }

    /**
     * Tests the given command sequence on both, interactive and argument-based mode.
     * 
     * @param cmds the commands
     * @param errorConsumer the error consumer applied to the cli
     * @param expectedErrors the expected number of errors
     */
    private static void test(String[] cmds, ErrorConsumer errorConsumer, int expectedErrors) {
        String allCmds = "";
        for (String c: cmds) {
            allCmds += c + "\n";
        }
        System.setIn(new ByteArrayInputStream(allCmds.getBytes()));
        de.iip_ecosphere.platform.platform.Cli.main(new String[] {});
        errorConsumer.assertCount("Interactive run", expectedErrors);
        errorConsumer.clear();
        de.iip_ecosphere.platform.platform.Cli.main(cmds);
        errorConsumer.assertCount("Argument-based run", expectedErrors);
        errorConsumer.clear();
    }

}
