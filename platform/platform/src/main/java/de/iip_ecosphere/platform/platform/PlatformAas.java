/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.platform;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.fixId;

import java.util.Map;

import de.iip_ecosphere.platform.platform.ArtifactsManager.Artifact;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;

/**
 * The platform AAS contributor, in particular for the available artifacts.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAas implements AasContributor {

    public static final String NAME_SUBMODEL = "Artifacts";
    public static final String NAME_COLL_SERVICE_ARTIFACTS = "ServiceArtifacts";
    public static final String NAME_COLL_CONTAINER = "Container";
    public static final String NAME_COLL_KNOWN_SERVICES = "KnownServices";
    public static final String NAME_PROP_ID = "id";
    public static final String NAME_PROP_NAME = "name";
    public static final String NAME_PROP_URI = "uri";
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);

        smB.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICE_ARTIFACTS, false, false).build();
        smB.createSubmodelElementCollectionBuilder(NAME_COLL_CONTAINER, false, false).build();
        SubmodelElementCollectionBuilder b = smB.createSubmodelElementCollectionBuilder(
            NAME_COLL_KNOWN_SERVICES, false, false);
        for (Map.Entry<String, String> ep : ServiceAas.createAas().entrySet()) {
            b.createPropertyBuilder(AasUtils.fixId(ep.getKey()))
                .setValue(Type.STRING, ep.getValue())
                .build();
        }
        b.build();
        smB.build();
        
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
    }

    @Override
    public Kind getKind() {
        return Kind.DYNAMIC;
    }

    @Override
    public boolean isValid() {
        return true;
    }
    
    /**
     * Called to notify that an artifact was created.
     * 
     * @param art the artifact
     */
    static void notifyArtifactCreated(Artifact art) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            
            String collName = null;
            switch (art.getKind()) {
            case SERVICE_ARTIFACT:
                collName = NAME_COLL_SERVICE_ARTIFACTS;
                break;
            case CONTAINER:
                collName = NAME_COLL_CONTAINER;
                break;
            default:
                collName = null;
                break;
            }
            SubmodelElementCollectionBuilder cBuilder // get or create
                = sub.createSubmodelElementCollectionBuilder(collName, false, false);

            SubmodelElementCollectionBuilder dBuilder 
                = cBuilder.createSubmodelElementCollectionBuilder(fixId(art.getId()), false, false);
            dBuilder.createPropertyBuilder(NAME_PROP_ID)
                .setValue(Type.STRING, art.getId())
                .build();
            dBuilder.createPropertyBuilder(NAME_PROP_NAME)
                .setValue(Type.STRING, art.getName())
                .build();
            dBuilder.createPropertyBuilder(NAME_PROP_URI)
                .setValue(Type.STRING, art.getAccessUri().toString())
                .build();
            dBuilder.build();

            cBuilder.build();
        });
    }

    /**
     * Called to notify that an artifact was modified.
     * 
     * @param art the artifact
     */
    static void notifyArtifactModified(Artifact art) {
        // currently no action as there is nothing to update, late timestamps?
    }

    /**
     * Called to notify that an artifact was deleted.
     * 
     * @param art the artifact
     */
    static void notifyArtifactDeleted(Artifact art) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            SubmodelElementCollection coll = sub.getSubmodelElementCollection(fixId(art.getId()));
            if (null != coll) {
                sub.delete(coll);
            }
        });
    }
    
}
