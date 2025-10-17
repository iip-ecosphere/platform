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

package de.iip_ecosphere.platform.services.spring;

import static de.iip_ecosphere.platform.services.spring.SpringInstances.getConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServicesAas;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.spring.descriptor.Endpoint;
import de.iip_ecosphere.platform.services.spring.descriptor.Relation;
import de.iip_ecosphere.platform.services.spring.descriptor.Validator;
import de.iip_ecosphere.platform.services.spring.loader.AppStarter;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.services.spring.yaml.YamlProcess;
import de.iip_ecosphere.platform.services.spring.yaml.YamlService;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.ZipUtils;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.setup.CmdLine;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Descriptor and artifact utility functions that may be used standalone.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DescriptorUtils {
    
    /**
     * Calls {@link #setState(ServiceDescriptor, ServiceState)} logging exceptions.
     * 
     * @param service the service to change
     * @param state the new state
     */
    public static void setStateSafe(ServiceDescriptor service, ServiceState state) {
        try {
            setState(service, state);
        } catch (ExecutionException e) {
            LoggerFactory.getLogger(DescriptorUtils.class).warn("While setting service {} state: {}", 
                service.getId(), e.getMessage());
        }
    }

    /**
     * Changes the service state and notifies {@link ServicesAas}.
     * 
     * @param service the service
     * @param state the new state
     * @throws ExecutionException if changing the state fails
     */
    public static void setState(ServiceDescriptor service, ServiceState state) throws ExecutionException {
        ServiceState old = service.getState();
        // must be done before setState (via stub), synchronous for now required on Jenkins/Linux
        ServicesAas.notifyServiceStateChanged(old, state, service, NotificationMode.SYNCHRONOUS); 
        service.setState(state);
        // if service made an implicit transition, take up and notify
        ServiceState further = service.getState();
        if (further == null) { // fallback, service does not respond
            further = state;
        }
        if (further != state) {
            ServicesAas.notifyServiceStateChanged(state, further, service, NotificationMode.SYNCHRONOUS); 
        }
    }
    
    /**
     * Reads the YAML deployment descriptor from {@code file}.
     * 
     * @param file the file to read from
     * @return the parsed descriptor
     * @throws ExecutionException if reading fails for some reason
     */
    public static YamlArtifact readFromFile(File file) throws ExecutionException {
        YamlArtifact result = null;
        if (file.getName().endsWith(".jar") || file.getName().endsWith(".zip")) {
            try {
                String descName = getDescriptorName();
                getLogger().info("Reading artifact " + file + ", descriptor " + descName);
                InputStream descStream = ZipUtils.findFile(file, "BOOT-INF/classes/" + descName);
                if (null == descStream) {
                    descStream = ZipUtils.findFile(file, "BOOT-INF/classes-app/" + descName);
                }
                if (null == descStream) {
                    descStream = ZipUtils.findFile(file, descName);                    
                }
                if (null != descStream) {
                    result = YamlArtifact.readFromYaml(descStream);
                    FileUtils.closeQuietly(descStream);
                    
                    Validator val = new Validator();
                    val.validate(result);
                    if (val.hasMessages()) {
                        DescriptorUtils.throwExecutionException("Adding " + file, 
                            "Problems in descriptor:\n" + val.getMessages());
                    }
                } else {
                    throwExecutionException("Reading artifact " + file, descName + " does not exist in " + file);
                }
            } catch (IOException e) {
                throwExecutionException("Reading artifact " + file, e);
            }
        } else {
            throwExecutionException("Reading artifact " + file, file + " is not considered as service "
                + "artifact (JAR, ZIP)");
        }
        return result;
    }

    /**
     * Returns the deployment descriptor file name to use.
     * 
     * @return the descriptor file name
     */
    private static String getDescriptorName() {
        String descName = "deployment.yml";
        if (null != getConfig()) { // null in case of standalone/non-spring execution
            descName = getConfig().getDescriptorName();
        }
        return descName;
    }
    
    /**
     * Reads the YAML deployment descriptor from {@code file}.
     * 
     * @return the parsed descriptor
     * @throws ExecutionException if reading fails for some reason
     */
    public static YamlArtifact readFromClasspath() throws ExecutionException {
        YamlArtifact result = null;
        String descName = getDescriptorName();
        InputStream descStream = ResourceLoader.getResourceAsStream(DescriptorUtils.class, descName);
        if (null != descStream) {
            try {
                result = YamlArtifact.readFromYaml(descStream);
            } catch (IOException e) {
                throwExecutionException("Reading deployment descriptor " + descName, e);
            }
            FileUtils.closeQuietly(descStream);
        } else {
            throwExecutionException("Reading deployment descriptor", descName + " not found on classpath");
        }
        return result;
    }

    /**
     * Throws an execution exception for the given throwable and logs an error in the same step.
     * 
     * @param action the actual action to log
     * @param th the throwable
     * @throws ExecutionException the exception based on {@code th}
     */
    public static void throwExecutionException(String action, Throwable th) throws ExecutionException {
        getLogger().error(action + ": " + th.getMessage());
        throw new ExecutionException(th);
    }
    
    /**
     * Throws an execution exception for the given message and logs an error in the same step.
     * 
     * @param action the actual action to log
     * @param message the message for the exception
     * @throws ExecutionException the exception based on {@code message}
     */
    public static void throwExecutionException(String action, String message) throws ExecutionException {
        getLogger().error(action + ": " + message);
        throw new ExecutionException(message, null);
    }
    
    /**
     * Adds commandline args for a given {@code endpoint}.
     * 
     * @param cmdLine the command line arguments to modify as a side effect
     * @param endpoint the endpoint to turn into command line arguments
     * @param addr the address containing port number and host (for substitution in results delivered 
     *     by {@code endpoint})
     */
    public static void addEndpointArgs(List<String> cmdLine, Endpoint endpoint, ServerAddress addr) {
        addEndpointArgs(cmdLine, endpoint, addr.getPort(), addr.getHost());
    }

    /**
     * Adds commandline args for a given {@code endpoint}.
     * 
     * @param cmdLine the command line arguments to modify as a side effect
     * @param endpoint the endpoint to turn into command line arguments
     * @param port the port number (for substitution in results delivered by {@code endpoint})
     * @param host the host name (for substitution in results delivered by {@code endpoint})
     */
    public static void addEndpointArgs(List<String> cmdLine, Endpoint endpoint, int port, String host) {
        if (null != endpoint) { // endpoints are optional
            CmdLine.parseToArgs(endpoint.getPortArg(port), cmdLine);
            if (endpoint.getHostArg().length() > 0) {
                CmdLine.parseToArgs(endpoint.getHostArg(host), cmdLine);
            }
        }
    }

    /**
     * Creates command line args for executing the (Spring) fat JAR in standalone/debugging manner requesting
     * the spring web server to be executed on an ephemeral port by default.
     *   
     * @param jar the JAR file to read
     * @param brokerPort the port where the transport broker is running 
     * @param brokerHost the host where the transport broker is running (usually "localhost")
     * @param adminPort the port where to run the AAS command server, for -1 an emphemeral port will be used 
     * @param serviceProtocol the protocol to run for the AAS command server (see {@link AasFactory})
     * @return the list of command line args to use 
     * @throws IOException if {@code jar} cannot be found or reading {@code jar} fails
     * @throws ExecutionException if extracting process artifacts fails
     */
    public static List<String> createStandaloneCommandArgs(File jar, int brokerPort, String brokerHost, 
        int adminPort, String serviceProtocol) throws IOException, ExecutionException {
        return createStandaloneCommandArgs(jar, brokerPort, brokerHost, 
            adminPort, serviceProtocol, -1);
    }
    
    // checkstyle: stop parameter number check
    
    /**
     * Creates command line args for executing the (Spring) fat JAR in standalone/debugging manner.
     *   
     * @param jar the JAR file to read
     * @param brokerPort the port where the transport broker is running 
     * @param brokerHost the host where the transport broker is running (usually "localhost")
     * @param adminPort the port where to run the AAS command server, for -1 an emphemeral port will be used 
     * @param serviceProtocol the protocol to run for the AAS command server (see {@link AasFactory})
     * @param springPort the port to run the spring application server on, usually 8080, if negative an ephemeral 
     *     port will be used
     * @return the list of command line args to use 
     * @throws IOException if {@code jar} cannot be found or reading {@code jar} fails
     * @throws ExecutionException if extracting process artifacts fails
     */
    public static List<String> createStandaloneCommandArgs(File jar, int brokerPort, String brokerHost, 
        int adminPort, String serviceProtocol, int springPort) throws IOException, ExecutionException {
        if (springPort < 0) {
            springPort = NetUtils.getEphemeralPort();
        }
        List<String> result;
        if (!jar.exists()) {
            throw new IOException("Cannot find Spring service binary '" + jar.getAbsolutePath() 
                + "'. Did you run the instantiation process?");
        } 
        // This shall not occur in normal applications. Usually, we do not know what the service execution
        // is. Here we rely on spring, also because the descriptors are not yet abstracted. 
        YamlArtifact art = readFromFile(jar);
        result = new ArrayList<String>();
        result.add("java");
        result.add("-jar");
        result.add("--add-opens=java.base/java.lang=ALL-UNNAMED"); // JDK 17
        Starter.addAppEnvironment(result);
        result.add(jar.getAbsolutePath());
        result.add("--" + Starter.PARAM_IIP_TEST_SERVICE_AUTOSTART + "=true"); // only for testing
        result.add("--" + Starter.PARAM_IIP_TEST_TRANSPORT_PORT + "=" + brokerPort); // only for testing
        result.add("--amqp.port" + "=" + brokerPort); // overwrite local default
        result.add("--mqtt.port" + "=" + brokerPort); // overwrite local default
        result.add("--server.port=" + springPort);
        List<String> tmp = new ArrayList<String>();
        for (YamlService service : art.getServices()) {
            if (service.getKind() != ServiceKind.SERVER) {
                YamlProcess proc = service.getProcess();
                if (null != proc) {
                    File d = Starter.extractProcessArtifacts(service.getId(), proc, jar, null);
                    FileUtils.deleteOnExit(d);
                }
                for (Relation r : service.getRelations()) {
                    // simplification, don't think about relations
                    DescriptorUtils.addEndpointArgs(tmp, r.getEndpoint(), brokerPort, brokerHost);
                }
                tmp.addAll(service.getCmdArg(adminPort, serviceProtocol));
            }
        }
        Set<String> tmp2 = new HashSet<String>(tmp); // spring deployment does this implicitly via requests
        result.addAll(tmp2);
        return result;
    }

    // checkstyle: resume parameter number check
    // checkstyle: stop exception type check

    /**
     * Creates a class loader for a JAR file, in particular an executable Spring-packaged Jar file.
     * 
     * @param jarFile the jar file
     * @return the class loader
     * @throws Exception if the class loader cannot be constructed
     */
    public static ClassLoader createClassLoader(File jarFile) throws Exception {
        // reuse spring launcher, they know how to create the class loader
        return AppStarter.createClassloader(jarFile);
    }

    // checkstyle: resume exception type check

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(DescriptorUtils.class);
    }

}
