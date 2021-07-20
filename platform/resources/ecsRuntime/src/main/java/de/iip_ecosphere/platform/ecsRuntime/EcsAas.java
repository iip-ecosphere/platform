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

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.*;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;

/**
 * Implements the AAS for the ECS runtime. Container ids used as short AAS ids may be translated into ids that are
 * valid from the perspective of the AAS implementation. All nested elements also carry their original id in 
 * {@link #NAME_PROP_ID}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsAas implements AasContributor {

    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_RESOURCES; 
    public static final String NAME_PROP_CSYS_NAME = "containerSystemName";
    public static final String NAME_PROP_CSYS_VERSION = "containerSystemVersion";
    public static final String NAME_COLL_CONTAINERS = "containers";
    public static final String NAME_PROP_ID = "id";
    public static final String NAME_PROP_NAME = "name";
    public static final String NAME_PROP_VERSION = "version";
    public static final String NAME_PROP_STATE = "state";
    public static final String NAME_PROP_RESOURCE = "resource";
    
    public static final String NAME_OP_GET_STATE = "getState";
    public static final String NAME_OP_CONTAINER_ADD = "addContainer";
    public static final String NAME_OP_CONTAINER_UNDEPLOY = "undeployContainer";
    public static final String NAME_OP_CONTAINER_UPDATE = "updateContainer";
    public static final String NAME_OP_CONTAINER_MIGRATE = "migrateContainer";
    public static final String NAME_OP_CONTAINER_STOP = "stopContainer";
    public static final String NAME_OP_CONTAINER_START = "startContainer";

    private static final String ID_SUBMODEL = null; // take the short name, shall become public and an URN later
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        ContainerManager mgr = EcsFactory.getContainerManager();
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);

        SubmodelElementCollectionBuilder jB 
            = smB.createSubmodelElementCollectionBuilder(Id.getDeviceIdAas(), false, false);

        MetricsAasConstructor.addProviderMetricsToAasSubmodel(jB, iCreator, null, s -> getQName(s));

        jB.createPropertyBuilder(NAME_PROP_CSYS_NAME)
            .setValue(Type.STRING, mgr.getContainerSystemName())
            .build();
        jB.createPropertyBuilder(NAME_PROP_CSYS_VERSION)
            .setValue(Type.STRING, mgr.getContainerSystemVersion())
            .build();

        createIdOp(jB, NAME_OP_CONTAINER_START, iCreator);
        createIdOp(jB, NAME_OP_CONTAINER_MIGRATE, iCreator, "location");
        createIdOp(jB, NAME_OP_CONTAINER_UPDATE, iCreator, "location");
        createIdOp(jB, NAME_OP_CONTAINER_UNDEPLOY, iCreator);
        createIdOp(jB, NAME_OP_CONTAINER_STOP, iCreator);
        createIdOp(jB, NAME_OP_GET_STATE, iCreator);
        jB.createOperationBuilder(NAME_OP_CONTAINER_ADD)
            .setInvocable(iCreator.createInvocable(getQName(NAME_OP_CONTAINER_ADD)))
            .addInputVariable("url", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .build();

        jB.build();

        for (ContainerDescriptor desc : mgr.getContainers()) {
            addContainer(smB, desc);
        }

        smB.defer(); // join with services if present, build done by AAS
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(NAME_OP_CONTAINER_START), 
            new JsonResultWrapper(p -> {
                EcsFactory.getContainerManager().startContainer(readString(p)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_CONTAINER_MIGRATE), 
            new JsonResultWrapper(p -> { 
                EcsFactory.getContainerManager().migrateContainer(readString(p), readString(p, 1)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_CONTAINER_UPDATE), 
            new JsonResultWrapper(p -> { 
                EcsFactory.getContainerManager().updateContainer(readString(p), readUri(p, 1, EMPTY_URI)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_CONTAINER_UNDEPLOY), 
            new JsonResultWrapper(p -> { 
                EcsFactory.getContainerManager().undeployContainer(readString(p)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_CONTAINER_STOP), 
            new JsonResultWrapper(p -> { 
                EcsFactory.getContainerManager().stopContainer(readString(p)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_GET_STATE), 
            new JsonResultWrapper(p -> { 
                return EcsFactory.getContainerManager().getState(readString(p)); 
            }
        ));

        sBuilder.defineOperation(getQName(NAME_OP_CONTAINER_ADD), 
            new JsonResultWrapper(p -> { 
                return EcsFactory.getContainerManager().addContainer(readUri(p, 0, EMPTY_URI)); 
            }
        ));
        MetricsAasConstructor.addMetricsProtocols(sBuilder, Monitor.getMetricsProvider(), null, s -> getQName(s));
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    /**
     * Creates an operation with a String parameter "id" and optional string parameters and a result of type string. 
     * The operation name is derived from {@code name} applied to {@link #getQName(String)}.
     * 
     * @param smB the submodel element collection builder
     * @param name the operation name
     * @param iCreator the invocables creator
     * @param otherParams other String parameters
     */
    private void createIdOp(SubmodelElementCollectionBuilder smB, String name, InvocablesCreator iCreator, 
        String... otherParams) {
        OperationBuilder oBuilder = smB.createOperationBuilder(name)
            .setInvocable(iCreator.createInvocable(getQName(name)))
            .addInputVariable(NAME_PROP_ID, Type.STRING);
        for (String p : otherParams) {
            oBuilder.addInputVariable(p, Type.STRING);
        }
        oBuilder.addOutputVariable("result", Type.STRING);
        oBuilder.build();
    }

    /**
     * Returns the qualified name for an operation/property implementation.
     * 
     * @param elementName the element name
     * @return the qualified name
     */
    public static String getQName(String elementName) {
        return NAME_SUBMODEL + "_" + elementName;
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
            = cBuilder.createSubmodelElementCollectionBuilder(fixId(desc.getId()), false, false);
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
        dBuilder.createPropertyBuilder(NAME_PROP_RESOURCE)
            .setValue(Type.STRING, Id.getDeviceIdAas())
            .build();
        dBuilder.build();
        
        cBuilder.build();
    }
    
    /**
     * Is called when a container is added.
     * 
     * @param desc the container descriptor 
     */
    public static void notifyContainerAdded(ContainerDescriptor desc) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            SubmodelBuilder builder = aas.createSubmodelBuilder(NAME_SUBMODEL, ID_SUBMODEL);
            addContainer(builder, desc);
            builder.build();
        });
    }

    /**
     * Is called when a container is removed.
     * 
     * @param desc the container descriptor 
     */
    public static void notifyContainerRemoved(ContainerDescriptor desc) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            SubmodelElementCollection coll = sub.getSubmodelElementCollection(NAME_COLL_CONTAINERS);
            coll.deleteElement(fixId(desc.getId()));
        });
    }

    /**
     * Is called when a container state changed.
     * 
     * @param desc the container descriptor 
     */
    public static void notifyContainerStateChanged(ContainerDescriptor desc) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            // other approach... link property against container descriptor while creation and reflect state
            // let's try this one for now
            SubmodelElementCollection elt = sub.getSubmodelElementCollection(NAME_COLL_CONTAINERS)
                .getSubmodelElementCollection(fixId(desc.getId()));
            if (null != elt) {
                Property prop = elt.getProperty(NAME_PROP_STATE);
                if (null != prop) {
                    try {
                        prop.setValue(desc.getState().toString());
                    } catch (ExecutionException e) {
                        getLogger().error("Cannot write state for container `" + desc.getId() + "`: " + e.getMessage());
                    }
                } else {
                    getLogger().error("Container state change - cannot find property " + NAME_PROP_STATE 
                        + "for container `" + desc.getId());
                }
            } else {
                getLogger().error("Container state change - cannot find container `" + desc.getId() + "`");
            }
        });
    }

    /**
     * Returns the logger instance.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(EcsAas.class);
    }
    
    @Override
    public boolean isValid() {
        return EcsFactory.getContainerManager() != null; // may happen in platform when this is used as lib only 
    }

}
