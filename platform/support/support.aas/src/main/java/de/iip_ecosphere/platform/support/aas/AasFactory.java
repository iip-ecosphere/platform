/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas;

import java.io.IOException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.SubModel.SubModelBuilder;

/**
 * A customizable factory for creating AAS instances independent of the underlying implementation.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AasFactory {

    private static final Logger LOGGER = Logger.getLogger(AasFactory.class.getName());
    // instance-based to allow later dependency injection
    private static AasFactory instance = null;
    
    /**
     * Returns the actual instance.
     * 
     * @return the actual instance
     */
    public static AasFactory getInstance() {
        if (null == instance) {
            ServiceLoader<AasFactoryDescriptor> loader = ServiceLoader.load(AasFactoryDescriptor.class);
            Optional<AasFactoryDescriptor> first = loader.findFirst();
            if (first.isPresent()) {
                instance = first.get().createInstance();
                if (null != instance) {
                    LOGGER.fine("AAS factory implementation registered: " + instance.getClass().getName());
                }
            } else {
                LOGGER.severe("No AAS factory implementation known.");
            }
        }
        return instance;
    }
    
    /**
     * Defines the actual instance.
     * 
     * @param newInstance the new instance (may be <b>null</b> but then the call is without effect)
     */
    public static void setInstance(AasFactory newInstance) {
        if (null != newInstance) {
            instance = newInstance;
        }
    }
    
    /**
     * Returns the (descriptive) name of the factory.
     * 
     * @return the name of the factory
     */
    public abstract String getName();

    /**
     * Creates an AAS builder instance.
     * 
     * @param idShort the shortId of the AAS
     * @param urn the uniform resource name of the AAS
     * 
     * @return the AAS builder instance
     * @throws IllegalArgumentException if {@code idShort} or {@code urn} is <b>null</b> or empty
     */
    public abstract AasBuilder createAasBuilder(String idShort, String urn);

    /**
     * Creates a standalone sub-model without parent AAS.
     * 
     * @param idShort the short id of the sub-model
     * @return the sub-model builder
     * @throws IllegalArgumentException if {@code idShort} is <b>null</b> or empty, or if this operation is not 
     *   supported
     */
    public abstract SubModelBuilder createSubModelBuilder(String idShort);
    
    /**
     * Retrieves an AAS.
     * 
     * @param host the host name of the AAS repository
     * @param port the TCP port number of the AAS repository
     * @param endpointPath the registry endpoint path on host
     * @param urn the URN of the AAS
     * @return the AAS
     * @throws IOException if accessing the AAS fails for some reason
     */
    public abstract Aas retrieveAas(String host, int port, String endpointPath, String urn) throws IOException;
    
    /**
     * Creates a deployment builder.
     * 
     * @param host the target host
     * @param port the target IP port
     * @return the deployment builder instance
     */
    public abstract DeploymentBuilder createDeploymentBuilder(String host, int port);

    /**
     * Creates a deployment builder.
     * 
     * @param contextPath the context base path (may be empty, otherwise shall start with a "/")
     * @param host the target host
     * @param port the target IP port
     * @return the deployment builder instance
     */
    public abstract DeploymentBuilder createDeploymentBuilder(String contextPath, String host, int port);
    
}
