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

package de.iip_ecosphere.platform.deviceMgt.ecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.Credentials;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * An AasClient which implements a {@link SubmodelElementsCollectionClient} and provides easy
 * access to the registry functions through the AAS.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class EcsAasClient extends SubmodelElementsCollectionClient {

    public static final String NAME_OP_CREATE_REMOTE_CONNECTION_CREDENTIALS = "createRemoteConnectionCredentials";
    public static final String PROP_NAME_RUNTIME_NAME = "runtimeName";

    /**
     * Creates an ECS AAS client for the given device/resource id.
     * 
     * @param id the device/resource id
     * @throws IOException in case that the AAS cannot be found/connection cannot be established
     */
    public EcsAasClient(String id) throws IOException {
        super(AasPartRegistry.NAME_SUBMODEL_RESOURCES, id);
    }

    /**
     * Creates remote connection credentials.
     * 
     * @return the credentials
     * @throws ExecutionException in case that the credentials cannot be created, the operation fails
     */
    public Credentials createRemoteConnectionCredentials() throws ExecutionException {
        String result = (String) getOperation(NAME_OP_CREATE_REMOTE_CONNECTION_CREDENTIALS).invoke();
        ObjectMapper mapper = new ObjectMapper();
        Credentials credentials = null;
        try {
            credentials = mapper.readValue(result, Credentials.class);
        } catch (JsonProcessingException ignore) {
            // should not happen
        }

        return credentials;
    }

    /**
     * Returns the runtime name of the device.
     * 
     * @return the runtime name
     * @throws ExecutionException in case that the operation call fails for some reason
     */
    public String getRuntimeName() throws ExecutionException {
        return getPropertyStringValue(PROP_NAME_RUNTIME_NAME, "");
    }
}