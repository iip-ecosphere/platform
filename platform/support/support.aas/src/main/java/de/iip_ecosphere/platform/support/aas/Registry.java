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
import java.util.List;

/**
 * Provides access to AAS/submodels via a (remote) registry. This also allows for deploying new AAS to a remote 
 * registry.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Registry {

    /**
     * Retrieves an AAS.
     * 
     * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn}, see {@link IdentifierType} for others)
     * @return the AAS (may be <b>null</b> if the AAS does not exist)
     * @throws IOException if accessing the AAS fails for some reason
     */
    public Aas retrieveAas(String identifier) throws IOException;

    /**
     * Retrieves a submodel for an AAS.
     * 
     * @param aasIdentifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
     *    {@code idShort}, interpreted as an URN if this starts with {@code urn}, see {@link IdentifierType} for others)
     * @param submodelIdentifier the identifier of the submodel (may be <b>null</b> or empty for an identification 
     *    based on {@code idShort}, interpreted as an URN if this starts with {@code urn}, see {@link IdentifierType} 
     *    for others)
     * @return the AAS (may be <b>null</b> if the AAS does not exist)
     * @throws IOException if accessing the AAS fails for some reason
     */
    public Submodel retrieveSubmodel(String aasIdentifier, String submodelIdentifier) throws IOException;

    /**
     * Creates from the given {@code aas} an AAS at the specified qualified {@code endpoint}. In other words,
     * deploys {@code aas} to {@code endpoint}.
     * 
     * @param aas the AAS to deploy
     * @param endpointURL the endpoint URL of the AAS
     */
    public void createAas(Aas aas, String endpointURL);

    /**
     * Creates a submodel for the specified stand-alone submodel (as created by 
     * {@link AasFactory#createSubmodelBuilder(String, String)}. In other words, deploys {@code submodel} into the 
     * endpoint of {@code aas}.
     * 
     * @param aas the hosting AAS
     * @param submodel the submodel to deploy
     */
    public void createSubmodel(Aas aas, Submodel submodel);

    /**
     * Registers an accessible, already existing sub-model for an AAS.
     * 
     * @param aas the AAS
     * @param submodel the submodel
     * @param endpointUrl the endpoint URL denoting the sub-model, if <b>null</b> the default endpoint for 
     *    the <code>submodel</code> is used
     */
    public void register(Aas aas, Submodel submodel, String endpointUrl);
    
    /**
     * Retrieves the short ids from registered AAS descriptors.
     * 
     * @return the short ids
     */
    public List<String> getAasIdShorts();

    /**
     * Retrieves the identifiers (e.g., URNs) from registered AAS descriptors.
     * 
     * @return the identifiers
     */
    public List<String> getAasIdentifiers();
   
    /**
     * Returns the URI of a (deployed) AAS.
     * 
     * @param aasIdShort the short Id of the AAS to look for
     * @return the URI within this registry
     */
    public String getEndpoint(String aasIdShort);
    
    /**
     * Returns the URI of a (deployed) AAS.
     * 
     * @param aas the AAS
     * @return the URI within this registry
     */
    public String getEndpoint(Aas aas);

    /**
     * Returns the URI of a (deployed) submodel.
     * 
     * @param aas the AAS
     * @param submodel the submodel
     * @return the URI within this registry
     */
    public String getEndpoint(Aas aas, Submodel submodel);

}
