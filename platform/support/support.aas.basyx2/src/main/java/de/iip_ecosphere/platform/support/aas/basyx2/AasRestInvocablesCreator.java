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

package de.iip_ecosphere.platform.support.aas.basyx2;

import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;

import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.SetupSpec;

/**
 * implements an invocables creator for AAS-REST.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasRestInvocablesCreator extends AbstractAasRestInvocablesCreator {

    private static final long serialVersionUID = 4353249016693669189L;
    private String id;
    private SetupSpec spec;
    private ConnectedSubmodelRepository smRepo = null;
    private String schemaPrefix;
    
    /**
     * Creates an invocables creator instance.
     * 
     * @param spec the setup specification
     */
    AasRestInvocablesCreator(SetupSpec spec) {
        this.spec = spec;
        ServerAddress addr = spec.getAssetServerAddress();
        schemaPrefix = addr.getSchema().toUri();
        if (null == schemaPrefix || schemaPrefix.length() == 0) {
            schemaPrefix = "http://"; // fallback, also for VAB tests; VAB specified as Schema.IGNORE
        }
        id = addr.getHost() + ":" + addr.getPort();
    }

    @Override
    protected String getId() {
        return id;
    }

    @Override
    protected String composeUrl(String suffix) {
        return schemaPrefix + id + "/" + suffix;
    }

    @Override
    protected ConnectedSubmodelRepository getSubmodelRepository() {
        if (null == smRepo) {
            //smRepo = new ConnectedSubmodelRepository(spec.getSubmodelRepositoryEndpoint().toServerUri());
            smRepo = SubmodelRepositoryUtils.createRepositoryApi(spec, 
                spec.getSubmodelRepositoryEndpoint().toServerUri());
        }
        return smRepo;
    }

    @Override
    protected String getSubmodelRepositoryUrl() {
        return null != spec.getSubmodelRepositoryEndpoint() ? spec.getSubmodelRepositoryEndpoint().toServerUri() : null;
    }

}
