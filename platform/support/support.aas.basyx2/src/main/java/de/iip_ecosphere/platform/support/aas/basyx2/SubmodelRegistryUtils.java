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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.digitaltwin.basyx.submodelregistry.client.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.basyx.submodelrepository.client.ConnectedSubmodelRepository;

import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

import org.eclipse.digitaltwin.basyx.submodelregistry.client.model.ProtocolInformation;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiClient;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.ApiException;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.api.SubmodelRegistryApi;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.model.Endpoint;

/**
 * Supporting methods for submodel registries. An own class is more convenient to separate the imports for descriptor, 
 * endpoint etc. as some between submodels and aas have the same simple names.
 * 
 * @author Holger Eichelberger, SSE
 */
class SubmodelRegistryUtils {

    /**
     * Returns the first endpoint from {@code desc}.
     * 
     * @param desc the descriptor to take the endpoint from
     * @return the endpoint or <b>null</b> for none
     */
    static Endpoint getFirstEndpoint(SubmodelDescriptor desc) {
        Endpoint result = null;
        if (null != desc) {
            List<Endpoint> eps = desc.getEndpoints();
            if (!eps.isEmpty()) {
                result = eps.getFirst();
            }
        }
        return result;
    }

    /**
     * Returns the first endpoint URL from {@code desc}.
     * 
     * @param desc the descriptor to take the endpoint from
     * @return the endpoint URL or <b>null</b> for none
     */
    static String getFirstEndpointUrl(SubmodelDescriptor desc) {
        String result = null;
        Endpoint ep = getFirstEndpoint(desc);
        if (null != ep && null != ep.getProtocolInformation()) {
            result = ep.getProtocolInformation().getHref();
        }
        return result;
    }
    
    /**
     * Turns given URLs into endpoint instances.
     * 
     * @param urls the URLs
     * @return the endpoint instances
     */
    static List<Endpoint> toEndpoints(String... urls) {
        List<Endpoint> result = new ArrayList<>();
        for (String u: urls) {
            Endpoint ep = new Endpoint();
            ProtocolInformation pi = new ProtocolInformation();
            pi.setHref(u);
            pi.setEndpointProtocol("HTTP");
            pi.setEndpointProtocolVersion(List.of("1.1"));
            ep.setProtocolInformation(pi);
            ep.setInterface("SUBMODEL-3.0");
            result.add(ep);
        }
        return result;
    }
    
    /**
     * Creates a submodel descriptor for {@code sm}.
     * 
     * @param sm the submodel
     * @param baseUrl the target repository URL
     * @return the descriptor
     */
    static SubmodelDescriptor createDescriptor(Submodel sm, String baseUrl) {
        SubmodelDescriptor desc = new SubmodelDescriptor();
        desc.setId(sm.getIdentification());
        desc.setIdShort(sm.getIdShort());
        // TODO more information, requires interface changes
        String url = baseUrl + "/submodels/" + sm.getIdentification();
        desc.setEndpoints(toEndpoints(url));
        return desc;
    }
    
    /**
     * Posts the descriptor for the given submodel {@code sm}.
     * 
     * @param sm the submodel
     * @param reg the submodel registry
     * @param repo the AAS repository (for URL composition)
     * @param overwrite whether existing information shall be overwritten
     */
    static void postDescriptor(Submodel sm, SubmodelRegistryApi reg, ConnectedSubmodelRepository repo, 
        boolean overwrite) {
        SubmodelDescriptor desc = null;
        if (!overwrite) {
            try {
                desc = reg.getSubmodelDescriptorById(sm.getIdentification());
            } catch (ApiException e) {
                // it's ok, it's not there -> desc = null
            }
        }
        if (null == desc) {
            try {
                reg.postSubmodelDescriptor(createDescriptor(sm, repo.getBaseUrl()));
            } catch (ApiException e) {
                LoggerFactory.getLogger(SubmodelRegistryUtils.class).error(
                    "While posting/registering submodel descriptor '{}': {}", sm.getIdentification(), e.getMessage());
            }
        }
    }
    
    /**
     * Creates an API instance.
     * 
     * @param spec the setup specification containing endpoint and keystore descriptor
     * @param uri specific URI, may be <b>null</b> for {@code endpoint}
     * @return the API instance
     */
    static SubmodelRegistryApi createRegistryApi(SetupSpec spec, String uri) {
        return Tools.createApi(spec.getSetup(AasComponent.SUBMODEL_REGISTRY), 
            uri, new ApiClient(), 
            (b, c, i) -> c.setHttpClientBuilder(b).setRequestInterceptor(i), 
            (u, c) -> c.updateBaseUri(u), 
            (u, c) -> new SubmodelRegistryApi(c), 
            SubmodelRegistryApi.class);
    }
    
}
