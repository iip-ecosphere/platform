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
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.Starter;
import de.iip_ecosphere.platform.services.spring.descriptor.ProcessSpec;
import de.iip_ecosphere.platform.services.spring.yaml.YamlArtifact;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;

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
            InputStream artifact = DescriptorUtils.class.getResourceAsStream(artPath);
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
     * Returns the logger.
     * 
     * @return the logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(DescriptorUtils.class);
    }

}
