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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configures the service manager.
 * 
 * @author Holger Eichelberger, SSE
 */
@ConfigurationProperties(prefix = "service-mgr")
@Component
public class SpringCloudServiceConfiguration {

    private String brokerHost = "localhost";
    private int brokerPort = 8883;
    private boolean deleteArtifacts = true;
    private File downloadDir;
    private String descriptorName = "deployment.yml";
    private int waitingTime = 30000;

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
     * @return downloadDir the download directory (if <b>null</b> use temporary directory/files)
     */
    public File getDownloadDir() {
        return downloadDir;
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
     * Defines the name of the descriptor file to load from an artifact.
     * 
     * @param descriptorName the name of the descriptor file (ignored if <b>null</b> or empty)
     */
    public void setDescriptorName(String descriptorName) {
        if (null != descriptorName && descriptorName.length() > 0) {
            this.descriptorName = descriptorName;
        }
    }

    /**
     * Defines the waiting time for longer operations, e.g., deploying a service.
     * 
     * @param waitingTime the waiting time in ms
     */
    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

}
