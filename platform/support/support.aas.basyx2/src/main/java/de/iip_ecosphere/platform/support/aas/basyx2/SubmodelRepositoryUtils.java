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
import org.eclipse.digitaltwin.basyx.submodelrepository.client.internal.SubmodelRepositoryApi;
import org.eclipse.digitaltwin.basyx.submodelservice.client.ConnectedSubmodelService;
import org.eclipse.digitaltwin.basyx.submodelservice.client.internal.SubmodelServiceApi;

import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonMapperFactory;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.SimpleAbstractTypeResolverFactory;
import org.eclipse.digitaltwin.basyx.client.internal.ApiClient;
import org.eclipse.digitaltwin.basyx.client.internal.ApiException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;

/**
 * Supporting methods for submodel repositories.
 * 
 * @author Holger Eichelberger, SSE
 */
class SubmodelRepositoryUtils {

    /**
     * Creates an API instance with default URL.
     * 
     * @param spec the setup specification containing endpoint and keystore descriptor, may be <b>null</b> leading 
     *     to <b>null</b> as result
     * @return the API instance
     */
    static ConnectedSubmodelRepository createRepositoryApi(SetupSpec spec) {
        return null == spec ? null : createRepositoryApi(spec, null);
    }

    /**
     * Creates an API instance.
     * 
     * @param spec the setup specification containing endpoint and keystore descriptor
     * @param uri specific URI, may be <b>null</b> for {@code endpoint}
     * @return the API instance
     */
    static ConnectedSubmodelRepository createRepositoryApi(SetupSpec spec, String uri) {
        return Tools.createApi(spec.getSetup(AasComponent.SUBMODEL_REPOSITORY), 
            uri, new ApiClient().setObjectMapper(
                new JsonMapperFactory().create(new SimpleAbstractTypeResolverFactory().create())), 
            (b, c, i) -> c.setHttpClientBuilder(b).setRequestInterceptor(i), 
            (u, c) -> c.updateBaseUri(u), 
            (u, c) -> new ExtendedConnectedSubmodelRepository(u, new SubmodelRepositoryApi(c), spec), 
            ConnectedSubmodelRepository.class);
    }

    /**
     * Customized connected submodel repository to equip subsequent API creation with information on TLS/authentication.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class ExtendedConnectedSubmodelRepository extends ConnectedSubmodelRepository {

        private SubmodelRepositoryApi repoApi;
        private SetupSpec spec;

        /**
         * Creates an instance.
         * 
         * @param submodelRepoUrl the submodel URL to connect to
         * @param submodelRepositoryApi the API instance to use
         * @param spec the setup specification needed for creating subsequence API instanced
         */
        public ExtendedConnectedSubmodelRepository(String submodelRepoUrl, SubmodelRepositoryApi submodelRepositoryApi,
            SetupSpec spec) {
            super(submodelRepoUrl, submodelRepositoryApi);
            this.repoApi = submodelRepositoryApi;
            this.spec = spec;
        }

        @Override
        public ConnectedSubmodelService getConnectedSubmodelService(String submodelId) 
            throws ElementDoesNotExistException {
            try {
                repoApi.getSubmodelById(submodelId, "", "");
                return Tools.createApi(spec.getSetup(AasComponent.SUBMODEL_REPOSITORY), 
                    getSubmodelUrl(submodelId), new ApiClient().setObjectMapper(
                        new JsonMapperFactory().create(new SimpleAbstractTypeResolverFactory().create())), 
                    (b, c, i) -> c.setHttpClientBuilder(b).setRequestInterceptor(i), 
                    (u, c) -> c.updateBaseUri(u), 
                    (u, c) -> new ConnectedSubmodelService(new SubmodelServiceApi(c)), 
                    ConnectedSubmodelService.class);
            } catch (ApiException e) {
                throw mapExceptionSubmodelAccess(submodelId, e);
            }
        }

    }
    
}
