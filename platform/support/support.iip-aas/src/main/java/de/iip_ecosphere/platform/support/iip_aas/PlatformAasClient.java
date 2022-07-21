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

package de.iip_ecosphere.platform.support.iip_aas;

import static de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.fromJson;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolutionResult;

/**
 * Implemented client for platform nameplate operations.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAasClient extends SubmodelClient implements PlatformClient {

    /**
     * Creates a client instance based on a deployed IIP-AAS from {@link AasPartRegistry} based on the 
     * {@link PlatformAas} submodel. 
     * 
     * @throws IOException if retrieving the IIP-AAS or the respective submodel fails
     */
    public PlatformAasClient() throws IOException {
        super(ActiveAasBase.getSubmodel(PlatformAas.NAME_SUBMODEL));
    }

    @Override
    public String snapshotAas(String id) throws ExecutionException {
        return fromJson(getOperation(PlatformAas.NAME_OPERATION_SNAPSHOTAAS).invoke(id));
    }

    @Override
    public SemanticIdResolutionResult resolveSemanticId(String id) throws ExecutionException {
        String json = fromJson(getOperation(PlatformAas.NAME_OPERATION_RESOLVE_SEMANTICID).invoke(id));
        return JsonUtils.fromJson(json, DefaultSemanticIdResolutionResult.class);
    }

}
