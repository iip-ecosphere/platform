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

package de.iip_ecosphere.platform.deviceMgt.storage;

/**
 * POJO class for the Storage Setup.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class PackageStorageSetup {

    private String endpoint;
    private String region = "";
    private String authenticationKey;
    private String bucket;
    private String prefix;
    private String packageDescriptor;
    private String packageFilename;

    /**
     * Default constructor, used by SnakeYaml.
     */
    public PackageStorageSetup() {
    }

    /**
     * Get the bucket.
     *
     * @return the bucket
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Set the bucket. [required by SnakeYaml]
     *
     * @param bucket the bucket
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * Get the endpoint.
     *
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }
    
    /**
     * Set the endpoint. [required by SnakeYaml]
     *
     * @param endpoint the endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Get the region. The region interpretation depends on the cloud provider. Irrelevant for local
     * installations.
     *
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Returns the region. The region interpretation depends on the cloud provider. Irrelevant for local
     * installations. [required by SnakeYaml]
     *
     * @param region the region (<b>null</b> is turned to into an empty string)
     */
    public void setRegion(String region) {
        this.region = null == region ? "" : region;
    }

    /**
     * Get the authentication key pointing to the identity store.
     *
     * @return the authentication key
     */
    public String getAuthenticationKey() {
        return authenticationKey;
    }

    /**
     * Set the authentication key pointing to the identity store. [required by SnakeYaml]
     *
     * @param authenticationKey the authentication key
     */
    public void setAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    /**
     * Get the prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix. [required by SnakeYaml]
     *
     * @param prefix the prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the packageDescriptor.
     *
     * @return the packageDescriptor
     */
    public String getPackageDescriptor() {
        return packageDescriptor;
    }

    /**
     * Set the packageDescriptor. [required by SnakeYaml]
     *
     * @param packageDescriptor the packageDescriptor
     */
    public void setPackageDescriptor(String packageDescriptor) {
        this.packageDescriptor = packageDescriptor;
    }

    /**
     * Get the packageFilename.
     *
     * @return the packageFilename
     */
    public String getPackageFilename() {
        return packageFilename;
    }

    /**
     * Set the packageFilename. [required by SnakeYaml]
     *
     * @param packageFilename the packageFilename
     */
    public void setPackageFilename(String packageFilename) {
        this.packageFilename = packageFilename;
    }
}
