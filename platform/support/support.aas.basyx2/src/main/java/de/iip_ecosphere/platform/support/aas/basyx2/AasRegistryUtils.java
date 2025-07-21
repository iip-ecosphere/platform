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
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.basyx.aasregistry.client.ApiClient;
import org.eclipse.digitaltwin.basyx.aasregistry.client.ApiException;
import org.eclipse.digitaltwin.basyx.aasregistry.client.api.RegistryAndDiscoveryInterfaceApi;
import org.eclipse.digitaltwin.basyx.aasregistry.client.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.basyx.aasregistry.client.model.Endpoint;
import org.eclipse.digitaltwin.basyx.aasregistry.client.model.ProtocolInformation;
import org.eclipse.digitaltwin.basyx.aasrepository.client.ConnectedAasRepository;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Supporting methods for AAS registries. An own class is more convenient to separate the imports for descriptor, 
 * endpoint etc. as some between submodels and aas have the same simple names.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasRegistryUtils {

    /**
     * Returns the first endpoint from {@code desc}.
     * 
     * @param desc the descriptor to take the endpoint from
     * @return the endpoint or <b>null</b> for none
     */
    static Endpoint getFirstEndpoint(AssetAdministrationShellDescriptor desc) {
        Endpoint result = null;
        if (null != desc) {
            List<Endpoint> eps = desc.getEndpoints();
            if (null != eps && !eps.isEmpty()) {
                result = eps.getFirst();
            }
        }
        return result;
    }

    /**
     * Returns whether one of the endpoints in the given descriptor {@code desc} has the specified href/URL 
     * {@code epUrl}.
     * 
     * @param desc the descriptor to examine
     * @param epUrl the URL/href to look for
     * @return whether there is such an endpoint
     */
    static boolean hasEndpointUrl(AssetAdministrationShellDescriptor desc, String epUrl) {
        boolean found = false;
        if (null != desc && !StringUtils.isBlank(epUrl)) {
            List<Endpoint> eps = desc.getEndpoints();
            if (null != eps && !eps.isEmpty()) {
                Optional<String> href = eps.stream()
                    .filter(e -> e != null)
                    .map(e -> e.getProtocolInformation())
                    .filter(p -> p != null)
                    .map(p -> p.getHref())
                    .filter(h -> epUrl.equals(h))
                    .findAny();
                found = href.isPresent();
            }
        }
        return found;
    }

    /**
     * Returns the first endpoint URL from {@code desc}.
     * 
     * @param desc the descriptor to take the endpoint from
     * @return the endpoint URL or <b>null</b> for none
     */
    static String getFirstEndpointUrl(AssetAdministrationShellDescriptor desc) {
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
            ep.setProtocolInformation(pi);
            ep.setInterface("AAS-3.0");
            result.add(ep);
        }
        return result;
    }
    
    /**
     * Creates an AAS descriptor for {@code sm}.
     * 
     * @param aas the AAS
     * @param baseUrl the target repository URL
     * @param fullUrl whether the given URL is a base or a full URL
     * @return the descriptor
     */
    static AssetAdministrationShellDescriptor createDescriptor(Aas aas, String baseUrl, boolean fullUrl) {
        AssetAdministrationShellDescriptor desc = new AssetAdministrationShellDescriptor();
        desc.setId(aas.getIdentification());
        desc.setIdShort(aas.getIdShort());
        // TODO more information, requires interface changes
        if (!StringUtils.isBlank(baseUrl)) {
            String url = baseUrl;
            if (!fullUrl) {
                url += "/shells/" + aas.getIdentification();
            }
            desc.setEndpoints(toEndpoints(url));
        }
        return desc;
    }
    
    /**
     * Posts the descriptor for the given {@code aas}.
     * 
     * @param aas the AAS
     * @param reg the AAS registry
     * @param repo the AAS repository (for URL composition)
     * @param overwrite whether existing information shall be overwritten
     */
    static void postDescriptor(Aas aas, RegistryAndDiscoveryInterfaceApi reg, ConnectedAasRepository repo, 
        boolean overwrite) {
        try {
            AssetAdministrationShellDescriptor desc = overwrite ? null 
                : reg.getAssetAdministrationShellDescriptorById(aas.getIdentification());
            if (null == desc) {
                reg.postAssetAdministrationShellDescriptor(createDescriptor(aas, repo.getBaseUrl(), true));
            }
        } catch (ApiException e) {
            LoggerFactory.getLogger(AasRegistryUtils.class).error("While posting/registering AAS descriptor '{}': {}", 
                aas.getIdentification(), e.getMessage());
        }
    }

    /**
     * Creates an API instance.
     * 
     * @param spec the setup specification containing endpoint and keystore descriptor
     * @param uri specific URI, may be <b>null</b> for {@code endpoint}
     * @return the API instance
     */
    static RegistryAndDiscoveryInterfaceApi createRegistryApi(SetupSpec spec, String uri) {
        return Tools.createApi(spec.getSetup(AasComponent.AAS_REGISTRY), uri, new ApiClient(), 
            (b, c, i) -> c.setHttpClientBuilder(b).setRequestInterceptor(i), 
            (u, c) -> c.updateBaseUri(u), 
            (u, c) -> new RegistryAndDiscoveryInterfaceApi(c), 
            RegistryAndDiscoveryInterfaceApi.class);
    }

}
