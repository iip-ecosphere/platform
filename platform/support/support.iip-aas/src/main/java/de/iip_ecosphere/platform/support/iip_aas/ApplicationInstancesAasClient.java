/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;

/**
 * A client to access the application instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ApplicationInstancesAasClient extends SubmodelClient {

    /**
     * Creates a client instance.
     * 
     * @throws IOException if accessing the AAS/submodel fails
     */
    public ApplicationInstancesAasClient() throws IOException {
        super(ActiveAasBase.getSubmodel(ApplicationInstanceAasConstructor.NAME_SUBMODEL_APPINSTANCES));
    }

    /**
     * Return the number of application instances with the given {@code appId}.
     * 
     * @param appId the application id to look for
     * @return the number of instances
     */
    public int getInstanceCount(String appId) {
        return ApplicationInstanceAasConstructor.countAppInstances(appId, getSubmodel());
    }

}
