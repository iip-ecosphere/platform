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

package test.de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.services.AbstractServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;
import de.iip_ecosphere.platform.services.TypedDataDescriptor;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * A test service descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyServiceDescriptor extends AbstractServiceDescriptor<MyArtifactDescriptor> {

    private MyServiceDescriptor ensembleLeader;
    
    /**
     * Creates an instance. Call {@link #setClassification(
     *     de.iip_ecosphere.platform.services.environment.ServiceKind, boolean, boolean)} afterwards.
     * 
     * @param id the service id
     * @param name the name of this service
     * @param description the description of the service
     * @param version the version
     */
    protected MyServiceDescriptor(String id, String name, String description, Version version) {
        super(id, name, description, version);
    }
    
    @Override
    public void addParameter(TypedDataDescriptor parameter) {
        super.addParameter(parameter);
    }

    @Override
    public void addInputDataConnector(TypedDataConnectorDescriptor input) {
        super.addInputDataConnector(input);
    }

    @Override
    public void addOutputDataConnector(TypedDataConnectorDescriptor output) {
        super.addOutputDataConnector(output);
    }
    
    @Override
    public ServiceDescriptor getEnsembleLeader() {
        return ensembleLeader;
    }
    
    /**
     * Sets the ensemble leader.
     * 
     * @param ensembleLeader the ensemble leader
     */
    void setEnsembleLeader(MyServiceDescriptor ensembleLeader) {
        this.ensembleLeader = ensembleLeader;
    }
    
    @Override
    public String toString() {
        return getId();
    }

    @Override
    protected Class<MyArtifactDescriptor> getArtifactDescriptorClass() {
        return MyArtifactDescriptor.class;
    }

    @Override
    public InvocablesCreator getInvocablesCreator() {
        return null;
    }
    
}