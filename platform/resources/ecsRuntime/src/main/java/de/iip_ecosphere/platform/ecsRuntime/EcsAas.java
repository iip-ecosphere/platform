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

package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;

/**
 * Implements the AAS for the ECS runtime.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsAas implements AasContributor {

    public static final String NAME_SUBMODEL = "resource"; // TODO ECS id
    public static final String NAME_PROP_CSYS_NAME = "containerSystemName";
    public static final String NAME_PROP_CSYS_VERSION = "containerSystemVersion";
    public static final String NAME_COLL_CONTAINERS = "containers";
    public static final String NAME_PROP_ID = "id";
    public static final String NAME_PROP_NAME = "name";
    public static final String NAME_PROP_VERSION = "version";
    public static final String NAME_PROP_STATE = "state";

    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        ContainerManager mgr = EcsFactory.getContainerManager();
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);
        // TODO resource information
        smB.createPropertyBuilder(NAME_PROP_CSYS_NAME)
            .setValue(Type.STRING, mgr.getContainerSystemName())
            .build();
        smB.createPropertyBuilder(NAME_PROP_CSYS_VERSION)
            .setValue(Type.STRING, mgr.getContainerSystemVersion())
            .build();
        for (ContainerDescriptor desc : mgr.getContainers()) {
            addContainer(smB, desc);
        }
        smB.build();
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    /**
     * Adds a container to the given submodel builder.
     * 
     * @param smB represents the submodel in creation
     * @param desc the descriptor to be added
     */
    private static void addContainer(SubmodelBuilder smB, ContainerDescriptor desc) {
        SubmodelElementCollectionBuilder cBuilder // get or create
            = smB.createSubmodelElementCollectionBuilder(NAME_COLL_CONTAINERS, false, false); 
        
        SubmodelElementCollectionBuilder dBuilder 
            = cBuilder.createSubmodelElementCollectionBuilder(desc.getId(), false, false);
        dBuilder.createPropertyBuilder(NAME_PROP_ID)
            .setValue(Type.STRING, desc.getId())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_NAME)
            .setValue(Type.STRING, desc.getName())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_VERSION)
            .setValue(Type.STRING, desc.getVersion().toString())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_STATE)
            .setValue(Type.STRING, desc.getState().toString())
            .build();
        dBuilder.build();
        
        cBuilder.build();
    }
    
    /**
     * Is called when a container is added.
     * 
     * @param desc the container descriptor 
     */
    public static void notifyContainerStarted(ContainerDescriptor desc) {
    }

    /**
     * Is called when a container is removed.
     * 
     * @param desc the container descriptor 
     */
    public static void notifyContainerStopped(ContainerDescriptor desc) {
    }

    /**
     * Is called when a container state changed.
     * 
     * @param desc the container descriptor 
     */
    public static void notifyContainerStateChanged(ServiceDescriptor desc) {
    }

}
