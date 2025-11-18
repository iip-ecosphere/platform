/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services.environment;

import java.io.InputStream;

import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * A service creation descriptor, in particular for plugins. If you describe a generic MIMO service, implement 
 * {@link #createService(YamlService, Object...)}, for a generic SISO service {@link #createService(TypeTranslator, 
 * TypeTranslator, ReceptionCallback, YamlService, Object...)} and if you describe a specific service implement 
 * {@link #createService(String, InputStream)} as well as the fallbacks {@link #createService(String)} and 
 * {@link #createService()}.
 * 
 * @param <S> the actual type of service being created
 * @author Holger Eichelberger, SSE
 */
public interface ServiceDescriptor <S extends Service> {

    /**
     * Creates a generic service (still to be wrapped) from a YAML service description.
     * 
     * @param yaml the YAML service description
     * @param args generic arguments to be passed to and interpreted by the service
     * @return the service instance, may be <b>null</b> if creation/casting is not possible
     */
    public S createService(YamlService yaml, Object... args);

    /**
     * Creates a generic service (still to be wrapped) from type translators, callback and YAML service description.
     * 
     * @param <I> input type
     * @param <O> output type
     * @param inTrans the input translator
     * @param outTrans the output translator
     * @param callback called when a processed item is received from the service
     * @param yaml the YAML service description
     * @param args generic arguments to be passed to and interpreted by the service
     * @return the service instance, may be <b>null</b> if creation/casting is not possible
     */
    public <I, O> S createService(TypeTranslator<I, String> inTrans, TypeTranslator<String, O> outTrans, 
        ReceptionCallback<O> callback, YamlService yaml, Object... args);
    
    /**
     * Creates a specific service from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     * @return the service instance, may be <b>null</b> if creation/casting is not possible
     */
    public S createService(String serviceId, InputStream ymlFile);

    /**
     * Creates a specific service from a service id, fallback.
     * 
     * @param serviceId the service id
     * @return the service instance, may be <b>null</b> if creation/casting is not possible
     */
    public S createService(String serviceId);

    /**
     * Creates a specific service, second fallback.
     * 
     * @return the service instance, may be <b>null</b> if creation/casting is not possible
     */
    public S createService();

}
