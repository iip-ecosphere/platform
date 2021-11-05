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

package de.iip_ecosphere.platform.deviceMgt;

import java.util.concurrent.ExecutionException;

/**
 * The DeviceRemoteManagementOperations provide access to the devices through a remote connection.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public interface DeviceRemoteManagementOperations {

    /**
     * Creates a ssh connection, so one can connect with a device through ssh.
     *
     * @param id the device id
     * @return SSHConnectionDetails
     * @throws ExecutionException if some (remote) execution errors occur
     */
    SSHConnectionDetails establishSsh(String id) throws ExecutionException;

    /**
     * SSHConnectionDetails provide information about the connection to the remote site.
     * It provides the host and port in combination with a basic auth flow.
     */
    class SSHConnectionDetails {

        private String host;
        private Integer port;
        private String username;
        private String password;

        /**
         * Default constructor, required for Jackson.
         */
        public SSHConnectionDetails() {
        }

        /**
         * All args constructor.
         * 
         * @param host the host
         * @param port the port
         * @param username the username
         * @param password the password
         */
        public SSHConnectionDetails(String host, Integer port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        /**
         * Get the host address.
         * 
         * @return the host
         */
        public String getHost() {
            return host;
        }

        /**
         * Set the host address.
         * 
         * @param host the host
         */
        public void setHost(String host) {
            this.host = host;
        }

        /**
         * Get the host port.
         * 
         * @return the port
         */
        public Integer getPort() {
            return port;
        }

        /**
         * Set the host port.
         * 
         * @param port the port
         */
        public void setPort(Integer port) {
            this.port = port;
        }

        /**
         * Get the username.
         * 
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        /**
         * Set the username.
         * 
         * @param username the username
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * Get the password.
         * 
         * @return the password
         */
        public String getPassword() {
            return password;
        }

        /**
         * Set the password.
         * 
         * @param password the password
         */
        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SSHConnectionDetails that = (SSHConnectionDetails) obj;
            return host.equals(that.host)
                && port.equals(that.port)
                && username.equals(that.username)
                && password.equals(that.password);
        }
        
        @Override
        public int hashCode() {
            return host.hashCode() + Integer.hashCode(port) + username.hashCode() + password.hashCode();
        }
        
    }
    
}
