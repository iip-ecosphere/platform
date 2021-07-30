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

package de.iip_ecosphere.platform.services.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.app.AppStatus;
import org.springframework.cloud.deployer.spi.app.DeploymentState;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.services.AbstractServiceManager;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactoryDescriptor;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.ServicesAas;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.spring.descriptor.Validator;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.uri.UriResolver;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;

import static de.iip_ecosphere.platform.services.spring.SpringInstances.*;

/**
 * Service manager for Spring Cloud Stream. Requires {@link SpringInstances} set correctly before use.
 * 
 * @author Holger Eichelberger, SSE
 */
@Component
@Import(SpringCloudServiceConfiguration.class)
public class SpringCloudServiceManager 
    extends AbstractServiceManager<SpringCloudArtifactDescriptor, SpringCloudServiceDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudServiceManager.class);
    private Predicate<TypedDataConnectorDescriptor> available = c -> true;
    
    // do not rename this class or the following descriptor class! Java Service Loader
    
    /**
     * Descriptor for creating the service manager instance.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class SpringCloudServiceFactoryDescriptor implements ServiceFactoryDescriptor {

        @Override
        public ServiceManager createInstance() {
            return new SpringCloudServiceManager();
        }

        @Override
        public AasSetup getAasSetup() {
            // may not yet have been initialized
            return null != SpringInstances.getConfig() ? SpringInstances.getConfig().getAas() : null;
        }

        @Override
        public TransportSetup getTransport() {
            return null != SpringInstances.getConfig() ? SpringInstances.getConfig().getTransport() : null;
        }
        
    }

    // TODO upon start, scan file-system for containers and add them automatically if applicable

    /**
     * Prevents external creation.
     */
    private SpringCloudServiceManager() {
    }

    @Override
    protected Predicate<TypedDataConnectorDescriptor> getAvailablePredicate() {
        if (null == available) {
            available = ServicesAas.createAvailabilityPredicate(SpringInstances.getConfig().getWaitingTime(), 
                SpringInstances.getConfig().getAvailabilityRetryDelay(), false);
        }
        return available;
    }
    
    @Override
    public String addArtifact(URI location) throws ExecutionException {
        LOGGER.info("Adding " + location);
        try {
            File jarFile = UriResolver.resolveToFile(location, SpringInstances.getConfig().getDownloadDir());
            YamlArtifact yamlArtifact = null;
            if (null != jarFile) {
                yamlArtifact = readFromFile(jarFile);
            } else {
                throwExecutionException("Adding " + location, 
                    "Cannot load " + location + ". Must be a (resolved) file.");
            }
            Validator val = new Validator();
            val.validate(yamlArtifact);
            if (val.hasMessages()) {
                throwExecutionException("Adding " + location, "Problems in descriptor:\n" + val.getMessages());
            }
            SpringCloudArtifactDescriptor artifact = SpringCloudArtifactDescriptor.createInstance(
                yamlArtifact, jarFile);
            return super.addArtifact(artifact.getId(), artifact);
        } catch (IOException e) {
            throwExecutionException("Adding " + location, e);
            return null;
        }
    }
    
    /**
     * Throws an execution exception for the given throwable.
     * @param action the actual action to log
     * @param th the throwable
     * @throws ExecutionException
     */
    private static void throwExecutionException(String action, Throwable th) throws ExecutionException {
        LOGGER.error(action + ": " + th.getMessage());
        throw new ExecutionException(th);
    }

    // checkstyle: stop exception type check

    /**
     * Throws an execution exception for the given message.
     * @param action the actual action to log
     * @param message the message for the exception
     * @throws ExecutionException
     */
    private static void throwExecutionException(String action, String message) throws ExecutionException {
        LOGGER.error(action + ": " + message);
        throw new ExecutionException(message, null);
    }
    
    /**
     * Reads the YAML deployment descriptor from {@code file}.
     * 
     * @param file the file to read from
     * @return the parsed descriptor
     * @throws ExecutionException if reading fails for some reason
     */
    static YamlArtifact readFromFile(File file) throws ExecutionException {
        YamlArtifact result = null;
        if (file.getName().endsWith(".jar")) {
            try {
                String descName = "deployment.yml";
                if (null != getConfig()) { // null in case of standalone/non-spring execution
                    descName = getConfig().getDescriptorName();
                }
                LOGGER.info("Reading artifact " + file + ", descriptor " + descName);
                InputStream descStream = JarUtils.findFile(new FileInputStream(file), "BOOT-INF/classes/" + descName);
                if (null != descStream) {
                    result = YamlArtifact.readFromYaml(descStream); 
                } else {
                    throwExecutionException("Reading artifact " + file, descName + " does not exist in " + file);
                }
            } catch (IOException e) {
                throwExecutionException("Reading artifact " + file, e);
            }
        } else {
            throwExecutionException("Reading artifact " + file, file + " is not considered as JAR file");
        }
        return result;
    }

    @Override
    public void startService(String... serviceIds) throws ExecutionException {
        AppDeployer deployer = getDeployer();
        // TODO add/check causes for failing, potentially re-sort remaining services iteratively 
        List<String> errors = new ArrayList<>();
        LOGGER.info("Starting services " + Arrays.toString(serviceIds));
        SpringCloudServiceConfiguration config = getConfig();
        for (String ids : sortByDependency(serviceIds, true)) {
            SpringCloudServiceDescriptor service = getService(ids);
            if (null == service) {
                errors.add("No service for id '" + ids + "' known.");
            } else {
                AppDeploymentRequest req = service.createDeploymentRequest(config);
                if (null != req) {
                    setState(service, ServiceState.DEPLOYING);
                    LOGGER.info("Starting " + ids);
                    String id = deployer.deploy(req);
                    waitFor(id, null, s -> null == s || s == DeploymentState.deploying);
                    LOGGER.info("Starting " + id + ": " + deployer.status(id));
                    AppStatus status = deployer.status(id); 
                    service.setDeploymentId(id);
                    if (DeploymentState.deployed == status.getState()) {
                        service.attachStub();
                        setState(service, ServiceState.RUNNING); // preliminary, done by/via service???
                        LOGGER.info("Starting " + ids + " completed");
                    } else {
                        setState(service, ServiceState.FAILED);
                        errors.add("Starting service id '" + ids + "' failed:\n" + getDeployer().getLog(id));
                        LOGGER.info("Starting " + id + " failed");
                    }
                } else {
                    LOGGER.info("Starting ensemble service " + ids);
                    ServiceState ensState = service.getEnsembleLeader().getState();
                    if (ServiceState.RUNNING == ensState) {
                        service.attachStub();
                        setState(service, ServiceState.RUNNING); // preliminary, done by/via service???
                        LOGGER.info("Starting ensemble service " + ids + " completed");
                    } else {
                        setState(service, ServiceState.FAILED);
                        errors.add("Starting ensemble service id '" + ids + "' failed: See " 
                            + service.getEnsembleLeader().getId());
                        LOGGER.info("Starting ensemble service " + ids + " failed");
                    }
                    
                }
            }
        }
        checkErrors(errors);
        LOGGER.info("Started services " + Arrays.toString(serviceIds));
    }
    
    /**
     * Checks the given errors list. If there are errors, composes a message and throws an exception.
     * 
     * @param errors the errors to check for
     * @throws ExecutionException an exception if there are errors
     */
    private void checkErrors(List<String> errors) throws ExecutionException {
        if (errors.size() > 0) {
            String result = "";
            for (String s : errors) {
                if (result.length() > 0) {
                    result += "\n";
                }
                result += s;
            }
            throw new ExecutionException(result, null);
        }
    }
    
    /**
     * Waits for completing a deployer operation.
     * 
     * @param id the service id to wait for
     * @param initState the initial deployment state, may be <b>null</b> for none
     * @param endCond the end condition when to stop waiting, anyway at longest 
     *   {@link SpringCloudServiceConfiguration#getWaitingTime()}.
     * @return the service state at the end of waiting
     */
    private DeploymentState waitFor(String id, DeploymentState initState, Predicate<DeploymentState> endCond) {
        AppDeployer deployer = getDeployer();
        int waitingTime = getConfig().getWaitingTime();
        long start = System.currentTimeMillis();
        DeploymentState state = null;
        do {
            if (endCond.test(state)) { // preliminary, always synchronous deployment
                state = deployer.status(id).getState();
                TimeUtils.sleep(500);
            }
            if (System.currentTimeMillis() - start > waitingTime) {
                break;
            }
        } while (endCond.test(state));
        return state;
    }

    @Override
    public void stopService(String... serviceIds) throws ExecutionException {
        List<String> errors = new ArrayList<>();
        AppDeployer deployer = getDeployer();
        LOGGER.info("Stopping services " + Arrays.toString(serviceIds));
        // TODO add/check causes for failing
        for (String ids : sortByDependency(serviceIds, false)) {
            SpringCloudServiceDescriptor service = getService(ids);
            String id = service.getDeploymentId();
            service.detachStub();
            if (null != id) {
                AppStatus status = deployer.status(id);
                if (null != status) {
                    DeploymentState state = status.getState();
                    if (state == DeploymentState.deployed) {
                        setState(service, ServiceState.STOPPING);
                        LOGGER.info("Stopping " + id + "... ");
                        deployer.undeploy(id);
                        state = waitFor(id, state, s -> DeploymentState.deployed == s);
                        LOGGER.info("Stopping " + id + "... ");
                        if (null == state || state == DeploymentState.undeployed) {
                            setState(service, ServiceState.STOPPED);
                        } else if (state == DeploymentState.error || state == DeploymentState.failed) {
                            setState(service, ServiceState.FAILED);
                        }
                    } else {
                        setState(service, ServiceState.STOPPED);
                    }
                }
            } else {
                setState(service, ServiceState.STOPPED);
            }
        }
        checkErrors(errors);
        LOGGER.info("Stopped services " + Arrays.toString(serviceIds));
    }

    @Override
    public void migrateService(String serviceId, String resourceId) throws ExecutionException {
        super.migrateService(serviceId, resourceId);
        throw new ExecutionException("not implemented", null);  // TODO must change host value in AAS!
    }

    @Override
    public void removeArtifact(String artifactId) throws ExecutionException {
        LOGGER.info("Removing artifact " + artifactId);
        checkId(artifactId, "artifactId");
        SpringCloudArtifactDescriptor desc = getArtifact(artifactId);
        super.removeArtifact(artifactId);
        // desc cannot be null otherwise exception by parent method
        // proper construction shall not leave jar null
        File jar = desc.getJar();
        File downloadDir = getConfig().getDownloadDir();
        if (null != downloadDir && jar.toPath().startsWith(downloadDir.toPath())) {
            if (getConfig().getDeleteArtifacts()) {
                FileUtils.deleteQuietly(desc.getJar());
            }
        }
        LOGGER.info("Removed artifact " + artifactId);
    }

    @Override
    public void updateService(String serviceId, URI location) throws ExecutionException {
        throw new ExecutionException("not implemented", null);  // TODO
    }

    @Override
    public void switchToService(String serviceId, String target) throws ExecutionException {
        super.switchToService(serviceId, target);
        throw new ExecutionException("not implemented", null); // TODO
    }

    @Override
    public void cloneArtifact(String artifactId, URI location) throws ExecutionException {
        throw new ExecutionException("not implemented", null);  // TODO
    }

    @Override
    protected void setState(ServiceDescriptor service, ServiceState state) throws ExecutionException {
        ServiceState old = service.getState();
        // must be done before setState (via stub), synchronous for now required on Jenkins/Linux
        ServicesAas.notifyServiceStateChanged(old, state, service, NotificationMode.SYNCHRONOUS); 
        service.setState(state);
    }

}
