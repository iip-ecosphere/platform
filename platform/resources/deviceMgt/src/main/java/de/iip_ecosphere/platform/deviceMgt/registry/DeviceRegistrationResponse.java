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

package de.iip_ecosphere.platform.deviceMgt.registry;

/**
 * Represents the response of a device registration process/onboarding.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DeviceRegistrationResponse {
    
    private boolean successful;
    private String message = "";
    
    /**
     * Returns whether the registration was successful.
     * 
     * @return {@code true} for successful, {@code false} else
     */
    public boolean isSuccessful() {
        return successful;
    }
    
    /**
     * Defines whether the registration was successful. [required for JSON]
     * 
     * @param successful {@code true} for successful, {@code false} else
     */
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    /**
     * Returns a message if the registration was not successful.
     * 
     * @return the message, may be <b>null</b>
     */
    public String getMessage() {
        return message;
    }

    /**
     * Defines the message if the registration was not successful. [required for JSON]
     * 
     * @param message the message, may be <b>null</b>
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    // TODO add certificates, tokens

}
