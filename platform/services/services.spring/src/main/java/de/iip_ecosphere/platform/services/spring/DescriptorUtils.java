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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.ResourceLoader;
import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.spring.descriptor.Endpoint;
import de.iip_ecosphere.platform.services.spring.descriptor.ProcessSpec;
import de.iip_ecosphere.platform.services.spring.descriptor.Relation;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.services.spring.yaml.YamlProcess;
import de.iip_ecosphere.platform.services.spring.yaml.YamlService;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.iip_aas.config.CmdLine;

/**
 * Descriptor and artifact utility functions that may be used standalone.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DescriptorUtils {

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
                InputStream descStream = JarUtils.findFile(new FileInputStream(file), "BOOT-INF/classes/" + descName);
                if (null == descStream) {
                    descStream = JarUtils.findFile(new FileInputStream(file), descName);                    
                }
                if (null != descStream) {
                    result = YamlArtifact.readFromYaml(descStream);
                    FileUtils.closeQuietly(descStream);
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
        InputStream descStream = DescriptorUtils.class.getResourceAsStream("/BOOT-INF/classes/" + descName);
        if (null == descStream) {
            descStream = DescriptorUtils.class.getResourceAsStream("/" + descName);
        }
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
     * Throws an execution exception for the given throwable.
     * @param action the actual action to log
     * @param th the throwable
     * @throws ExecutionException
     */
    public static void throwExecutionException(String action, Throwable th) throws ExecutionException {
        getLogger().error(action + ": " + th.getMessage());
        throw new ExecutionException(th);
    }
    
    /**
     * Throws an execution exception for the given message.
     * @param action the actual action to log
     * @param message the message for the exception
     * @throws ExecutionException
     */
    public static void throwExecutionException(String action, String message) throws ExecutionException {
        getLogger().error(action + ": " + message);
        throw new ExecutionException(message, null);
    }
    
    /**
     * Extracts artifacts that are required for a service being realized of external processes.
     * 
     * @param sId the service id
     * @param pSpec the process specification
     * @param artFile the ZIP/JAR service artifact
     * @param processBaseDir the base directory to be used to create a process home directory within if 
     *     {@link ProcessSpec#getHomePath()} is <b>null</b> 
     * @return the folder into which the process has been extracted. May be {@link ProcessSpec#getHomePath()} or
     *     a temporary directory.
     * @throws IOException if accessing files fails
     */
    public static File extractProcessArtifacts(String sId, ProcessSpec pSpec, File artFile, File processBaseDir) 
        throws IOException {
        // take over / create process home dir
        File processDir = pSpec.getHomePath();
        if (null == processDir) {
            processDir = new File(processBaseDir, Starter.normalizeServiceId(sId) + "-" + System.currentTimeMillis());
        }
        if (!pSpec.isStarted()) {
            FileUtils.deleteQuietly(processDir); // unlikely, just to be sure
        }
        processDir.mkdirs();

        // unpack artifacts to home
        for (String artPath : pSpec.getArtifacts()) {
            while (artPath.startsWith("/")) {
                artPath = artPath.substring(1);
            }
            FileInputStream fis = null;
            InputStream artifact = ResourceLoader.getResourceAsStream(DescriptorUtils.class, artPath);
            if (null == artifact) { // spring packaging fallback
                try {
                    fis = new FileInputStream(artFile);
                    artifact = JarUtils.findFile(fis, "BOOT-INF/classes/" + artPath);
                    if (null == artifact) {
                        fis = new FileInputStream(artFile); // TODO preliminary, use predicate 
                        artifact = JarUtils.findFile(fis, artPath);
                    }
                } catch (IOException e) {
                    getLogger().info("Cannot open " + artFile + ": " + e.getMessage());
                }
            }
            if (null == artifact) {
                throw new IOException("Cannot find artifact '" + artPath + "' in actual service JAR");
            }
            JarUtils.extractZip(artifact, processDir.toPath());
            getLogger().info("Extracted process artifact " + artPath + " to " + processDir);
            FileUtils.closeQuietly(artifact);
            FileUtils.closeQuietly(fis);
        }
        return processDir;
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
        result.add("-Dlog4j2.formatMsgNoLookups=true");
        result.add(jar.getAbsolutePath());
        result.add("--" + Starter.PARAM_IIP_TEST_SERVICE_AUTOSTART + "=true"); // only for testing
        result.add("--server.port=" + springPort);
        List<String> tmp = new ArrayList<String>();
        for (YamlService service : art.getServices()) {
            YamlProcess proc = service.getProcess();
            if (null != proc) {
                File d = extractProcessArtifacts(service.getId(), proc, jar, null);
                FileUtils.deleteOnExit(d);
            }
            for (Relation r : service.getRelations()) {
                // simplification, don't think about relations
                DescriptorUtils.addEndpointArgs(tmp, r.getEndpoint(), brokerPort, brokerHost);
            }
            tmp.addAll(service.getCmdArg(adminPort, serviceProtocol));
        }
        Set<String> tmp2 = new HashSet<String>(tmp); // spring deployment does this implicitly via requests
        result.addAll(tmp2);
        return result;
    }

    // checkstyle: resume parameter number check

    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(DescriptorUtils.class);
    }

}
