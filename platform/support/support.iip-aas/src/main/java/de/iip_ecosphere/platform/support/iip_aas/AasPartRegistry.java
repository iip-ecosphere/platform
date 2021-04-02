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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;

import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.DeploymentRecipe.ImmediateDeploymentRecipe;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;

/**
 * A registry for {@link AasContributor} instances to be loaded via the Java Service loader.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasPartRegistry {

    /**
     * The name of the top-level AAS created by this registry in {@link #build()}.
     */
    public static final String NAME_AAS = "IIP_Ecosphere";
    
    // common submodel names are declared already here to relax dependencies
    public static final String NAME_SUBMODEL_RESOURCES = "resources"; 
    
    /**
     * The URN of the top-level AAS created by this registry in {@link #build()}.
     */
    public static final String URN_AAS = "urn:::AAS:::iipEcosphere#";
    
    public static final Schema DEFAULT_SCHEMA = Schema.HTTP;
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_PROTOCOL_PORT = 9000;
    public static final String DEFAULT_ENDPOINT = "registry";
    public static final String DEFAULT_PROTOCOL = AasFactory.DEFAULT_PROTOCOL;
    
    // TODO local vs. global
    public static final Endpoint DEFAULT_EP = new Endpoint(DEFAULT_SCHEMA, DEFAULT_HOST, 
        DEFAULT_PORT, DEFAULT_ENDPOINT);
    private static Endpoint aasEndpoint = DEFAULT_EP;
    public static final ServerAddress DEFAULT_IMPL = new ServerAddress(Schema.IGNORE, 
            aasEndpoint.getHost(), DEFAULT_PROTOCOL_PORT);
    private static ServerAddress protocolAddress = DEFAULT_IMPL;

    /**
     * Defines the AAS endpoint.
     * 
     * @param endpoint the registry endpoint 
     * @return the endpoint before this call
     */
    public static Endpoint setAasEndpoint(Endpoint endpoint) {
        Endpoint old = aasEndpoint;
        aasEndpoint = endpoint;
        return old;
    }

    /**
     * Defines the operation/property implementation protocol address.
     * 
     * @param address the address
     * @return the address before this call
     */
    public static ServerAddress setProtocolAddress(ServerAddress address) {
        ServerAddress old = protocolAddress;
        protocolAddress = address;
        return old;
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
        Set<Class<? extends AasContributor>> result = new HashSet<Class<? extends AasContributor>>();
        Iterator<AasContributor> iter = contributors();
        while (iter.hasNext()) {
            result.add(iter.next().getClass());
        }
        return result;
    }

    /**
     * Represents the result of building the platform AAS.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class AasBuildResult {
        
        private List<Aas> aas;
        private ProtocolServerBuilder sBuilder;
        
        /**
         * Creates an instance.
         * 
         * @param aas the created AAS
         * @param sBuilder the server builder
         */
        private AasBuildResult(List<Aas> aas, ProtocolServerBuilder sBuilder) {
            this.aas = aas;
            this.sBuilder = sBuilder;
        }
        
        /**
         * Returns the list of created AAS.
         * 
         * @return the created AAS
         */
        public List<Aas> getAas() {
            return aas;
        }
        
        /**
         * Returns the protocol server builder instance.
         * 
         * @return the server builder instance
         */
        public ProtocolServerBuilder getProtocolServerBuilder() {
            return sBuilder;
        }
    }

    /**
     * Build up all AAS of the currently running platform part including all contributors. [public for testing]
     * 
     * @return the list of AAS
     */
    public static AasBuildResult build() {
        return build(c -> true);
    }
    
    /**
     * Build up all AAS of the currently running platform part. [public for testing]
     * 
     * @param filter filter out contributors, in particular for testing, e.g., active AAS that require an 
     * implementation server
     * 
     * @return the list of AAS
     */
    public static AasBuildResult build(Predicate<AasContributor> filter) {
        List<Aas> aas = new ArrayList<>();
        AasFactory factory = AasFactory.getInstance();
        AasBuilder aasBuilder = factory.createAasBuilder(NAME_AAS, URN_AAS);
        InvocablesCreator iCreator = factory.createInvocablesCreator(DEFAULT_PROTOCOL, 
            protocolAddress.getHost(), protocolAddress.getPort());
        ProtocolServerBuilder sBuilder = factory.createProtocolServerBuilder(DEFAULT_PROTOCOL, 
            protocolAddress.getPort());
        Iterator<AasContributor> iter = contributors();
        while (iter.hasNext()) {
            AasContributor contributor = iter.next();
            if (filter.test(contributor)) {
                Aas partAas = contributor.contributeTo(aasBuilder, iCreator);
                contributor.contributeTo(sBuilder);
                if (null != partAas) {
                    aas.add(partAas);
                }
            }
        }
        aas.add(0, aasBuilder.build());
        return new AasBuildResult(aas, sBuilder);
    }
    
    /**
     * Obtains the IIP-Ecosphere platform AAS. Be careful with the returned instance, as if
     * the AAS is modified in the mean time, you may hold an outdated instance.
     * 
     * @return the platform AAS (may be <b>null</b> for none)
     * @throws IOException if the AAS cannot be read due to connection errors
     */
    public static Aas retrieveIipAas() throws IOException {
        return AasFactory.getInstance().obtainRegistry(aasEndpoint).retrieveAas(URN_AAS);
    }
    
    /**
     * Deploy the given AAS to a local server. [preliminary]
     * 
     * @param aas the list of aas, e.g., from {@link #build()}
     * @return the server instance
     */
    public static Server deploy(List<Aas> aas) {
        ImmediateDeploymentRecipe dBuilder = AasFactory.getInstance()
            .createDeploymentRecipe(new Endpoint(aasEndpoint, ""))
            .addInMemoryRegistry(aasEndpoint.getEndpoint());
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
