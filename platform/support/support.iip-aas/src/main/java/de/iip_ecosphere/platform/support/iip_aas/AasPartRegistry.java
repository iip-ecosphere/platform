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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe;

/**
 * A registry for {@link AasContributor} instances to be loaded via the Java Service loader.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasPartRegistry {

    /**
     * The name of the top-level AAS created by this registry in {@link #build()}.
     */
    public static final String NAME_AAS = "IIP-Ecosphere";
    
    /**
     * The URN of the top-level AAS created by this registry in {@link #build()}.
     */
    public static final String URN_AAS = "urn:::AAS:::iipEcosphere#";
    
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_ENDPOINT = "registry";
    
    // TODO local vs. global
    private static String host = DEFAULT_HOST;
    private static int port = DEFAULT_PORT;
    private static String endpoint = DEFAULT_ENDPOINT;

    /**
     * Defines the AAS endpoint.
     * 
     * @param nHost the host name
     * @param nPort the TCP port
     * @param nEndpoint the registry endpoint 
     */
    public static void setAasEndpoint(String nHost, int nPort, String nEndpoint) {
        host = nHost;
        port = nPort;
        endpoint = nEndpoint;
    }
    
    /**
     * Returns the contributor loader.
     * 
     * @return the loader instance
     */
    private static ServiceLoader<AasContributor> getContributorLoader() {
        return ServiceLoader.load(AasContributor.class);        
    }
    
    /**
     * Returns the contributors.
     * 
     * @return the contributors
     */
    public static Iterator<AasContributor> contributors() {
        return getContributorLoader().iterator();
    }
    
    /**
     * Returns the contributor classes.
     * 
     * @return the contributor classes
     */
    public static Set<Class<? extends AasContributor>> contributorClasses() {
        return getContributorLoader()
            .stream()
            .map((p) -> p.get().getClass())
            .collect(Collectors.toSet());        
    }
    
    /**
     * Build up all AAS of the currently running platform part. [public for testing]
     * 
     * @return the list of AAS
     */
    public static List<Aas> build() {
        List<Aas> aas = new ArrayList<>();
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder(NAME_AAS, URN_AAS);
        Iterator<AasContributor> iter = contributors();
        while (iter.hasNext()) {
            AasContributor contributor = iter.next();
            Aas partAas = contributor.contributeTo(aasBuilder);
            if (null != partAas) {
                aas.add(partAas);
            }
        }
        aas.add(0, aasBuilder.build());
        return aas;
    }
    
    /**
     * Obtains the IIP-Ecosphere platform AAS. Be careful with the returned instance, as if
     * the AAS is modified in the mean time, you may hold an outdated instance.
     * 
     * @return the platform AAS (may be <b>null</b> for none)
     * @throws IOException if the AAS cannot be read due to connection errors
     */
    public static Aas retrieveIipAas() throws IOException {
        return AasFactory.getInstance().retrieveAas(host, port, endpoint, URN_AAS);
    }
    
    /**
     * Deploy the given AAS to a local server. [preliminary]
     * 
     * @param aas the list of aas, e.g., from {@link #build()}
     * @return the server instance
     */
    public static Server deploy(List<Aas> aas) {
        DeploymentRecipe dBuilder = AasFactory.getInstance().createDeploymentRecipe(host, port);
        dBuilder.addInMemoryRegistry(endpoint);
        for (Aas a: aas) {
            dBuilder.deploy(a);
        }
        return dBuilder.createServer();
    }
    
    /**
     * Returns the first AAS in {@code list} matching the given name. [utility]
     * 
     * @param list the list to consider
     * @param idShort the short name to filter for
     * @return the first AAS or <b>null</b> for none
     */
    public static Aas getAas(List<Aas> list, String idShort) {
        return list.stream()
            .filter(a -> a.getIdShort().equals(idShort))
            .findFirst()
            .orElse(null);
    }

}
