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

import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.net.HttpClientHelper;

import java.net.http.HttpClient;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonMapperFactory;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.SimpleAbstractTypeResolverFactory;
import org.eclipse.digitaltwin.basyx.aasrepository.client.ConnectedAasRepository;
import org.eclipse.digitaltwin.basyx.aasrepository.client.internal.AssetAdministrationShellRepositoryApi;
import org.eclipse.digitaltwin.basyx.client.internal.ApiClient;

/**
 * Supporting methods for AAS repositories.
 * 
 * @author Holger Eichelberger, SSE
 */
class AasRepositoryUtils {

    /**
     * Specialized {@link ApiClient} to prevent unneeded instances of Http client.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class AasRepositoryApiClient extends ApiClient {
        
        /**
         * Creates an instance.
         * 
         * @param builder the HTTP client builder
         */
        public AasRepositoryApiClient(HttpClient.Builder builder) {
            super(builder, null, null);
            setObjectMapper(new JsonMapperFactory().create(new SimpleAbstractTypeResolverFactory().create()));
        }

        @Override
        public HttpClient getHttpClient() {
            return HttpClientHelper.inContext(() -> super.getHttpClient());
        }

    }

    /**
     * Creates an API instance.
     * 
     * @param spec the setup specification containing endpoint and keystore descriptor
     * @param uri specific URI, may be <b>null</b> for {@code endpoint}
     * @return the API instance
     */
    static ConnectedAasRepository createRepositoryApi(SetupSpec spec, String uri) {
        return Tools.getApi(spec.getSetup(AasComponent.AAS_REPOSITORY), uri, 
            h -> new AasRepositoryApiClient(h), 
            (c, i) -> c.setRequestInterceptor(i), 
            (u, c) -> c.updateBaseUri(u), 
            (u, c) -> new ConnectedAasRepository(u, new AssetAdministrationShellRepositoryApi(c)), 
            ConnectedAasRepository.class);
    }
    
}
