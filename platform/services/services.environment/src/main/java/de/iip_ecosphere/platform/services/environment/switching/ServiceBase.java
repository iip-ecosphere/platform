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

package de.iip_ecosphere.platform.services.environment.switching;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.services.environment.ServiceState;

/**
 * Just the very basics needed to do service switching. 
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServiceBase {

    public static final String APPLICATION_SEPARATOR = "@";
    public static final String DEFAULT_APPLICATION_INSTANCE_ID = "dflt";
    
    /**
     * Returns the unique id of the service. May be a single service id, a service id and a postfixed application 
     * id (after {@link #APPLICATION_SEPARATOR}) or a service id with application id and postfixed application instance 
     * id (after {@link #APPLICATION_SEPARATOR})
     * 
     * @return the unique id
     */
    public String getId();

    /**
     * Returns the state the service is currently in. [R4c]
     * 
     * @return the state
     */
    public ServiceState getState();

    /**
     * Changes the state. [R133c]
     * 
     * @param state the new state
     * @throws ExecutionException if changing the state fails for some reason
     */
    public void setState(ServiceState state) throws ExecutionException;

    /**
     * Returns the service id from an (internal) id.
     * 
     * @param id the id to split
     * @return the service id, may be {@code id} if there is no application id
     * 
     * @see #composeId(String, String)
     * @see #composeId(String, String, String)
     */
    public static String getServiceId(String id) {
        String result;
        int pos = id.indexOf(APPLICATION_SEPARATOR);
        if (pos > 0) {
            result = id.substring(0, pos);
        } else {
            result = id;
        }
        return result;
    }
    
    /**
     * Returns the application id from an (internal) id.
     * 
     * @param id the id to split
     * @return the application id, may be empty if there is none
     * 
     * @see #composeId(String, String)
     * @see #composeId(String, String, String)
     */
    public static String getApplicationId(String id) {
        String result;
        int fPos = id.indexOf(APPLICATION_SEPARATOR);
        int lPos = id.lastIndexOf(APPLICATION_SEPARATOR);
        if (fPos > 0 && lPos > 0 && fPos != lPos) {
            result = id.substring(fPos + 1, lPos);
        } else if (fPos > 0) {
            result = id.substring(fPos + 1);
        } else {
            result = "";
        }
        return result;
    }

    /**
     * Returns the application instance id from an (internal) id.
     * 
     * @param id the id to split
     * @return the application id, may be empty if there is none
     * 
     * @see #composeId(String, String)
     * @see #composeId(String, String, String)
     */
    public static String getApplicationInstanceId(String id) {
        String result;
        int fPos = id.indexOf(APPLICATION_SEPARATOR);
        int lPos = id.lastIndexOf(APPLICATION_SEPARATOR);
        if (fPos > 0 && lPos > 0 && fPos != lPos) {
            result = id.substring(lPos + 1);
        } else {
            result = "";
        }
        return result;
    }
    
    /**
     * Validates an application instance id.
     * 
     * @param appInstanceId the id to validate
     * @return the validated id, if {@code appInstanceId} would be <b>null</b> or empty, 
     *    {@link #DEFAULT_APPLICATION_INSTANCE_ID} is returned
     */
    public static String validateApplicationInstanceId(String appInstanceId) {
        String result = appInstanceId;
        if (appInstanceId == null || appInstanceId.length() == 0) {
            result = DEFAULT_APPLICATION_INSTANCE_ID;
        }
        return result;
    }

    /**
     * Composes an (internal) id from a service and an application id.
     * 
     * @param serviceId the service id
     * @param applicationId the application id, may be <b>null</b> or empty for none, {@link #APPLICATION_SEPARATOR} in 
     *     {@code applicationId} will be removed
     * @return the composed id
     */
    public static String composeId(String serviceId, String applicationId) {
        return composeId(serviceId, applicationId, "");
    }

    /**
     * Composes an (internal) id from a service and an application id.
     * 
     * @param serviceId the service id, {@link #APPLICATION_SEPARATOR} in 
     *     {@code serviceId} will be removed
     * @param applicationId the application id, may be <b>null</b> or empty for none, {@link #APPLICATION_SEPARATOR} in 
     *     {@code applicationId} will be removed
     * @param applicationInstanceId the application instance id, may be <b>null</b> or empty for none, used 
     *     only if {@code applicationId} is given, {@link #APPLICATION_SEPARATOR} in 
     *     {@code applicationInstanceId} will be removed
     * @return the composed id
     */
    public static String composeId(String serviceId, String applicationId, String applicationInstanceId) {
        String result;
        if (null != applicationId && applicationId.length() > 0) {
            result = serviceId.replace(APPLICATION_SEPARATOR, "");
            result += APPLICATION_SEPARATOR + applicationId.replace(APPLICATION_SEPARATOR, "");
            if (null != applicationInstanceId && applicationInstanceId.length() > 0) {
                result += APPLICATION_SEPARATOR + applicationInstanceId.replace(APPLICATION_SEPARATOR, "");
            }
        } else {
            result = serviceId.replace(APPLICATION_SEPARATOR, "");
        }
        return result;
    }
    
}
