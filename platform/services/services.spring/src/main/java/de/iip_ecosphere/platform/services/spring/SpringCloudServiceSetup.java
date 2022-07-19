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
import java.util.HashMap;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import de.iip_ecosphere.platform.services.ServiceSetup;
import de.iip_ecosphere.platform.support.CollectionUtils;

/**
 * Configures the service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
@ConfigurationProperties(prefix = "service-mgr")
@Component
public class SpringCloudServiceSetup extends ServiceSetup {

    private String brokerHost = "localhost";
    private int brokerPort = 8883;
    private boolean deleteArtifacts = true;
    private File downloadDir;
    private String descriptorName = "deployment.yml";
    private int waitingTime = 120000;
    private int availabilityRetryDelay = 500;
    private HashMap<String, String> executables = new HashMap<String, String>();
    private List<String> javaOpts = CollectionUtils.toList("-Dlog4j2.formatMsgNoLookups=true"); 
    private File sharedLibs = new File("/shared"); // preliminary default

    /**
     * Returns the name of the broker host.
     * 
     * @return the host name (usually "localhost")
     */
    public String getBrokerHost() {
        return brokerHost;
    }
    
    /**
     * Returns the broker port.
     * 
     * @return the broker port (by default 8883)
     */
    public int getBrokerPort() {
        return brokerPort;
    }

    /**
     * Returns whether artifacts may be deleted.
     * 
     * @return {@code true} for delete, {@code false} else
     */
    public boolean getDeleteArtifacts() {
        return deleteArtifacts;
    }

    /**
     * Returns the download directory for artifacts.
     * 
     * @return the download directory (if <b>null</b> use temporary directory/files)
     */
    public File getDownloadDir() {
        return downloadDir;
    }

    /**
     * Returns the directory for shared service libraries.
     * 
     * @return the shared library directory (if <b>null</b> or empty for none)
     */
    public File getSharedLibs() {
        return sharedLibs;
    }

    /**
     * Returns the name of the descriptor file to load from an artifact.
     * 
     * @return the name of the descriptor file
     */
    public String getDescriptorName() {
        return descriptorName;
    }

    /**
     * Returns the waiting time for longer operations, e.g., deploying a service.
     * 
     * @return the waiting time in ms
     */
    public int getWaitingTime() {
        return waitingTime;
    }
    
    /**
     * Returns the delay between two availability checks.
     * 
     * @return the retry delay in ms
     */
    public int getAvailabilityRetryDelay() {
        return availabilityRetryDelay;
    }

    /**
     * Returns the configured value for an executable, i.e., either from the configured executables mapping or the
     * {@code Executable}. The idea here is to allow the configuration to override commands that are available via
     * the OS path in certain cases.
     * 
     * @param executable the executable to look for
     * @return either {@code executable} or the mapped executable value
     */
    public String getExecutable(String executable) {
        String result = executables.get(executable);
        if (null == result) {
            result = executable;
        }
        return result;
    }
    
    /**
     * Returns the Java command line options.
     * 
     * @return the command line options (disables by default log4j format lookups)
     */
    public List<String> getJavaOpts() {
        return javaOpts;
    }

    /**
     * Defines the name of the broker host. [required by Spring]
     * 
     * @param brokerHost the host name 
     */
    public void setBrokerHost(String brokerHost) {
        this.brokerHost = brokerHost;
    }

    /**
     * Defines the broker port. [required by Spring]
     * 
     * @param brokerPort the broker port 
     */
    public void setBrokerPort(int brokerPort) {
        this.brokerPort = brokerPort;
    }
   
    /**
     * Defines whether artifacts may be deleted. [required by Spring]
     * 
     * @param deleteArtifacts {@code true} for delete, {@code false} else
     */
    public void setDeleteArtifacts(boolean deleteArtifacts) {
        this.deleteArtifacts = deleteArtifacts;
    }
   
    /**
     * Defines the download directory for artifacts. [required by Spring]
     * 
     * @param downloadDir the download directory (if <b>null</b> use temporary directory/files)
     */
    public void setDownloadDir(File downloadDir) {
        this.downloadDir = downloadDir;
    }

    /**
     * Changes the directory for shared service libraries.
     * 
     * @param sharedLibs the shared library directory (if <b>null</b> or empty for none)
     */
    public void setSharedLibs(File sharedLibs) {
        this.sharedLibs = sharedLibs;
    }

    /**
     * Defines the name of the descriptor file to load from an artifact. [required by Spring]
     * 
     * @param descriptorName the name of the descriptor file (ignored if <b>null</b> or empty)
     */
    public void setDescriptorName(String descriptorName) {
        if (null != descriptorName && descriptorName.length() > 0) {
            this.descriptorName = descriptorName;
        }
    }

    /**
     * Defines the waiting time for longer operations, e.g., deploying a service. [required by Spring]
     * 
     * @param waitingTime the waiting time in ms
     */
    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    /**
     * Changes the delay between two availability checks. [required by Spring]
     * 
     * @param availabilityRetryDelay the retry delay in ms
     */
    public void setAvailabilityRetryDelay(int availabilityRetryDelay) {
        this.availabilityRetryDelay = availabilityRetryDelay;
    }
    
    /**
     * Defines the executables mapping, i.e., an optional mapping of OS command names to paths or other command names
     * that are available on the respective target system. [required by Spring]
     * 
     * @param executables the executables mapping
     */
    public void setExecutables(HashMap<String, String> executables) {
        this.executables = executables;
    }
    
    /**
     * Sets the Java command line options. [required by Spring]
     * 
     * @param javaOpts the command line options
     */
    public void setJavaOpts(List<String> javaOpts) {
        this.javaOpts = javaOpts;
    }

}
