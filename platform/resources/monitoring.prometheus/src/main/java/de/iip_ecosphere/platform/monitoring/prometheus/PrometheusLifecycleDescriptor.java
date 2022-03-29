package de.iip_ecosphere.platform.monitoring.prometheus;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.monitoring.prometheus.util.PrometheusFileUtils;
import de.iip_ecosphere.platform.services.environment.AbstractProcessService;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;

public class PrometheusLifecycleDescriptor implements LifecycleDescriptor {
    
    private Process prometheusProcess;  
    private File prometheusWorkingDirectory;
    private File prometheusFile;
    private String exeName;
    private Path fromBinary;
    private Path toBinary;
    private Path fromYml;
    private Path toYml;
    private InputStream in;
    private InputStream inYml;
    private ProcessBuilder prometheusProcessBuilder;
    //Only needed in development, determines which copy-function to use.
    private boolean inProduction = false;
     
    @Override
    public void startup(String[] args) {
        //Only unzip the binary needed
        if (SystemUtils.IS_OS_WINDOWS) {
            PrometheusFileUtils.unzip(
                    PrometheusProjectConstants.PROMETHEUS_ZIP_WINDOWS, 
                    PrometheusProjectConstants.PROMETHEUS_BINARY_WINDOWS);
        } else if (SystemUtils.IS_OS_LINUX) {
            PrometheusFileUtils.unzip(
                    PrometheusProjectConstants.PROMETHEUS_ZIP_LINUX, 
                    PrometheusProjectConstants.PROMETHEUS_BINARY_LINUX);
        } else {
            LoggerFactory
            .getLogger(PrometheusLifecycleDescriptor.class)
                .info("Other OS other than Windows and Linux not supported.");
        }
        exeName = AbstractProcessService.getExecutableName(
                PrometheusProjectConstants.PROMETHEUS, 
                PrometheusProjectConstants.PROMETHEUS_VERSION);
        //Set working directory in a temporary directory
        prometheusWorkingDirectory = FileUtils.createTmpFolder("iip-prometheus");
        //Storing the paths of both files (inside project and tmp-folder) for better readability.
        fromBinary = Paths.get(PrometheusProjectConstants.PROMETHEUS_BINARY_WINDOWS, exeName);            
        toBinary = Paths.get(prometheusWorkingDirectory.getAbsolutePath(), exeName);
        fromYml = Paths.get(
                PrometheusProjectConstants.PROMETHEUS_BINARY_WINDOWS, 
                PrometheusProjectConstants.PROMETHEUS_CONFIG);                
        toYml = Paths.get(
                prometheusWorkingDirectory.getAbsolutePath(), 
                PrometheusProjectConstants.PROMETHEUS_CONFIG);
        try {
            //Currently fallback methods, in production filesCopy()-method should be used.
            if (inProduction) {
                in = getClass().getClassLoader().getResourceAsStream(exeName);
                inYml = getClass().getClassLoader().getResourceAsStream(PrometheusProjectConstants.PROMETHEUS_CONFIG);
                PrometheusFileUtils.filesCopy(in, inYml, prometheusWorkingDirectory, exeName);
            } else {
                PrometheusFileUtils.copy(fromBinary.toFile(), toBinary.toFile());
                PrometheusFileUtils.copy(fromYml.toFile(), toYml.toFile());                
            }  
            prometheusFile = new File(prometheusWorkingDirectory, exeName);
            //Making the binary executable, only needed for linux.
            prometheusFile.setExecutable(true);
            //Using the process builder to create the process
            prometheusProcessBuilder = new ProcessBuilder(prometheusFile.getAbsolutePath(),
                    "--config.file=prometheus.yml");
            prometheusProcessBuilder.directory(prometheusWorkingDirectory);
            prometheusProcessBuilder.inheritIO();
            //Starting prometheus
            prometheusProcess = prometheusProcessBuilder.start();
            LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class)
                .info(PrometheusProjectConstants.PROMETHEUS
                        + " " +  PrometheusProjectConstants.PROMETHEUS_VERSION + " started");
        } catch (IOException e) {
            LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class).error(e.getMessage(), e);
        }
    } 
    /**
     * Deletes all files used in prometheus run.
     */
    public void deleteWorkingFiles() {
        try {
            PrometheusFileUtils.deleteFile(fromBinary.toFile());
            PrometheusFileUtils.deleteFile(toBinary.toFile());
            PrometheusFileUtils.deleteFile(toYml.toFile());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void shutdown() {
        prometheusProcess.destroyForcibly();
        LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class)
            .info(PrometheusProjectConstants.PROMETHEUS 
                    + " " +  PrometheusProjectConstants.PROMETHEUS_VERSION + " shutdown");
    }
    @Override
    public Thread getShutdownHook() {
        return null;
    }
    @Override
    public int priority() {
        return LifecycleDescriptor.INIT_PRIORITY;
    }
}
