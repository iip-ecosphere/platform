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

package de.iip_ecosphere.platform.services.spring.metricsProvider.metricsAas;

import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;

/**
 * Contains the needed objects required by the {@link MetricsAasConstructor} to
 * execute operations.<br>
 * As the objects we need to use to correctly construct the AAS submodel and the
 * Protocol Server are needed all together, but we also want to have a way to
 * modify them in the coming future, the creation of this bundle was created in
 * order to have an in-out set of arguments.<br>
 * The instances of {@link InvocablesCreator} and
 * {@link MetricsExtractorRestClient} are only used as input arguments, but the
 * instances of both {@link SubmodelBuilder} and {@link ProtocolServerBuilder}
 * are used both as input arguments and output arguments, containing the
 * resulting structure after the relevant information is added.
 * 
 * @author Miguel Gomez
 */
public class MetricsAasConstructionBundle {

    private SubmodelBuilder smBuilder;
    private ProtocolServerBuilder pBuilder;
    private InvocablesCreator iCreator;
    private MetricsExtractorRestClient client;

    /**
     * Initializes a new instance of a MetricsAasConstructionBundle.<br>
     * The Bundle will contain all the information needed by the
     * {@link MetricsAasConstructor} to operate as well as save the changes.
     * 
     * @param smBuilder submodel builder of the AAS
     * @param pBuilder  protocol server builder of the AAS
     * @param iCreator  invocables creator of the AAS
     * @param client    REST client responsible of retrieving the data
     * @throws IllegalArgumentException if any of the arguments is null
     */
    public MetricsAasConstructionBundle(SubmodelBuilder smBuilder, ProtocolServerBuilder pBuilder,
        InvocablesCreator iCreator, MetricsExtractorRestClient client) {
        setSubmodelBuilder(smBuilder);
        setProtocolBuilder(pBuilder);
        setInvocablesCreator(iCreator);
        setClient(client);
    }

    /**
     * Sets the submodel builder.
     * 
     * @param smBuilder submodel builder
     * @throws IllegalArgumentException if the argument is null
     */
    public void setSubmodelBuilder(SubmodelBuilder smBuilder) {
        if (smBuilder == null) {
            throw new IllegalArgumentException("The Submodel Builder is null!");
        }
        this.smBuilder = smBuilder;
    }

    /**
     * Retrieves the submodel builder stored in the bundle.
     * 
     * @return the submodel builder
     */
    public SubmodelBuilder getSubmodelBuilder() {
        return smBuilder;
    }

    /**
     * Sets the protocol server builder.
     * 
     * @param pBuilder protocol server builder
     * @throws IllegalArgumentException if the argument is null
     */
    public void setProtocolBuilder(ProtocolServerBuilder pBuilder) {
        if (pBuilder == null) {
            throw new IllegalArgumentException("The Protocol Server Builder is null!");
        }
        this.pBuilder = pBuilder;
    }

    /**
     * Retrieves the protocol server builder stored in the bundle.
     * 
     * @return the protocol server builder
     */
    public ProtocolServerBuilder getProtocolBuilder() {
        return pBuilder;
    }

    /**
     * Sets the invocables creator.
     * 
     * @param iCreator invocables creator
     * @throws IllegalArgumentException if the argument is null
     */
    public void setInvocablesCreator(InvocablesCreator iCreator) {
        if (iCreator == null) {
            throw new IllegalArgumentException("The Invocables creator is null!");
        }
        this.iCreator = iCreator;
    }

    /**
     * Retrieves the invocables creator stored in the bundle.
     * 
     * @return the invocables creator
     */
    public InvocablesCreator getInvocablesCreator() {
        return iCreator;
    }

    /**
     * Sets the REST client.
     * 
     * @param client REST client
     * @throws IllegalArgumentException if the argument is null
     */
    public void setClient(MetricsExtractorRestClient client) {
        if (client == null) {
            throw new IllegalArgumentException("The Submodel Builder is null!");
        }
        this.client = client;
    }

    /**
     * Retrieves the REST client stored in the bundle.
     * 
     * @return the REST client
     */
    public MetricsExtractorRestClient getClient() {
        return client;
    }

}
